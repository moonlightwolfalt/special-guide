// Draws the milestone badge images: one per milestone, as transparent PNGs.
//
//   Black Dragon  blackdragon_1..3   (fire volley milestones, 12.5m / 17.5m / 25m XP)
//   King Dragon   kingdragon_1..3    (fire stream + tail slap milestones, same XP)
//   5m tier       dinomonster_1, landmonster_1, seamonster_1, giantscorpion_1, icemonster_1 (7.25m XP)
//
// The game server names these in Milestones.image() and the client shows them in the
// milestone popup and the skill tree panel.
//
// Usage: node tools/milestones/make-milestone-images.js [OUT_DIR] [SIZE]
//        (defaults: web/img/milestones, 256)
'use strict';
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const S = 512; // drawing size; the PNG is scaled to SIZE
const C = S / 2;

// --- colour helpers --------------------------------------------------------------
function shade(hex, f) {
    const h = hex.replace('#', '');
    let [r, g, b] = [0, 2, 4].map(i => parseInt(h.slice(i, i + 2), 16));
    if (f < 1) [r, g, b] = [r, g, b].map(v => Math.round(v * f));
    else [r, g, b] = [r, g, b].map(v => Math.round(v + (255 - v) * (f - 1)));
    return '#' + [r, g, b].map(v => v.toString(16).padStart(2, '0')).join('');
}

const ROMAN = ['', 'I', 'II', 'III'];

// --- shared pieces -----------------------------------------------------------------
function star(cx, cy, r, fill) {
    let d = '';
    for (let i = 0; i < 10; i++) {
        const a = -Math.PI / 2 + i * Math.PI / 5;
        const rr = i % 2 ? r * 0.45 : r;
        d += (i ? 'L' : 'M') + (cx + Math.cos(a) * rr).toFixed(1) + ' ' + (cy + Math.sin(a) * rr).toFixed(1);
    }
    return `<path d="${d}Z" fill="${fill}" stroke="${shade(fill, 0.55)}" stroke-width="4" stroke-linejoin="round"/>`;
}

// a teardrop flame pointing up, centred on (cx, cy)
function flame(cx, cy, h, outer, inner) {
    const w = h * 0.62;
    const d = (s, col) => {
        const hh = h * s, ww = w * s, y0 = cy + h * 0.5 - hh * 0.5;
        return `<path d="M${cx} ${y0 - hh * 0.5} C ${cx + ww * 0.15} ${y0 - hh * 0.15}, ${cx + ww * 0.62} ${y0 + hh * 0.02}, ${cx + ww * 0.5} ${y0 + hh * 0.26}
            C ${cx + ww * 0.42} ${y0 + hh * 0.5}, ${cx - ww * 0.42} ${y0 + hh * 0.5}, ${cx - ww * 0.5} ${y0 + hh * 0.26}
            C ${cx - ww * 0.6} ${y0 + hh * 0.02}, ${cx - ww * 0.32} ${y0 - hh * 0.06}, ${cx} ${y0 - hh * 0.5} Z" fill="${col}"/>`;
    };
    return `<g stroke="${shade(outer, 0.55)}" stroke-width="5" stroke-linejoin="round">${d(1, outer)}</g>${d(0.62, inner)}`;
}

function fireball(cx, cy, r, angleDeg, col, core) {
    return `<g transform="rotate(${angleDeg} ${cx} ${cy})">
        <path d="M${cx - r * 2.6} ${cy} Q ${cx - r * 1.2} ${cy - r * 1.05} ${cx} ${cy - r} A ${r} ${r} 0 0 1 ${cx} ${cy + r} Q ${cx - r * 1.2} ${cy + r * 1.05} ${cx - r * 2.6} ${cy} Z"
              fill="${col}" stroke="${shade(col, 0.55)}" stroke-width="4" stroke-linejoin="round"/>
        <circle cx="${cx + r * 0.05}" cy="${cy}" r="${r * 0.55}" fill="${core}"/>
    </g>`;
}

/**
 * The medallion every badge shares: a scalloped rim in the track colour, a dark face,
 * one star per level over the top and a ribbon with the level numeral.
 */
