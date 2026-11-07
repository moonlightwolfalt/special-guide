#!/usr/bin/env python3
"""Draws the outer-space objects as SVGs in the game's flat cartoon style.

Each image is solid colours with a thick outline a shade darker than the fill, a
soft highlight top-left and a shade bottom-right, like the animals and food.
render.js turns the SVGs into the PNGs in web/img/space/.

Usage: python3 tools/space-art/make_space_art.py OUT_DIR
"""
import math
import random
import sys
from pathlib import Path

S = 512  # canvas size of every image
C = S / 2


def shade(hex_color, f):
    """Darker (f < 1) or lighter (f > 1) version of a #rrggbb colour."""
    h = hex_color.lstrip('#')
    r, g, b = (int(h[i:i + 2], 16) for i in (0, 2, 4))
    if f < 1:
        r, g, b = (int(v * f) for v in (r, g, b))
    else:
        r, g, b = (int(v + (255 - v) * (f - 1)) for v in (r, g, b))
    return '#%02x%02x%02x' % (r, g, b)


def svg(body, defs=''):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{S}" height="{S}" viewBox="0 0 {S} {S}">'
            f'<defs>{defs}</defs>{body}</svg>')


def ball(cx, cy, r, color, outline=None, highlight=True, shadow=True, clip_id=None):
    """A mope-style ball: fill, shade crescent, highlight, thick darker outline."""
    out = outline or shade(color, 0.62)
    w = max(3, r * 0.09)
    cid = clip_id or f'c{int(cx)}{int(cy)}{int(r)}'
    parts = [f'<clipPath id="{cid}"><circle cx="{cx}" cy="{cy}" r="{r}"/></clipPath>']
    if shadow:
        # darker ball with the main colour laid over it, nudged up-left: a shade crescent bottom-right
        parts.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{shade(color, 0.8)}"/>')
        parts.append(f'<circle clip-path="url(#{cid})" cx="{cx - r * 0.13}" cy="{cy - r * 0.13}" r="{r * 0.93}" fill="{color}"/>')
    else:
        parts.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{color}"/>')
    if highlight:
        parts.append(f'<ellipse cx="{cx - r * 0.35}" cy="{cy - r * 0.38}" rx="{r * 0.34}" ry="{r * 0.22}" '
                     f'transform="rotate(-35 {cx - r * 0.35} {cy - r * 0.38})" fill="#ffffff" opacity="0.35"/>')
    parts.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="{out}" stroke-width="{w}"/>')
    return ''.join(parts)


def glow(cx, cy, r, color, gid, opacity=0.6):
    return (f'<radialGradient id="{gid}"><stop offset="0" stop-color="{color}" stop-opacity="{opacity}"/>'
            f'<stop offset="1" stop-color="{color}" stop-opacity="0"/></radialGradient>',
            f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="url(#{gid})"/>')


def sparkle(x, y, r, color='#ffffff'):
    """A little four-pointed star."""
    p = []
    for i in range(8):
        a = math.pi / 4 * i
        rr = r if i % 2 == 0 else r * 0.3
        p.append(f'{x + math.cos(a) * rr:.1f},{y + math.sin(a) * rr:.1f}')
    return f'<polygon points="{" ".join(p)}" fill="{color}"/>'


def spiral_galaxy(cx, cy, r, arm, core, rot=0, arms=2, gid='g'):
    """Spiral arms as thick tapering curves with a bright core."""
    d, body = glow(cx, cy, r * 1.05, arm, gid + 'h', 0.45)
    parts = [body]
    for k in range(arms):
        base = rot + k * 2 * math.pi / arms
        pts = []
        for i in range(60):
            t = i / 59
            a = base + t * 3.4
            rr = r * (0.12 + 0.88 * t)
            pts.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr * 0.62))
        path = 'M' + ' L'.join(f'{x:.1f},{y:.1f}' for x, y in pts)
        parts.append(f'<path d="{path}" fill="none" stroke="{shade(arm, 0.6)}" stroke-width="{r * 0.2}" stroke-linecap="round"/>')
        parts.append(f'<path d="{path}" fill="none" stroke="{arm}" stroke-width="{r * 0.13}" stroke-linecap="round"/>')
        parts.append(f'<path d="{path}" fill="none" stroke="{shade(arm, 1.5)}" stroke-width="{r * 0.04}" stroke-linecap="round" opacity="0.7"/>')
    parts.append(ball(cx, cy, r * 0.2, core, highlight=True, shadow=False))
    return d, ''.join(parts)


