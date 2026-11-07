#!/usr/bin/env node
// Fetches the shop skin list (item id, display name, image) from a Mope site
// and downloads every skin image.
//
// The skin names and image paths live in the site's client.js, inside
// getAnimalInfo(itemName). Prices and categories come from the account
// server's /loadShopItems, which needs a logged-in user, so they are only
// added when MOPE_USER_ID and MOPE_PASSWORD_TOKEN are set.
//
// Usage:
//   node tools/fetch-shop-skins.js [--site URL] [--out DIR] [--no-images] [--concurrency N]
//
// Output:
//   DIR/skins.json        list of skins
//   DIR/images/<path>.png the skin images, same layout as the site's skins/ folder

'use strict';

const fs = require('fs');
const path = require('path');

function parseArgs(argv) {
    const opts = {
        site: 'https://mope.trygames.party',
        out: 'shop-skins',
        images: true,
        concurrency: 8,
    };
    for (let i = 0; i < argv.length; i++) {
        const a = argv[i];
        if (a === '--site') opts.site = argv[++i];
        else if (a === '--out') opts.out = argv[++i];
        else if (a === '--no-images') opts.images = false;
        else if (a === '--concurrency') opts.concurrency = Math.max(1, parseInt(argv[++i], 10) || 1);
        else if (a === '-h' || a === '--help') {
            console.log('Usage: node tools/fetch-shop-skins.js [--site URL] [--out DIR] [--no-images] [--concurrency N]');
            process.exit(0);
        } else {
            console.error(`Unknown argument: ${a}`);
            process.exit(1);
        }
    }
    opts.site = opts.site.replace(/\/+$/, '');
    return opts;
}

async function fetchWithRetry(url, { timeoutMs = 60000, retries = 3 } = {}) {
    let lastErr;
    for (let attempt = 0; attempt <= retries; attempt++) {
        try {
            const res = await fetch(url, { signal: AbortSignal.timeout(timeoutMs) });
            if (!res.ok) {
                const err = new Error(`HTTP ${res.status} for ${url}`);
                err.status = res.status;
                // 4xx will not get better on retry
                if (res.status >= 400 && res.status < 500) throw Object.assign(err, { final: true });
                throw err;
            }
            return res;
        } catch (err) {
            lastErr = err;
            if (err.final || attempt === retries) break;
            await new Promise(r => setTimeout(r, 1000 * 2 ** attempt));
        }
    }
    throw lastErr;
}

// Decode a JS string literal body ('...', "..." or `...` without ${}).
function decodeJsString(body) {
    return body.replace(/\\(x[0-9a-fA-F]{2}|u\{[0-9a-fA-F]+\}|u[0-9a-fA-F]{4}|[\s\S])/g, (_, e) => {
        if (e[0] === 'x') return String.fromCharCode(parseInt(e.slice(1), 16));
        if (e[0] === 'u' && e.length > 1) {
            const hex = e[1] === '{' ? e.slice(2, -1) : e.slice(1);
            return String.fromCodePoint(parseInt(hex, 16));
        }
        switch (e) {
            case 'n': return '\n';
            case 'r': return '\r';
            case 't': return '\t';
            case 'b': return '\b';
            case 'f': return '\f';
            case 'v': return '\v';
            case '0': return '\0';
            case '\n': return '';
            default: return e;
        }
    });
}

const STR = String.raw`(?:'(?:[^'\\]|\\[\s\S])*'|"(?:[^"\\]|\\[\s\S])*"|` + '`(?:[^`\\\\$]|\\\\[\\s\\S])*`)';
const NUM = String.raw`(?:0x[0-9a-fA-F]+|\d+)`;
// case 'gold_kraken': return [a_kraken, 0x1, 'Golden\nKraken', 'ocean/kraken/1/kraken'];
// An optional 5th element marks the skin as new in the shop.
const CASE_RE = new RegExp(
    String.raw`case\s*(${STR})\s*:\s*return\s*\[\s*([A-Za-z_$][\w$]*)\s*,\s*(${NUM})\s*,\s*(${STR})\s*,\s*(${STR})\s*(?:,\s*(!0x0|!0x1|!0|!1|true|false)\s*)?\]`,
    'g'
);

function literal(s) {
    return decodeJsString(s.slice(1, -1));
}

function parseSkins(clientJs) {
    const skins = new Map();
    for (const m of clientJs.matchAll(CASE_RE)) {
        const id = literal(m[1]);
        if (skins.has(id)) continue;
        const animalVar = m[2];
        const name = literal(m[4]);
        const image = literal(m[5]);
        const underscore = id.indexOf('_');
        skins.set(id, {
            id,
            name: name.replace(/\s*\n\s*/g, ' ').trim(),
            displayName: name,
            // Readable builds name the animal constant a_<animal>; obfuscated ones do not.
            animal: /^a_/.test(animalVar) ? animalVar.slice(2) : null,
            species: Number(m[3]),
            group: underscore > 0 ? id.slice(0, underscore) : null,
            image: `skins/${image}.png`,
            isNew: m[6] === '!0x0' || m[6] === '!0' || m[6] === 'true',
        });
    }
    return [...skins.values()];
}

