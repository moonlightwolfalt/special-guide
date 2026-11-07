#!/usr/bin/env node
// Downloads every mope.io fire frame from s3.mope.io.
//
// Fireballs (dragons, phoenixes, lava monsters, ...) are drawn from two animated
// frame sets per style, at items/abilities/fireball/<style>/dynamic/<n>.webp (the
// moving fireball) and .../static/<n>.webp. "default" is the plain fireball; every
// other style belongs to a skin. The phoenix's fire tornado has one image per style
// at items/abilities/fire_tornado/<style>.webp.
//
// The bucket only allows listing the items/ prefix, so the script lists that and
// keeps the fire files; no names are guessed. Files already on disk with the same
// size are skipped, so running it again only fetches new frames.
//
// Usage:
//   node tools/fetch-fire-frames.js [--out DIR] [--concurrency N] [--no-tornado]
//
// Output:
//   DIR/fireball/<style>/dynamic/<n>.webp
//   DIR/fireball/<style>/static/<n>.webp
//   DIR/fire_tornado/<style>.webp
//   DIR/fire-frames.json   every style and its frames, in frame order

'use strict';

const fs = require('fs');
const path = require('path');

const S3 = 'https://s3.mope.io/mope-prod';
const FIREBALL = 'items/abilities/fireball/';
const TORNADO = 'items/abilities/fire_tornado/';

function parseArgs(argv) {
    const opts = { out: 'fire-frames', concurrency: 12, tornado: true };
    for (let i = 0; i < argv.length; i++) {
        const a = argv[i];
        if (a === '--out') opts.out = argv[++i];
        else if (a === '--concurrency') opts.concurrency = Math.max(1, parseInt(argv[++i], 10) || 1);
        else if (a === '--no-tornado') opts.tornado = false;
        else if (a === '-h' || a === '--help') {
            console.log('Usage: node tools/fetch-fire-frames.js [--out DIR] [--concurrency N] [--no-tornado]');
            process.exit(0);
        } else {
            console.error(`Unknown argument: ${a}`);
            process.exit(1);
        }
    }
    return opts;
}

async function get(url, { timeoutMs = 60000, retries = 3 } = {}) {
    let lastErr;
    for (let attempt = 0; attempt <= retries; attempt++) {
        try {
            const res = await fetch(url, { signal: AbortSignal.timeout(timeoutMs) });
            if (res.ok) return res;
            lastErr = Object.assign(new Error(`HTTP ${res.status} for ${url}`), { status: res.status });
            if (res.status < 500) break; // a 4xx will not get better
        } catch (err) {
            lastErr = err;
        }
        if (attempt < retries) await new Promise(r => setTimeout(r, 1000 * 2 ** attempt));
    }
    throw lastErr;
}

const decodeXml = s => s.replace(/&(amp|lt|gt|quot|apos);/g, (_, e) => ({ amp: '&', lt: '<', gt: '>', quot: '"', apos: "'" })[e]);

async function listItems() {
    const out = [];
    let token = null;
    do {
        const q = new URLSearchParams({ 'list-type': '2', prefix: 'items/', 'max-keys': '1000' });
        if (token) q.set('continuation-token', token);
        const xml = await (await get(`${S3}/?${q}`)).text();
        for (const m of xml.matchAll(/<Contents>([\s\S]*?)<\/Contents>/g)) {
            const key = m[1].match(/<Key>([^<]*)<\/Key>/);
            const size = m[1].match(/<Size>(\d+)<\/Size>/);
            if (key) out.push({ key: decodeXml(key[1]), size: size ? Number(size[1]) : -1 });
        }
        token = /<IsTruncated>true<\/IsTruncated>/.test(xml)
            ? decodeXml((xml.match(/<NextContinuationToken>([^<]*)<\/NextContinuationToken>/) || [])[1] || '') || null
            : null;
    } while (token);
    return out;
}