def planet_rocky():
    rnd = random.Random(1)
    body = ball(C, C, 200, '#d9573b', clip_id='pr')
    craters = ''
    for _ in range(7):
        a, d, cr = rnd.random() * 6.3, rnd.random() * 140, 14 + rnd.random() * 26
        x, y = C + math.cos(a) * d, C + math.sin(a) * d
        craters += (f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{cr:.0f}" fill="{shade("#d9573b", 0.8)}" '
                    f'stroke="{shade("#d9573b", 0.62)}" stroke-width="5"/>')
    return svg(body + f'<g clip-path="url(#pr)">{craters}</g>' + ball(C, C, 200, '#d9573b', highlight=True, shadow=False).replace(
        'fill="#d9573b"', 'fill="none"', 1))


def planet_ocean():
    body = ball(C, C, 200, '#2f8fe0', clip_id='po')
    land = ('<g clip-path="url(#po)" fill="#4cc25a" stroke="#2f8a3b" stroke-width="7">'
            '<path d="M150 170 q40 -50 90 -10 q30 30 -10 60 q-40 30 -70 0 q-30 -20 -10 -50z"/>'
            '<path d="M300 280 q60 -30 90 20 q20 50 -40 70 q-50 10 -60 -30 q-10 -40 10 -60z"/>'
            '<path d="M190 340 q30 -10 40 20 q0 30 -30 30 q-30 -10 -10 -50z"/></g>')
    clouds = ('<g clip-path="url(#po)" fill="#ffffff" opacity="0.8">'
              '<ellipse cx="320" cy="160" rx="60" ry="16"/><ellipse cx="170" cy="290" rx="50" ry="13"/></g>')
    return svg(body + land + clouds + ball(C, C, 200, '#2f8fe0', shadow=False).replace('fill="#2f8fe0"', 'fill="none"', 1))


def planet_ringed():
    ring_back = ('<ellipse cx="256" cy="262" rx="236" ry="62" fill="none" stroke="#a86a2c" stroke-width="40" transform="rotate(-18 256 262)"/>'
                 '<ellipse cx="256" cy="262" rx="236" ry="62" fill="none" stroke="#f0bd6e" stroke-width="26" transform="rotate(-18 256 262)"/>')
    body = ball(C, C, 150, '#f2a344', clip_id='pg')
    bands = ('<g clip-path="url(#pg)" fill="none" stroke-width="22">'
             '<path d="M90 200 Q256 170 420 200" stroke="#d7813a"/><path d="M90 280 Q256 250 420 280" stroke="#e8c27a"/>'
             '<path d="M90 335 Q256 310 420 335" stroke="#d7813a"/></g>')
    # front half of the ring, drawn over the planet
    ring_front = ('<clipPath id="rf"><rect x="0" y="262" width="512" height="250" transform="rotate(-18 256 262)"/></clipPath>'
                  '<g clip-path="url(#rf)"><ellipse cx="256" cy="262" rx="236" ry="62" fill="none" stroke="#a86a2c" stroke-width="40" transform="rotate(-18 256 262)"/>'
                  '<ellipse cx="256" cy="262" rx="236" ry="62" fill="none" stroke="#f0bd6e" stroke-width="26" transform="rotate(-18 256 262)"/></g>')
    return svg(ring_back + body + bands + ball(C, C, 150, '#f2a344', shadow=False).replace('fill="#f2a344"', 'fill="none"', 1) + ring_front)


def star():
    d, g = glow(C, C, 250, '#ffd23f', 'sg', 0.55)
    rays = ''
    for i in range(12):
        a = 2 * math.pi * i / 12
        x1, y1 = C + math.cos(a) * 150, C + math.sin(a) * 150
        x2, y2 = C + math.cos(a) * 225, C + math.sin(a) * 225
        rays += f'<line x1="{x1:.0f}" y1="{y1:.0f}" x2="{x2:.0f}" y2="{y2:.0f}" stroke="#f08c1e" stroke-width="30" stroke-linecap="round"/>'
        rays += f'<line x1="{x1:.0f}" y1="{y1:.0f}" x2="{x2:.0f}" y2="{y2:.0f}" stroke="#ffd23f" stroke-width="16" stroke-linecap="round"/>'
    return svg(g + rays + ball(C, C, 140, '#ffd23f', outline='#e07a12'), d)


