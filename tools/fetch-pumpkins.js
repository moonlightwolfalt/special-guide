#!/usr/bin/env node
// Downloads the mope.io pumpkin textures.
//
// The mope.io client draws each pumpkin from 5 frames at
// assets/terrain/pumpkin/<kind>/<0-4>.webp. Those files are served from
// mope.io itself; s3.mope.io only holds item and translation files and
// refuses everything else.
//
// Usage:
//   node tools/fetch-pumpkins.js [--site URL] [--out DIR]
//
// Output:
//   DIR/<kind>/<frame>.webp and DIR/pumpkins.json

'use strict';

const fs = require('fs');
const path = require('path');

// Same order as the subspecies numbers the game server sends.
const KINDS = ['normal', 'jack', 'gold', 'emerald', 'ruby', 'diamond', 'onyx'];
const FRAMES = 5;

function parseArgs(argv) {
    const opts = { site: 'https://mope.io', out: 'pumpkins' };
    for (let i = 0; i < argv.length; i++) {
        const a = argv[i];
        if (a === '--site') opts.site = argv[++i];
        else if (a === '--out') opts.out = argv[++i];
        else if (a === '-h' || a === '--help') {
            console.log('Usage: node tools/fetch-pumpkins.js [--site URL] [--out DIR]');
            process.exit(0);
        } else {
            console.error(`Unknown argument: ${a}`);
            process.exit(1);
        }
    }
    opts.site = opts.site.replace(/\/+$/, '');
    return opts;
}

async function download(url, dest) {
    for (let attempt = 0; ; attempt++) {
        try {
            const res = await fetch(url, { signal: AbortSignal.timeout(30000) });
            if (!res.ok) throw Object.assign(new Error(`HTTP ${res.status}`), { final: res.status < 500 });
            const buf = Buffer.from(await res.arrayBuffer());
            fs.mkdirSync(path.dirname(dest), { recursive: true });
            fs.writeFileSync(dest, buf);
            return buf.length;
        } catch (err) {
            if (err.final || attempt >= 3) throw err;
            await new Promise(r => setTimeout(r, 1000 * 2 ** attempt));
        }
    }
}

async function main() {
    const opts = parseArgs(process.argv.slice(2));
    const list = [];
    let failed = 0;
    await Promise.all(KINDS.map(async (kind, subspecies) => {
        const frames = [];
        for (let n = 0; n < FRAMES; n++) {
            const rel = `${kind}/${n}.webp`;
            const url = `${opts.site}/assets/terrain/pumpkin/${rel}`;
            try {
                await download(url, path.join(opts.out, rel));
                frames.push({ frame: n, url, file: rel });
            } catch (err) {
                failed++;
                console.warn(`  failed ${url}: ${err.message}`);
            }
        }
        list[subspecies] = { kind, subspecies, frames };
    }));
    fs.writeFileSync(path.join(opts.out, 'pumpkins.json'),
        JSON.stringify({ site: opts.site, fetchedAt: new Date().toISOString(), pumpkins: list }, null, 2));
    const got = list.reduce((n, p) => n + p.frames.length, 0);
    console.log(`Downloaded ${got} pumpkin frames (${KINDS.length} kinds) to ${opts.out}/`);
    if (failed) {
        console.warn(`${failed} frame(s) failed.`);
        process.exit(1);
    }
}

main().catch(err => {
    console.error(err.message || err);
    process.exit(1);
});