function medallion(o, emblem) {
    const { rim, face, glow, level, maxLevel, label } = o;
    const id = o.id;
    // scalloped rim
    let rimPath = '';
    const n = 24;
    for (let i = 0; i <= n * 2; i++) {
        const a = (i / (n * 2)) * Math.PI * 2;
        const r = i % 2 ? 214 : 232;
        rimPath += (i ? 'L' : 'M') + (C + Math.cos(a) * r).toFixed(1) + ' ' + (C + 10 + Math.sin(a) * r).toFixed(1);
    }
    const stars = [];
    for (let i = 0; i < maxLevel; i++) {
        const spread = maxLevel === 1 ? 0 : 34;
        const a = (-90 + (i - (maxLevel - 1) / 2) * spread) * Math.PI / 180;
        const lit = i < level;
        stars.push(star(C + Math.cos(a) * 222, C + 10 + Math.sin(a) * 222, 30, lit ? '#ffd23f' : '#5d5a6b'));
    }
    return `<svg xmlns="http://www.w3.org/2000/svg" width="${S}" height="${S}" viewBox="0 0 ${S} ${S}">
      <defs>
        <radialGradient id="${id}f" cx="42%" cy="36%" r="70%">
          <stop offset="0" stop-color="${shade(face, 1.35)}"/>
          <stop offset="1" stop-color="${shade(face, 0.7)}"/>
        </radialGradient>
        <radialGradient id="${id}g" cx="50%" cy="50%" r="50%">
          <stop offset="0.55" stop-color="${glow}" stop-opacity="0.55"/>
          <stop offset="1" stop-color="${glow}" stop-opacity="0"/>
        </radialGradient>
        <clipPath id="${id}c"><circle cx="${C}" cy="${C + 10}" r="178"/></clipPath>
      </defs>
      <circle cx="${C}" cy="${C + 10}" r="250" fill="url(#${id}g)"/>
      <path d="${rimPath}Z" fill="${rim}" stroke="${shade(rim, 0.5)}" stroke-width="8" stroke-linejoin="round"/>
      <circle cx="${C}" cy="${C + 10}" r="194" fill="${shade(rim, 1.3)}" stroke="${shade(rim, 0.5)}" stroke-width="6"/>
      <circle cx="${C}" cy="${C + 10}" r="178" fill="url(#${id}f)" stroke="${shade(face, 0.45)}" stroke-width="6"/>
      <g clip-path="url(#${id}c)">
        <ellipse cx="${C - 60}" cy="${C - 80}" rx="120" ry="60" fill="#ffffff" opacity="0.08" transform="rotate(-25 ${C - 60} ${C - 80})"/>
        ${emblem}
      </g>
      ${stars.join('')}
      <g>
        <path d="M${C - 150} ${C + 168} L${C + 150} ${C + 168} L${C + 132} ${C + 204} L${C + 150} ${C + 240} L${C - 150} ${C + 240} L${C - 132} ${C + 204} Z"
              fill="${shade(rim, 0.85)}" stroke="${shade(rim, 0.45)}" stroke-width="6" stroke-linejoin="round"/>
        <text x="${C}" y="${C + 218}" text-anchor="middle" font-family="Verdana, 'DejaVu Sans', sans-serif" font-weight="bold"
              font-size="${label.length > 9 ? 30 : 36}" fill="#ffffff" stroke="${shade(rim, 0.35)}" stroke-width="6" paint-order="stroke">${label}</text>
      </g>
    </svg>`;
}

// --- emblems -------------------------------------------------------------------------
const Y = C + 10; // face centre