function isWebp(buf) {
    return buf.length > 12 && buf.toString('ascii', 0, 4) === 'RIFF' && buf.toString('ascii', 8, 12) === 'WEBP';
}

async function runPool(items, concurrency, worker) {
    let next = 0, done = 0;
    await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, async () => {
        while (next < items.length) {
            await worker(items[next++]);
            done++;
            if (done % 50 === 0 || done === items.length) process.stdout.write(`  ${done}/${items.length}\r`);
        }
    }));
    if (items.length) process.stdout.write('\n');
}

async function main() {
    const opts = parseArgs(process.argv.slice(2));
    const outDir = path.resolve(opts.out);

    console.log(`Listing ${S3}/items/ ...`);
    const all = await listItems();
    // fireball/<style>/<dynamic|static>/<n>.webp and fire_tornado/<style>.webp; this also
    // leaves out stray keys stored under a file's name (".../1.webp/<uuid>").
    const frames = all.filter(o =>
        /^items\/abilities\/fireball\/[^/]+\/(dynamic|static)\/\d+\.webp$/.test(o.key)
        || (opts.tornado && /^items\/abilities\/fire_tornado\/[^/]+\.webp$/.test(o.key)));
    console.log(`  ${frames.length} fire frames among ${all.length} files.`);

    let saved = 0, kept = 0, failed = 0;
    await runPool(frames, opts.concurrency, async o => {
        const rel = o.key.slice('items/abilities/'.length);
        const dest = path.join(outDir, ...rel.split('/'));
        if (!path.resolve(dest).startsWith(outDir + path.sep)) return void failed++;
        try {
            if (fs.existsSync(dest) && fs.statSync(dest).size === o.size) return void kept++;
            const buf = Buffer.from(await (await get(`${S3}/${o.key.split('/').map(encodeURIComponent).join('/')}`)).arrayBuffer());
            if (!isWebp(buf)) throw new Error('not a WebP image');
            fs.mkdirSync(path.dirname(dest), { recursive: true });
            fs.writeFileSync(dest, buf);
            saved++;
        } catch (err) {
            failed++;
            console.warn(`\n  failed ${o.key}: ${err.message}`);
        }
    });

    // Index: every style with its frames in numeric order.
    const fireball = {}, tornado = {};
    for (const o of frames) {
        let m;
        if ((m = o.key.match(/^items\/abilities\/fireball\/([^/]+)\/(dynamic|static)\/(\d+)\.webp$/))) {
            const style = (fireball[m[1]] ||= { dynamic: [], static: [] });
            style[m[2]].push(Number(m[3]));
        } else if ((m = o.key.match(/^items\/abilities\/fire_tornado\/([^/]+)\.webp$/))) {
            tornado[m[1]] = { file: `fire_tornado/${m[1]}.webp`, url: `${S3}/${o.key}` };
        }
    }
    const styles = Object.keys(fireball).sort().map(name => {
        const s = fireball[name];
        const list = kind => s[kind].sort((a, b) => a - b).map(n => ({
            frame: n, file: `fireball/${name}/${kind}/${n}.webp`, url: `${S3}/${FIREBALL}${name}/${kind}/${n}.webp`,
        }));
        return { style: name, dynamic: list('dynamic'), static: list('static') };
    });
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(path.join(outDir, 'fire-frames.json'), JSON.stringify({
        source: S3, fetchedAt: new Date().toISOString(),
        fireball: styles,
        fireTornado: Object.keys(tornado).sort().map(k => ({ style: k, ...tornado[k] })),
    }, null, 2));

    console.log(`${saved} downloaded, ${kept} already here, ${failed} failed: ${styles.length} fireball styles`
        + (opts.tornado ? `, ${Object.keys(tornado).length} fire tornadoes` : '') + `. Wrote ${path.join(opts.out, 'fire-frames.json')}`);
    if (failed) process.exitCode = 1;
}

main().catch(err => {
    console.error(err.message || err);
    process.exit(1);
});