def star_system():
    d, g = glow(C, C, 110, '#ffd23f', 'ssg', 0.7)
    orbits = ''.join(f'<circle cx="{C}" cy="{C}" r="{r}" fill="none" stroke="#9fb4ff" stroke-width="4" stroke-dasharray="10 12" opacity="0.7"/>'
                     for r in (110, 165, 215))
    planets = (ball(C + 110 * math.cos(0.6), C + 110 * math.sin(0.6), 20, '#d9573b')
               + ball(C + 165 * math.cos(3.6), C + 165 * math.sin(3.6), 28, '#2f8fe0')
               + ball(C + 215 * math.cos(5.4), C + 215 * math.sin(5.4), 34, '#f2a344'))
    return svg(g + orbits + ball(C, C, 62, '#ffd23f', outline='#e07a12') + planets, d)


def black_hole():
    d1, g = glow(C, C, 250, '#ff7a1a', 'bhg', 0.5)
    disc = ''
    for w, col in ((70, '#8a2a0a'), (52, '#ff6a14'), (30, '#ffb347'), (10, '#fff1c2')):
        disc += f'<ellipse cx="{C}" cy="{C}" rx="215" ry="62" fill="none" stroke="{col}" stroke-width="{w}" transform="rotate(-15 {C} {C})"/>'
    hole = f'<circle cx="{C}" cy="{C}" r="105" fill="#05030a" stroke="#ffb347" stroke-width="10"/>'
    # the disc's near side passes in front of the hole
    # the half of the picture below the disc's tilted axis, as a polygon
    a = math.radians(-15)
    corners = [(-S, C), (2 * S, C), (2 * S, 2 * S), (-S, 2 * S)]
    rot = [(C + (x - C) * math.cos(a) - (y - C) * math.sin(a), C + (x - C) * math.sin(a) + (y - C) * math.cos(a)) for x, y in corners]
    poly = ' '.join(f'{x:.1f},{y:.1f}' for x, y in rot)
    front = f'<clipPath id="bf"><polygon points="{poly}"/></clipPath><g clip-path="url(#bf)">{disc}</g>'

    return svg(g + disc + hole + front, d1)


def galaxy():
    d, body = spiral_galaxy(C, C, 230, '#9b6bff', '#fff3c4', rot=0.3, arms=2, gid='ga')
    rnd = random.Random(5)
    sparks = ''.join(sparkle(C + (rnd.random() - 0.5) * 420, C + (rnd.random() - 0.5) * 260, 5 + rnd.random() * 6)
                     for _ in range(14))
    return svg(body + sparks, d)


def quasar():
    d1, g = glow(C, C, 200, '#7fe9ff', 'qg', 0.6)
    jets = ''
    for sgn in (-1, 1):
        y_tip = C + sgn * 245
        jets += (f'<polygon points="{C - 34},{C} {C + 34},{C} {C + 12},{y_tip} {C - 12},{y_tip}" fill="#3aa0ff" opacity="0.55"/>'
                 f'<polygon points="{C - 16},{C} {C + 16},{C} {C + 5},{y_tip} {C - 5},{y_tip}" fill="#e7fbff"/>')
    disc = (f'<ellipse cx="{C}" cy="{C}" rx="150" ry="40" fill="#ff8a3d" stroke="#a8461a" stroke-width="12"/>'
            f'<ellipse cx="{C}" cy="{C}" rx="100" ry="24" fill="#ffd27a"/>')
    return svg(g + jets + disc + ball(C, C, 38, '#ffffff', outline='#7fe9ff', shadow=False), d1)


def galaxy_cluster():
    d, g = glow(C, C, 250, '#b06bff', 'gcg', 0.35)
    defs, body = d, g
    spots = [(C - 110, C - 70, 70, '#9b6bff', 0.2), (C + 100, C - 90, 58, '#5fb0ff', 1.3), (C + 20, C + 90, 80, '#ff7ad9', 2.1),
             (C - 140, C + 110, 44, '#ffd23f', 0.9), (C + 160, C + 80, 40, '#7fe9a0', 2.8), (C - 10, C - 10, 34, '#ffb36b', 1.7)]
    for i, (x, y, r, col, rot) in enumerate(spots):
        dd, bb = spiral_galaxy(x, y, r, col, '#fff3c4', rot=rot, arms=2, gid=f'gc{i}')
        defs += dd
        body += bb
    return svg(body, defs)


def supercluster():
    rnd = random.Random(9)
    d, g = glow(C, C, 255, '#8a5cff', 'scg', 0.3)
    defs, body = d, g
    for i in range(16):
        a, dist = rnd.random() * 6.3, 40 + rnd.random() * 175
        x, y = C + math.cos(a) * dist, C + math.sin(a) * dist * 0.8
        r = 26 + rnd.random() * 30
        col = rnd.choice(['#b06bff', '#5fb0ff', '#ff7ad9', '#ffd23f'])
        dd, gg = glow(x, y, r * 1.4, col, f'sc{i}', 0.7)
        defs += dd
        body += gg + ball(x, y, r * 0.32, shade(col, 1.4), shadow=False)
    return svg(body, defs)