function blackDragonEmblem(level) {
    // a fan of violet fireballs, one more per level, flying up out of a dark maw
    const count = 2 + level;
    const parts = [];
    parts.push(`<path d="M${C - 110} ${Y + 150} Q ${C} ${Y + 40} ${C + 110} ${Y + 150} Z" fill="#1d1426" stroke="#0b0710" stroke-width="6"/>`);
    parts.push(`<path d="M${C - 70} ${Y + 120} l12 -26 l12 26 M${C + 46} ${Y + 120} l12 -26 l12 26" fill="#f2ecff" stroke="#0b0710" stroke-width="4" stroke-linejoin="round"/>`);
    for (let i = 0; i < count; i++) {
        const t = count === 1 ? 0 : -1 + 2 * i / (count - 1);
        const ang = -90 + t * (34 + level * 6);
        const dist = 120 - Math.abs(t) * 10;
        const x = C + Math.cos(ang * Math.PI / 180) * dist;
        const y = Y + 60 + Math.sin(ang * Math.PI / 180) * dist;
        parts.push(fireball(x, y, 22 + level, ang, '#9b4dff', '#f3d9ff'));
    }
    if (level >= 3) parts.push(flame(C, Y - 120, 70, '#7b2cff', '#e8c8ff'));
    return parts.join('');
}

function kingDragonEmblem(level) {
    // a crown over a fire stream, with a curled tail sweeping under it
    const parts = [];
    // tail
    const tw = 18 + level * 4;
    parts.push(`<path d="M${C - 150} ${Y + 110} C ${C - 90} ${Y + 170}, ${C + 70} ${Y + 170}, ${C + 120} ${Y + 90} C ${C + 140} ${Y + 60}, ${C + 110} ${Y + 40}, ${C + 92} ${Y + 62}"
        fill="none" stroke="#7a1010" stroke-width="${tw + 10}" stroke-linecap="round"/>
        <path d="M${C - 150} ${Y + 110} C ${C - 90} ${Y + 170}, ${C + 70} ${Y + 170}, ${C + 120} ${Y + 90} C ${C + 140} ${Y + 60}, ${C + 110} ${Y + 40}, ${C + 92} ${Y + 62}"
        fill="none" stroke="#d63a2a" stroke-width="${tw}" stroke-linecap="round"/>
        <path d="M${C + 80} ${Y + 44} l 34 4 l -16 30 Z" fill="#ffcf3f" stroke="#7a1010" stroke-width="5" stroke-linejoin="round"/>`);
    // fire stream: one row of fireballs per level
    for (let row = 0; row < level; row++) {
        for (let i = 0; i < 3; i++) {
            const x = C - 50 + i * 50;
            const y = Y + 30 - row * 46 - (i === 1 ? 10 : 0);
            parts.push(fireball(x, y, 20, -90, '#ff7a1a', '#fff1a8'));
        }
    }
    // crown
    const cy = Y - 112 + (3 - level) * 12;
    parts.push(`<path d="M${C - 80} ${cy + 46} L${C - 92} ${cy - 18} L${C - 44} ${cy + 14} L${C} ${cy - 36} L${C + 44} ${cy + 14} L${C + 92} ${cy - 18} L${C + 80} ${cy + 46} Z"
        fill="#ffcf3f" stroke="#8a5a00" stroke-width="7" stroke-linejoin="round"/>
        <circle cx="${C}" cy="${cy + 22}" r="11" fill="#e8312a" stroke="#8a1010" stroke-width="4"/>
        <circle cx="${C - 46}" cy="${cy + 28}" r="8" fill="#3fa4ff" stroke="#164c8a" stroke-width="3"/>
        <circle cx="${C + 46}" cy="${cy + 28}" r="8" fill="#3fa4ff" stroke="#164c8a" stroke-width="3"/>`);
    return parts.join('');
}

function footprint() { // dino monster: a big three-toed print
    const toe = (ang, len) => {
        const a = ang * Math.PI / 180;
        const x = C + Math.cos(a) * len, y = Y + 20 + Math.sin(a) * len;
        return `<ellipse cx="${x}" cy="${y}" rx="30" ry="58" transform="rotate(${ang + 90} ${x} ${y})" fill="#3b5d2a" stroke="#1d2f14" stroke-width="6"/>
                <path d="M${x + Math.cos(a) * 52} ${y + Math.sin(a) * 52} l ${Math.cos(a) * 28} ${Math.sin(a) * 28}" stroke="#f4ecd2" stroke-width="12" stroke-linecap="round"/>`;
    };
    return `${toe(-130, 92)}${toe(-90, 104)}${toe(-50, 92)}
        <ellipse cx="${C}" cy="${Y + 70}" rx="78" ry="64" fill="#3b5d2a" stroke="#1d2f14" stroke-width="6"/>`;
}

