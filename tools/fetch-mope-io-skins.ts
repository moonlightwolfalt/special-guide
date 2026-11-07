#!/usr/bin/env bun
// Downloads the mope.io shop skin images from s3.mope.io and mope.io.
//
// Where the files live:
//   s3.mope.io/mope-prod/items/<itemId>/...      every skin's own textures and its
//                                                 shop picture (<animal>.ui.webp), plus
//                                                 skinned abilities (items/abilities/...).
//                                                 The bucket allows listing, so nothing is guessed.
//   mope.io/assets/animals/<biome>/<animal>/...  the plain animal and its subspecies. A skin
//                                                 folder only holds the parts it changes; the
//                                                 rest of the animal comes from here.
//   api.mope.io/store                            the shop catalog (price, rarity, animal).
//
// Files already on disk are skipped, so running it again only fetches what is missing.
//
// Usage:
//   bun tools/fetch-mope-io-skins.ts [--out DIR] [--only ID[,ID...]] [--concurrency N]
//                                    [--no-base] [--list-only]
//
//   --only        item ids or animal names (e.g. fox,dragon_astral) to limit the download
//   --no-base     skip the mope.io animal textures; only download s3 skin folders
//   --list-only   write skins.json without downloading images
//
// Output:
//   DIR/s3/items/<itemId>/...                    exactly as on s3.mope.io/mope-prod
//   DIR/mope.io/assets/animals/<biome>/<animal>/ exactly as on mope.io
//   DIR/mope.io/assets/UI/icons/store/currency/  coin and gem icons
//   DIR/skins.json                               every skin with its name, price and files

import { mkdir, stat } from "node:fs/promises";
import path from "node:path";

const S3 = "https://s3.mope.io/mope-prod";
const SITE = "https://mope.io";
const API = "https://api.mope.io";
const BIOMES = ["land", "ocean", "arctic", "desert", "volcano"];
const USER_AGENT = "mope-skin-fetch/1.0";

type Opts = { out: string; only: Set<string> | null; concurrency: number; base: boolean; listOnly: boolean };

function parseArgs(argv: string[]): Opts {
    const opts: Opts = { out: "mope-io-skins", only: null, concurrency: 12, base: true, listOnly: false };
    for (let i = 0; i < argv.length; i++) {
        const a = argv[i];
        if (a === "--out") opts.out = argv[++i];
        else if (a === "--only") opts.only = new Set(argv[++i].split(",").map(s => s.trim()).filter(Boolean));
        else if (a === "--concurrency") opts.concurrency = Math.max(1, parseInt(argv[++i], 10) || 1);
        else if (a === "--no-base") opts.base = false;
        else if (a === "--list-only") opts.listOnly = true;
        else if (a === "-h" || a === "--help") {
            console.log("Usage: bun tools/fetch-mope-io-skins.ts [--out DIR] [--only ID[,ID...]] [--concurrency N] [--no-base] [--list-only]");
            process.exit(0);
        } else {
            console.error(`Unknown argument: ${a}`);
            process.exit(1);
        }
    }
    return opts;
}

// --- HTTP ---------------------------------------------------------------------

class HttpError extends Error {
    constructor(public status: number, url: string) {
        super(`HTTP ${status} for ${url}`);
    }
}

async function get(url: string, timeoutMs = 60000, retries = 3): Promise<Response> {
    let lastErr: unknown;
    for (let attempt = 0; attempt <= retries; attempt++) {
        try {
            const res = await fetch(url, { headers: { "User-Agent": USER_AGENT }, signal: AbortSignal.timeout(timeoutMs) });
            if (res.ok) return res;
            const err = new HttpError(res.status, url);
            if (res.status < 500) throw err; // a 4xx will not get better
            lastErr = err;
        } catch (err) {
            if (err instanceof HttpError && err.status < 500) throw err;
            lastErr = err;
        }
        if (attempt < retries) await Bun.sleep(1000 * 2 ** attempt);
    }
    throw lastErr;
}

const getJson = async (url: string) => (await get(url)).json();
const getText = async (url: string) => (await get(url)).text();

// mope.io answers missing files with its HTML page and status 200, so check the bytes.
function isImage(b: Uint8Array): boolean {
    const ascii = (from: number, to: number) => String.fromCharCode(...b.subarray(from, to));
    return (ascii(0, 4) === "RIFF" && ascii(8, 12) === "WEBP")
        || (b[0] === 0x89 && ascii(1, 4) === "PNG")
        || (b[0] === 0xff && b[1] === 0xd8 && b[2] === 0xff)
        || ascii(0, 4) === "GIF8";
}