def cluster_filaments():
    rnd = random.Random(13)
    nodes = [(C + (rnd.random() - 0.5) * 400, C + (rnd.random() - 0.5) * 400) for _ in range(14)]
    links = set()
    for i, (x, y) in enumerate(nodes):
        near = sorted(range(len(nodes)), key=lambda j: (nodes[j][0] - x) ** 2 + (nodes[j][1] - y) ** 2)[1:4]
        for j in near:
            links.add(tuple(sorted((i, j))))
    fil = ''
    for i, j in links:
        (x1, y1), (x2, y2) = nodes[i], nodes[j]
        mx, my = (x1 + x2) / 2 + (rnd.random() - 0.5) * 50, (y1 + y2) / 2 + (rnd.random() - 0.5) * 50
        p = f'M{x1:.0f},{y1:.0f} Q{mx:.0f},{my:.0f} {x2:.0f},{y2:.0f}'
        fil += (f'<path d="{p}" fill="none" stroke="#5a2fa8" stroke-width="18" stroke-linecap="round" opacity="0.8"/>'
                f'<path d="{p}" fill="none" stroke="#c49bff" stroke-width="7" stroke-linecap="round"/>')
    defs, dots = '', ''
    for i, (x, y) in enumerate(nodes):
        dd, gg = glow(x, y, 46, '#ffb3f0', f'fn{i}', 0.75)
        defs += dd
        dots += gg + ball(x, y, 12, '#fff0fb', outline='#c45cff', shadow=False)
    return svg(fil + dots, defs)


def observable_universe():
    rnd = random.Random(21)
    # the cosmic microwave background as a mottled shell
    defs = '<clipPath id="ou"><circle cx="256" cy="256" r="236"/></clipPath>'
    cmb = '<circle cx="256" cy="256" r="236" fill="#f28a3a"/>'
    blobs = ''.join(f'<circle cx="{256 + (rnd.random() - 0.5) * 480:.0f}" cy="{256 + (rnd.random() - 0.5) * 480:.0f}" '
                    f'r="{12 + rnd.random() * 26:.0f}" fill="{rnd.choice(["#ffc15a", "#e0582a", "#4aa3ff", "#ffd98a"])}" opacity="0.85"/>'
                    for _ in range(140))
    inner = '<circle cx="256" cy="256" r="196" fill="#120a2e" stroke="#2a1660" stroke-width="10"/>'
    web = ''
    pts = [(256 + (rnd.random() - 0.5) * 330, 256 + (rnd.random() - 0.5) * 330) for _ in range(22)]
    for i, (x, y) in enumerate(pts):
        for j in sorted(range(len(pts)), key=lambda k: (pts[k][0] - x) ** 2 + (pts[k][1] - y) ** 2)[1:3]:
            x2, y2 = pts[j]
            web += f'<line x1="{x:.0f}" y1="{y:.0f}" x2="{x2:.0f}" y2="{y2:.0f}" stroke="#a77bff" stroke-width="4" opacity="0.8"/>'
    dots = ''.join(f'<circle cx="{x:.0f}" cy="{y:.0f}" r="6" fill="#ffe6ff"/>' for x, y in pts)
    clip_inner = '<clipPath id="oi"><circle cx="256" cy="256" r="192"/></clipPath>'
    outline = '<circle cx="256" cy="256" r="236" fill="none" stroke="#8a3d14" stroke-width="16"/>'
    highlight = '<ellipse cx="170" cy="130" rx="90" ry="40" transform="rotate(-35 170 130)" fill="#ffffff" opacity="0.18"/>'
    return svg(f'<g clip-path="url(#ou)">{cmb}{blobs}</g>{inner}<g clip-path="url(#oi)">{web}{dots}</g>{outline}{highlight}',
               defs + clip_inner)


ART = {
    'planet_rocky': planet_rocky,
    'planet_ocean': planet_ocean,
    'planet_ringed': planet_ringed,
    'star': star,
    'star_system': star_system,
    'black_hole': black_hole,
    'galaxy': galaxy,
    'quasar': quasar,
    'galaxy_cluster': galaxy_cluster,
    'supercluster': supercluster,
    'cluster_filaments': cluster_filaments,
    'observable_universe': observable_universe,
}

if __name__ == '__main__':
    out = Path(sys.argv[1] if len(sys.argv) > 1 else 'space-svg')
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in ART.items():
        (out / f'{name}.svg').write_text(fn())
    print(f'wrote {len(ART)} SVGs to {out}')