function clawMarks() { // land monster: three torn claw slashes through rock
    const parts = [`<circle cx="${C}" cy="${Y + 10}" r="140" fill="#8d6a4a" stroke="#4d3624" stroke-width="6"/>`];
    for (let i = -1; i <= 1; i++) {
        const x = C + i * 62;
        parts.push(`<path d="M${x - 40} ${Y - 100} Q ${x + 22} ${Y} ${x + 30} ${Y + 110} Q ${x - 18} ${Y + 10} ${x - 40} ${Y - 100} Z"
            fill="#2a1a10" stroke="#1a0e06" stroke-width="5" stroke-linejoin="round"/>`);
    }
    parts.push(`<path d="M${C - 120} ${Y - 30} l 30 -20 M${C + 110} ${Y + 50} l 26 8" stroke="#c7a07a" stroke-width="10" stroke-linecap="round"/>`);
    return parts.join('');
}

function seaEmblem() { // sea monster: a tentacle curling out of a wave
    return `<path d="M${C - 180} ${Y + 70} Q ${C - 120} ${Y + 20} ${C - 60} ${Y + 70} T ${C + 60} ${Y + 70} T ${C + 180} ${Y + 70} L ${C + 180} ${Y + 200} L ${C - 180} ${Y + 200} Z"
            fill="#1f8fd6" stroke="#0c4a75" stroke-width="7" stroke-linejoin="round"/>
        <path d="M${C - 30} ${Y + 80} C ${C - 60} ${Y - 20}, ${C + 40} ${Y - 70}, ${C + 70} ${Y - 120} C ${C + 96} ${Y - 160}, ${C + 40} ${Y - 180}, ${C + 26} ${Y - 140} C ${C + 16} ${Y - 112}, ${C + 52} ${Y - 110}, ${C + 52} ${Y - 128}"
            fill="none" stroke="#2c6b3f" stroke-width="54" stroke-linecap="round"/>
        <path d="M${C - 30} ${Y + 80} C ${C - 60} ${Y - 20}, ${C + 40} ${Y - 70}, ${C + 70} ${Y - 120} C ${C + 96} ${Y - 160}, ${C + 40} ${Y - 180}, ${C + 26} ${Y - 140} C ${C + 16} ${Y - 112}, ${C + 52} ${Y - 110}, ${C + 52} ${Y - 128}"
            fill="none" stroke="#4fbf72" stroke-width="40" stroke-linecap="round"/>
        ${[0, 1, 2, 3].map(i => `<circle cx="${C - 34 + i * 26}" cy="${Y + 20 - i * 44}" r="8" fill="#c6f5d2"/>`).join('')}
        <path d="M${C - 160} ${Y + 110} q 30 -18 60 0 M${C + 70} ${Y + 130} q 30 -18 60 0" stroke="#bfe6ff" stroke-width="8" fill="none" stroke-linecap="round"/>`;
}

function stinger() { // giant scorpion: a segmented tail curling to a stinger
    const segs = [];
    const pts = [[C - 30, Y + 140], [C - 70, Y + 70], [C - 74, Y - 4], [C - 34, Y - 70], [C + 30, Y - 100], [C + 92, Y - 76]];
    pts.forEach(([x, y], i) => {
        const r = 44 - i * 4;
        segs.push(`<circle cx="${x}" cy="${y}" r="${r}" fill="#b5462a" stroke="#5e1f10" stroke-width="6"/>
                   <circle cx="${x - r * 0.3}" cy="${y - r * 0.3}" r="${r * 0.3}" fill="#e07a5a"/>`);
    });
    segs.push(`<path d="M${C + 110} ${Y - 92} Q ${C + 170} ${Y - 70} ${C + 150} ${Y} Q ${C + 136} ${Y - 40} ${C + 92} ${Y - 46} Z"
        fill="#2b1a14" stroke="#120a06" stroke-width="6" stroke-linejoin="round"/>
        <circle cx="${C + 152}" cy="${Y + 14}" r="9" fill="#9fff5a"/>`);
    return segs.join('');
}