/** Image bytes, or null when the server has no such image. */
async function getImage(url: string): Promise<Uint8Array | null> {
    try {
        const buf = new Uint8Array(await (await get(url, 30000)).arrayBuffer());
        return isImage(buf) ? buf : null;
    } catch (err) {
        if (err instanceof HttpError && err.status < 500) return null;
        throw err;
    }
}

async function runPool<T>(items: T[], concurrency: number, worker: (item: T) => Promise<void>, label: string) {
    let next = 0, done = 0;
    const tick = () => {
        done++;
        if (process.stdout.isTTY || done === items.length || done % 100 === 0) {
            process.stdout.write(`  ${label} ${done}/${items.length}${process.stdout.isTTY ? "\r" : "\n"}`);
        }
    };
    await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, async () => {
        while (next < items.length) {
            const item = items[next++];
            await worker(item);
            tick();
        }
    }));
    if (process.stdout.isTTY && items.length) process.stdout.write("\n");
}

// --- files on disk ------------------------------------------------------------

function safeDest(root: string, rel: string): string {
    const dest = path.resolve(root, ...rel.split("/"));
    if (!dest.startsWith(path.resolve(root) + path.sep)) throw new Error(`Unsafe path: ${rel}`);
    return dest;
}

async function sizeOnDisk(file: string): Promise<number | null> {
    try {
        return (await stat(file)).size;
    } catch {
        return null;
    }
}

async function save(file: string, data: Uint8Array) {
    await mkdir(path.dirname(file), { recursive: true });
    await Bun.write(file, data);
}

// --- s3.mope.io ---------------------------------------------------------------

type S3Object = { key: string; size: number };

async function listBucket(prefix: string): Promise<S3Object[]> {
    const out: S3Object[] = [];
    let token: string | null = null;
    do {
        const q = new URLSearchParams({ "list-type": "2", prefix, "max-keys": "1000" });
        if (token) q.set("continuation-token", token);
        const xml = await getText(`${S3}/?${q}`);
        for (const m of xml.matchAll(/<Contents>([\s\S]*?)<\/Contents>/g)) {
            const key = m[1].match(/<Key>([^<]*)<\/Key>/)?.[1];
            const size = Number(m[1].match(/<Size>(\d+)<\/Size>/)?.[1] ?? -1);
            // Skip folders, and stray keys stored under a file's name (".../1.webp/<uuid>").
            if (key && !key.endsWith("/") && /\.(webp|png|jpe?g|gif)$/i.test(key)) out.push({ key: decodeXml(key), size });
        }
        token = /<IsTruncated>true<\/IsTruncated>/.test(xml)
            ? decodeXml(xml.match(/<NextContinuationToken>([^<]*)<\/NextContinuationToken>/)?.[1] ?? "") || null
            : null;
    } while (token);
    return out;
}

function decodeXml(s: string): string {
    return s.replace(/&(amp|lt|gt|quot|apos);/g, (_, e) => ({ amp: "&", lt: "<", gt: ">", quot: '"', apos: "'" })[e as string]!);
}

// --- mope.io client -------------------------------------------------------------