async function loadShopItems(accountUrl) {
    const userId = process.env.MOPE_USER_ID;
    const token = process.env.MOPE_PASSWORD_TOKEN;
    if (!userId || !token) return null;
    const url = `${accountUrl}/loadShopItems?userId=${encodeURIComponent(userId)}&passwordToken=${encodeURIComponent(token)}`;
    try {
        const res = await fetchWithRetry(url, { timeoutMs: 30000, retries: 1 });
        const data = JSON.parse(await res.text());
        if (!Array.isArray(data)) throw new Error('response is not a list (bad login?)');
        return data;
    } catch (err) {
        console.warn(`Could not load shop prices from ${accountUrl}: ${err.message}`);
        return null;
    }
}

async function findAccountUrl(site) {
    try {
        const res = await fetchWithRetry(`${site}/deployment-config.js`, { timeoutMs: 20000, retries: 1 });
        const m = (await res.text()).match(/"account"\s*:\s*"([^"]+)"/);
        if (m) return m[1].replace(/\/+$/, '');
    } catch {}
    return null;
}

async function runPool(items, concurrency, worker) {
    let next = 0;
    const runners = Array.from({ length: Math.min(concurrency, items.length) }, async () => {
        while (next < items.length) {
            const i = next++;
            await worker(items[i], i);
        }
    });
    await Promise.all(runners);
}

async function downloadImages(site, skins, outDir, concurrency) {
    const unique = [...new Set(skins.map(s => s.image))];
    const failed = new Set();
    let done = 0;
    await runPool(unique, concurrency, async rel => {
        const dest = path.join(outDir, 'images', rel.replace(/^skins\//, ''));
        // Guard against path tricks in data read from the site.
        if (!path.resolve(dest).startsWith(path.resolve(outDir, 'images') + path.sep)) {
            failed.add(rel);
            return;
        }
        try {
            if (!fs.existsSync(dest)) {
                const res = await fetchWithRetry(`${site}/${rel.split('/').map(encodeURIComponent).join('/')}`);
                const buf = Buffer.from(await res.arrayBuffer());
                fs.mkdirSync(path.dirname(dest), { recursive: true });
                fs.writeFileSync(dest, buf);
            }
        } catch (err) {
            failed.add(rel);
            console.warn(`  failed ${rel}: ${err.message}`);
        }
        done++;
        if (done % 25 === 0 || done === unique.length) {
            process.stdout.write(`  images ${done}/${unique.length}\r`);
        }
    });
    process.stdout.write('\n');
    for (const s of skins) {
        s.imageUrl = `${site}/${s.image}`;
        s.localImage = failed.has(s.image) ? null : path.posix.join('images', s.image.replace(/^skins\//, ''));
    }
    return failed.size;
}

async function main() {
    const opts = parseArgs(process.argv.slice(2));
    console.log(`Fetching ${opts.site}/client.js ...`);
    const clientJs = await (await fetchWithRetry(`${opts.site}/client.js`, { timeoutMs: 120000 })).text();

    const skins = parseSkins(clientJs);
    if (!skins.length) throw new Error('No shop skins found in client.js; the format may have changed.');
    console.log(`Found ${skins.length} shop skins.`);

    const accountUrl = await findAccountUrl(opts.site);
    const shopItems = accountUrl ? await loadShopItems(accountUrl) : null;
    if (shopItems) {
        const byName = new Map(shopItems.map(i => [i.itemName, i]));
        for (const s of skins) {
            const item = byName.get(s.id);
            s.inShop = !!item;
            s.category = item ? item.shopCategory ?? null : null;
            s.price = item ? item.coinsPrice ?? null : null;
            s.owned = item ? !!item.purchasedByPlayer : false;
        }
        console.log(`Added prices for ${shopItems.length} shop items.`);
    } else if (!process.env.MOPE_USER_ID) {
        console.log('Skipping prices (set MOPE_USER_ID and MOPE_PASSWORD_TOKEN to include them).');
    }

    fs.mkdirSync(opts.out, { recursive: true });

    if (opts.images) {
        console.log('Downloading images ...');
        const failed = await downloadImages(opts.site, skins, opts.out, opts.concurrency);
        if (failed) console.warn(`${failed} image(s) could not be downloaded.`);
    } else {
        for (const s of skins) s.imageUrl = `${opts.site}/${s.image}`;
    }

    const outFile = path.join(opts.out, 'skins.json');
    fs.writeFileSync(outFile, JSON.stringify({ site: opts.site, fetchedAt: new Date().toISOString(), count: skins.length, skins }, null, 2));
    console.log(`Wrote ${outFile}`);
}

main().catch(err => {
    console.error(err.message || err);
    process.exit(1);
});