function snowflake() { // ice monster: a crystal snowflake
    const arms = [];
    for (let i = 0; i < 6; i++) {
        const a = i * 60;
        arms.push(`<g transform="rotate(${a} ${C} ${Y})">
            <path d="M${C} ${Y} L${C} ${Y - 140}" stroke="#2f6fa8" stroke-width="30" stroke-linecap="round"/>
            <path d="M${C} ${Y} L${C} ${Y - 140}" stroke="#bfe9ff" stroke-width="18" stroke-linecap="round"/>
            <path d="M${C} ${Y - 80} l -36 -36 M${C} ${Y - 80} l 36 -36" stroke="#2f6fa8" stroke-width="22" stroke-linecap="round"/>
            <path d="M${C} ${Y - 80} l -36 -36 M${C} ${Y - 80} l 36 -36" stroke="#e6f7ff" stroke-width="10" stroke-linecap="round"/>
        </g>`);
    }
    return arms.join('') + `<circle cx="${C}" cy="${Y}" r="34" fill="#e6f7ff" stroke="#2f6fa8" stroke-width="8"/>`;
}

// --- the milestone list -------------------------------------------------------------
const BADGES = [];
for (let lvl = 1; lvl <= 3; lvl++) {
    BADGES.push({ name: `blackdragon_${lvl}`, rim: '#6a3fb0', face: '#2a1f3d', glow: '#9b4dff', level: lvl, maxLevel: 3, label: 'BLACK ' + ROMAN[lvl], emblem: blackDragonEmblem(lvl) });
    BADGES.push({ name: `kingdragon_${lvl}`, rim: '#d4a017', face: '#5a1616', glow: '#ff9a1f', level: lvl, maxLevel: 3, label: 'KING ' + ROMAN[lvl], emblem: kingDragonEmblem(lvl) });
}
BADGES.push({ name: 'dinomonster_1', rim: '#6aa84f', face: '#d9c99a', glow: '#8fd16a', level: 1, maxLevel: 1, label: 'TITAN', emblem: footprint() });
BADGES.push({ name: 'landmonster_1', rim: '#a0703f', face: '#e8d5b0', glow: '#d39a5c', level: 1, maxLevel: 1, label: 'TITAN', emblem: clawMarks() });
BADGES.push({ name: 'seamonster_1', rim: '#2d7fc1', face: '#123a5c', glow: '#3fb0ff', level: 1, maxLevel: 1, label: 'TITAN', emblem: seaEmblem() });
BADGES.push({ name: 'giantscorpion_1', rim: '#c2582f', face: '#f0d58c', glow: '#ff8a4a', level: 1, maxLevel: 1, label: 'TITAN', emblem: stinger() });
BADGES.push({ name: 'icemonster_1', rim: '#4fa3d9', face: '#1d3f66', glow: '#9fe3ff', level: 1, maxLevel: 1, label: 'TITAN', emblem: snowflake() });

(async () => {
    const outDir = process.argv[2] || path.join(__dirname, '..', '..', 'web', 'img', 'milestones');
    const size = Number(process.argv[3]) || 256;
    fs.mkdirSync(outDir, { recursive: true });
    const browser = await chromium.launch();
    const page = await browser.newPage({ viewport: { width: size, height: size } });
    for (const b of BADGES) {
        const svg = medallion({ ...b, id: b.name }, b.emblem);
        await page.setContent(`<html><body style="margin:0;background:transparent">` +
            `<img src="data:image/svg+xml;base64,${Buffer.from(svg).toString('base64')}" width="${size}" height="${size}" style="display:block"></body></html>`);
        await page.waitForFunction(() => document.images[0].complete);
        await page.screenshot({ path: path.join(outDir, b.name + '.png'), omitBackground: true, clip: { x: 0, y: 0, width: size, height: size } });
    }
    await browser.close();
    console.log(`drew ${BADGES.length} milestone images in ${outDir}`);
})().catch(e => {
    console.error(e);
    process.exit(1);
});