/** animal name -> biome folder, read from the animal table in the site's JS bundle. */
async function loadBiomes(): Promise<Map<string, string>> {
    const biomes = new Map<string, string>();
    try {
        const html = await getText(`${SITE}/`);
        const scripts = [...new Set([...html.matchAll(/(?:src|href)="(\.?\/?c\/[^"]+\.js)"/g)].map(m => m[1]))];
        for (const s of scripts) {
            const js = await getText(new URL(s, `${SITE}/`).href);
            // fox:cm(`fox`,{ ... biome:`land`, ... })
            for (const m of js.matchAll(/[A-Za-z_$][\w$]*:[A-Za-z_$][\w$]*\(`(\w+)`,\{/g)) {
                const body = js.slice(m.index! + m[0].length, m.index! + m[0].length + 4000);
                const b = body.match(/(?:^|[,{])biome:`(\w+)`/);
                if (b && BIOMES.includes(b[1]) && !biomes.has(m[1])) biomes.set(m[1], b[1]);
            }
        }
    } catch (err) {
        console.warn(`Could not read the animal biomes from ${SITE}: ${(err as Error).message}`);
    }
    return biomes;
}

// --- main -----------------------------------------------------------------------

type Skin = {
    id: string;
    name: string;
    animal: string | null;
    inShop: boolean;
    rarity: string | null;
    price: number | null;
    currency: string | null;
    salePrice: number | null;
    uiImage: string | null;
    files: string[];
};

async function main() {
    const opts = parseArgs(process.argv.slice(2));
    const outDir = path.resolve(opts.out);
    const s3Root = path.join(outDir, "s3");
    const siteRoot = path.join(outDir, "mope.io");

    console.log("Loading the shop catalog, names and animal list ...");
    const [store, itemNames, animalInfo, biomes] = await Promise.all([
        getJson(`${API}/store`).catch(err => (console.warn(`No shop catalog: ${err.message}`), { catalog: [] })),
        getJson(`${S3}/translations/items/en.json`).catch(() => ({})),
        getJson(`${SITE}/translations/animals/en.json`).catch(() => ({})),
        opts.base ? loadBiomes() : Promise.resolve(new Map<string, string>()),
    ]);
    const catalog = new Map<string, any>();
    for (const p of store.catalog ?? []) if (p.itemId) catalog.set(p.itemId, p);
    const animals = Object.keys(animalInfo).sort((a, b) => b.length - a.length); // longest first, for prefix matching

    console.log(`Listing ${S3}/items/ ...`);
    const objects = await listBucket("items/");
    console.log(`  ${objects.length} files on s3, ${catalog.size} skins in the shop.`);

    // Group s3 files by item folder.
    const folders = new Map<string, S3Object[]>();
    for (const o of objects) {
        const parts = o.key.split("/");
        const folder = parts[1] === "abilities" ? `abilities/${parts[2]}` : parts[1];
        if (!folders.has(folder)) folders.set(folder, []);
        folders.get(folder)!.push(o);
    }

    // The shop says which animal a skin is for; otherwise match the folder name, or use the
    // name of its shop picture (<animal>.ui.webp) for animals the site doesn't list yet.
    const animalOf = (id: string): string | null =>
        catalog.get(id)?.forAnimal
        ?? animals.find(a => id === a || id.startsWith(a + "_"))
        ?? folders.get(id)?.map(o => o.key.match(/^items\/[^/]+\/([^/]+)\.ui\.webp$/)?.[1]).find(Boolean)
        ?? null;

    const skins: Skin[] = [];
    for (const [id, files] of folders) {
        if (id.startsWith("abilities/")) continue;
        const animal = animalOf(id);
        const p = catalog.get(id);
        const ui = animal ? `items/${id}/${animal}.ui.webp` : null;
        skins.push({
            id,
            name: itemNames[id]?.displayName ?? p?.id ?? id,
            animal,
            inShop: !!p,
            rarity: p?.itemRarity ?? null,
            price: p?.basePrice ?? null,
            currency: p?.currency ?? null,
            salePrice: p?.salePrice ?? null,
            uiImage: ui && files.some(f => f.key === ui) ? `s3/${ui}` : null,
            files: files.map(f => `s3/${f.key}`),
        });
    }
    // Shop skins that have no folder on s3 yet.
    for (const [id, p] of catalog) {
        if (folders.has(id)) continue;
        skins.push({ id, name: itemNames[id]?.displayName ?? id, animal: p.forAnimal ?? null, inShop: true,
            rarity: p.itemRarity ?? null, price: p.basePrice ?? null, currency: p.currency ?? null,
            salePrice: p.salePrice ?? null, uiImage: null, files: [] });
    }
    skins.sort((a, b) => a.id.localeCompare(b.id));

    const wanted = (id: string, animal: string | null) =>
        !opts.only || opts.only.has(id) || (animal !== null && opts.only.has(animal));
    const chosen = skins.filter(s => wanted(s.id, s.animal));
    const abilityFolders = [...folders.keys()].filter(f => f.startsWith("abilities/") && (!opts.only || opts.only.has(f.split("/")[1])));

    // mope.io: for every animal that has skins, the plain animal's textures with the same names
    // the skins use (the parts a skin leaves out), and the shop picture of each subspecies.
    const base: { url: string; rel: string; animal: string }[] = [];
    const baseFound = new Map<string, string[]>();
    if (opts.base) {
        const namesByAnimal = new Map<string, Set<string>>();
        for (const s of chosen) {
            if (!s.animal) continue;
            if (!namesByAnimal.has(s.animal)) namesByAnimal.set(s.animal, new Set([`${s.animal}.ui.webp`]));
            for (const f of s.files) {
                const name = f.split("/").slice(3).join("/");
                if (name && !name.includes("/")) namesByAnimal.get(s.animal)!.add(name);
            }
        }
        if (!opts.only) {
            for (const a of Object.keys(animalInfo)) if (!namesByAnimal.has(a)) namesByAnimal.set(a, new Set([`${a}.ui.webp`]));
        }
        for (const [animal, names] of namesByAnimal) {
            const subs = Object.keys(animalInfo[animal]?.subspecies ?? {});
            for (const folder of ["", ...subs.map(s => `${s}/`)]) {
                for (const name of names) {
                    if (folder && name !== `${animal}.ui.webp`) continue; // subspecies: only the shop picture
                    const rel = `assets/animals/{biome}/${animal}/${folder}${name}`;
                    base.push({ url: rel, rel, animal });
                }
            }
        }
        for (const c of ["mopecoins", "gems"]) {
            const rel = `assets/UI/icons/store/currency/${c}.webp`;
            base.push({ url: rel, rel, animal: "" });
        }
    }

    const s3Files = [...chosen.flatMap(s => s.files), ...abilityFolders.flatMap(f => folders.get(f)!.map(o => `s3/${o.key}`))];
    const sizes = new Map(objects.map(o => [`s3/${o.key}`, o.size]));

    let s3Saved = 0, s3Kept = 0, s3Failed = 0, baseSaved = 0, baseKept = 0;
    if (!opts.listOnly) {
        console.log(`Downloading ${s3Files.length} skin files from s3.mope.io ...`);
        await runPool(s3Files, opts.concurrency, async rel => {
            const key = rel.slice(3);
            const dest = safeDest(s3Root, key);
            if ((await sizeOnDisk(dest)) === sizes.get(rel)) return void s3Kept++;
            try {
                const data = await getImage(`${S3}/${key.split("/").map(encodeURIComponent).join("/")}`);
                if (!data) throw new Error("not an image");
                await save(dest, data);
                s3Saved++;
            } catch (err) {
                s3Failed++;
                console.warn(`\n  failed ${key}: ${(err as Error).message}`);
            }
        }, "s3");

        if (base.length) {
            // Find each animal's folder once with its shop picture: the biome from the site's
            // animal table first, then the others.
            const animalsToResolve = [...new Set(base.map(b => b.animal).filter(Boolean))];
            const folderOf = new Map<string, string>();
            console.log(`Finding ${animalsToResolve.length} animal folders on mope.io ...`);
            await runPool(animalsToResolve, opts.concurrency, async animal => {
                const tries = [...new Set([biomes.get(animal), ...BIOMES].filter(Boolean))] as string[];
                for (const biome of tries) {
                    const rel = `assets/animals/${biome}/${animal}/${animal}.ui.webp`;
                    if ((await sizeOnDisk(safeDest(siteRoot, rel))) !== null
                        || (await getImage(`${SITE}/${rel}`).catch(() => null))) {
                        folderOf.set(animal, biome);
                        return;
                    }
                }
            }, "animals");

            const todo = base
                .filter(b => !b.animal || folderOf.has(b.animal))
                .map(b => ({ ...b, rel: b.rel.replace("{biome}", folderOf.get(b.animal) ?? "") }));
            console.log(`Downloading up to ${todo.length} animal textures from mope.io ...`);
            await runPool(todo, opts.concurrency, async b => {
                const dest = safeDest(siteRoot, b.rel);
                let ok = (await sizeOnDisk(dest)) !== null;
                if (ok) baseKept++;
                else {
                    const data = await getImage(`${SITE}/${b.rel}`).catch(() => null);
                    if (data) {
                        await save(dest, data);
                        baseSaved++;
                        ok = true;
                    }
                }
                if (ok) {
                    if (!baseFound.has(b.animal)) baseFound.set(b.animal, []);
                    baseFound.get(b.animal)!.push(`mope.io/${b.rel}`);
                }
            }, "mope.io");
        }
    }

    const report = {
        fetchedAt: new Date().toISOString(),
        sources: { s3: S3, site: SITE, catalog: `${API}/store` },
        count: chosen.length,
        inShop: chosen.filter(s => s.inShop).length,
        skins: chosen,
        abilities: Object.fromEntries(abilityFolders.map(f => [f.split("/")[1], folders.get(f)!.map(o => `s3/${o.key}`)])),
        animals: Object.fromEntries([...baseFound].filter(([a]) => a).sort().map(([a, files]) => [a, {
            name: animalInfo[a]?.name ?? a,
            biome: biomes.get(a) ?? files[0]?.split("/")[3] ?? null,
            subspecies: Object.keys(animalInfo[a]?.subspecies ?? {}),
            files: files.sort(),
        }])),
    };
    await mkdir(outDir, { recursive: true });
    await Bun.write(path.join(outDir, "skins.json"), JSON.stringify(report, null, 2));

    if (opts.listOnly) {
        console.log(`Listed ${chosen.length} skins (${s3Files.length} s3 files). Wrote ${path.join(opts.out, "skins.json")}`);
    } else {
        console.log(`s3.mope.io: ${s3Saved} downloaded, ${s3Kept} already here, ${s3Failed} failed.`);
        if (opts.base) console.log(`mope.io: ${baseSaved} downloaded, ${baseKept} already here.`);
        console.log(`Wrote ${path.join(opts.out, "skins.json")} (${chosen.length} skins, ${report.inShop} in the shop).`);
    }
    if (s3Failed) process.exitCode = 1;
}

main().catch(err => {
    console.error((err as Error).message ?? err);
    process.exit(1);
});
