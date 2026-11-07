// Renders the space-art SVGs to transparent PNGs.
// Usage: node tools/space-art/render.js SVG_DIR OUT_DIR [size]
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

(async () => {
    const [svgDir, outDir, sizeArg] = process.argv.slice(2);
    const size = Number(sizeArg) || 512;
    fs.mkdirSync(outDir, { recursive: true });
    const browser = await chromium.launch();
    const page = await browser.newPage({ viewport: { width: size, height: size } });
    for (const file of fs.readdirSync(svgDir).filter(f => f.endsWith('.svg')).sort()) {
        const svg = fs.readFileSync(path.join(svgDir, file), 'utf8');
        await page.setContent(`<html><body style="margin:0;background:transparent">` +
            `<img src="data:image/svg+xml;base64,${Buffer.from(svg).toString('base64')}" width="${size}" height="${size}" style="display:block"></body></html>`);
        await page.waitForFunction(() => document.images[0].complete);
        await page.screenshot({ path: path.join(outDir, file.replace(/\.svg$/, '.png')), omitBackground: true, clip: { x: 0, y: 0, width: size, height: size } });
    }
    await browser.close();
    console.log(`rendered to ${outDir}`);
})();
