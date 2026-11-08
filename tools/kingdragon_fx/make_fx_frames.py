#!/usr/bin/env python3
"""Draws the animation frames for the King Dragon's Wing Slash, Wingspan and Shoot Meteor.

Each effect is a numbered sequence of transparent PNGs that the client plays over the game
(web/skilltree.js has the frame counts in FX; client.js draws object type 175, o_kingDragonFx):

  web/img/kingdragon/wingslash/0..7.png   a burning crescent sweeping across the front
  web/img/kingdragon/wingspan/0..9.png    both wings unfolding with a shockwave ring
  web/img/kingdragon/meteor/0..5.png      the flying meteor (loops)
  web/img/kingdragon/impact/0..9.png      the meteor's explosion

Every frame faces right (+x), with the dragon (or the meteor's centre) in the middle of the
image; the game rotates it to the dragon's facing. Drawn at 2x and scaled down for smooth edges.

Usage: python3 tools/kingdragon_fx/make_fx_frames.py [OUT_DIR] [SIZE]
       (defaults: web/img/kingdragon, 512)
Needs Pillow.
"""
import math
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

FRAMES = {'wingslash': 8, 'wingspan': 10, 'meteor': 6, 'impact': 10}

SS = 2  # supersampling
W = 512 * SS
C = W / 2
R = W / 2 * 0.96  # usable radius


def ease_out(t):
    return 1 - (1 - t) ** 3


def rgba(hex_color, a):
    h = hex_color.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (max(0, min(255, int(a))),)


def polar(r, deg, cx=C, cy=C):
    a = math.radians(deg)
    return (cx + math.cos(a) * r, cy + math.sin(a) * r)


def layer():
    return Image.new('RGBA', (W, W), (0, 0, 0, 0))


def over(base, paint):
    """Paints on a fresh layer and stacks it on base. Pillow replaces pixels when drawing (and
    blends alpha wrongly onto transparent images), so each colour gets its own layer."""
    top = layer()
    paint(ImageDraw.Draw(top))
    return Image.alpha_composite(base, top)


def band(draw, r0, r1, a0, a1, color, steps=None):
    """A ring segment between radii r0..r1 and angles a0..a1 (degrees)."""
    steps = steps or max(2, int(abs(a1 - a0) / 2))
    pts = [polar(r1, a0 + (a1 - a0) * i / steps) for i in range(steps + 1)]
    pts += [polar(r0, a1 - (a1 - a0) * i / steps) for i in range(steps + 1)]
    draw.polygon(pts, fill=color)


def glow(img, radius, color='#ff9a2a', strength=0.7):
    """A soft coloured glow under the shapes: their blurred outline in one colour.
    (Blurring the image itself would mix in the transparent black around it and leave grey edges.)"""
    a = img.getchannel('A').filter(ImageFilter.GaussianBlur(radius))
    a = a.point(lambda v: int(v * strength))
    g = Image.new('RGBA', img.size, rgba(color, 255))
    g.putalpha(a)
    return Image.alpha_composite(g, img)


# ------------------------------------------------------------------ Wing Slash
def wingslash(t):
    """A crescent of fire sweeping from -70 to +70 degrees, its tail fading behind the edge."""
    img = layer()
    d = ImageDraw.Draw(img)
    lead = -75 + 150 * ease_out(min(1, t * 1.25))
    fade = 1 if t < 0.65 else max(0, 1 - (t - 0.65) / 0.35)
    tail_len = 95
    a_start = max(-75, lead - tail_len)
    step = 1.5

    def slices(r0, r1, col, a_max, from_f=0.0):
        def paint(dd):
            a = a_start
            while a < lead:
                f = (a - a_start) / max(1, lead - a_start)  # 0 at the tail, 1 at the edge
                if f >= from_f:
                    al = (f ** 1.4) * fade * (1 if from_f == 0 else (f - from_f) / (1 - from_f))
                    band(dd, r0, r1, a, a + step + 0.5, rgba(col, a_max * al), 2)
                a += step
        return paint

    # outer orange, thinner yellow, white-hot core near the edge
    img = over(img, slices(R * 0.50, R * 0.97, '#ff5a14', 210))
    img = over(img, slices(R * 0.62, R * 0.92, '#ffb52e', 235))
    img = over(img, slices(R * 0.72, R * 0.87, '#fff6d0', 255, 0.55))
    d = ImageDraw.Draw(img)
    # embers thrown off the edge
    rnd = random.Random(7)
    for i in range(14):
        ang = lead - rnd.uniform(0, tail_len * 0.8)
        if ang < a_start:
            continue
        r = R * rnd.uniform(0.55, 1.0)
        x, y = polar(r + R * 0.08 * t, ang)
        s = W * rnd.uniform(0.004, 0.009)
        d.ellipse([x - s, y - s, x + s, y + s], fill=rgba('#ffe9a8', 230 * fade))
    # the dark leading edge line, like a claw
    if fade > 0:
        p1, p2 = polar(R * 0.48, lead), polar(R * 0.99, lead)
        d.line([p1, p2], fill=rgba('#7a1206', 220 * fade), width=int(W * 0.012))
    return glow(img, W * 0.018)


# ------------------------------------------------------------------ Wingspan
def wing(img, side, spread, alpha):
    """One dragon wing, unfolded by spread (0..1). side = -1 (up) or 1 (down) from the facing."""
    shoulder = (C - R * 0.05, C + side * R * 0.12)
    # finger bones fan out from the shoulder; their tips make the wing's edge.
    # Angles from the facing (+x): folded, every bone lies back along the body (165 degrees);
    # unfolded, they fan from 40 (forward) to 160 (back).
    fingers = [(40, 0.62), (70, 0.95), (100, 1.0), (130, 0.9), (160, 0.7)]
    folded = 165
    tips = []
    for open_ang, length in fingers:
        ang = side * (folded + (open_ang - folded) * spread)
        tips.append(polar(R * length * (0.35 + 0.6 * spread), ang, *shoulder))
    # membrane: shoulder, tips, with scalloped edges between tips
    pts = [shoulder]
    for i, tip in enumerate(tips):
        pts.append(tip)
        if i + 1 < len(tips):
            nx = (tip[0] + tips[i + 1][0]) / 2
            ny = (tip[1] + tips[i + 1][1]) / 2
            # pull the midpoint towards the shoulder for the scallop
            pts.append((nx + (shoulder[0] - nx) * 0.22, ny + (shoulder[1] - ny) * 0.22))
    pts.append((C - R * 0.30, C + side * R * 0.10))
    img = over(img, lambda dd: dd.polygon(pts, fill=rgba('#8e1b1b', 235 * alpha), outline=rgba('#3a0606', 255 * alpha)))
    # lighter inner membrane
    inner = [shoulder] + [(shoulder[0] + (x - shoulder[0]) * 0.7, shoulder[1] + (y - shoulder[1]) * 0.7) for x, y in tips]
    img = over(img, lambda dd: dd.polygon(inner, fill=rgba('#c4362a', 150 * alpha)))
    d = ImageDraw.Draw(img)
    # bones, gold-edged
    for tip in tips:
        d.line([shoulder, tip], fill=rgba('#3a0606', 255 * alpha), width=int(W * 0.022))
        d.line([shoulder, tip], fill=rgba('#e8b23a', 255 * alpha), width=int(W * 0.010))
        d.ellipse([tip[0] - W * 0.012, tip[1] - W * 0.012, tip[0] + W * 0.012, tip[1] + W * 0.012],
                  fill=rgba('#fff0b0', 255 * alpha))
    return img


def wingspan(t):
    img = layer()
    d = ImageDraw.Draw(img)
    spread = ease_out(min(1, t * 1.6))
    alpha = 1 if t < 0.7 else max(0, 1 - (t - 0.7) / 0.3)
    # shockwave ring
    ring_r = R * (0.25 + 0.75 * ease_out(t))
    ring_a = 220 * (1 - t)
    for k, col in ((0.06, '#ffcf6a'), (0.03, '#ffffff')):
        wdt = R * k
        d.ellipse([C - ring_r, C - ring_r, C + ring_r, C + ring_r], outline=rgba(col, ring_a), width=int(wdt))
    img = wing(img, -1, spread, alpha)
    img = wing(img, 1, spread, alpha)
    d = ImageDraw.Draw(img)
    # gust streaks pushed outward
    rnd = random.Random(3)
    for i in range(18):
        ang = rnd.uniform(0, 360)
        r0 = ring_r * rnd.uniform(0.75, 0.95)
        p1, p2 = polar(r0, ang), polar(r0 + R * 0.12, ang)
        d.line([p1, p2], fill=rgba('#fff3d6', 180 * (1 - t)), width=int(W * 0.006))
    return glow(img, W * 0.01, '#ffcf6a', 0.5)


# ------------------------------------------------------------------ Meteor
def meteor(i, n):
    img = layer()
    d = ImageDraw.Draw(img)
    rnd = random.Random(100 + i)
    rock_r = R * 0.30
    cx, cy = C + R * 0.42, C
    # fiery tail behind (to the left), flickering
    for layer_i, (col, wscale, lscale, a) in enumerate((('#ff4a10', 1.25, 1.0, 170), ('#ff9a2a', 1.0, 0.82, 210), ('#fff1a8', 0.6, 0.55, 230))):
        length = R * 1.25 * lscale * rnd.uniform(0.92, 1.05)
        half = rock_r * wscale
        pts = [(cx, cy - half)]
        steps = 10
        for s in range(1, steps + 1):
            f = s / steps
            wob = rnd.uniform(-0.08, 0.08) * half
            pts.append((cx - length * f, cy - half * (1 - f) ** 1.3 + wob))
        for s in range(steps, -1, -1):
            f = s / steps
            wob = rnd.uniform(-0.08, 0.08) * half
            pts.append((cx - length * f, cy + half * (1 - f) ** 1.3 + wob))
        d.polygon(pts, fill=rgba(col, a))
    img = glow(img, W * 0.02, '#ff5a14')
    d = ImageDraw.Draw(img)
    # the rock: lumpy outline, turning a little each frame
    turn = 360 / n * i
    pts = []
    lr = random.Random(42)
    for k in range(18):
        ang = k * 20 + turn
        r = rock_r * lr.uniform(0.86, 1.05)
        pts.append(polar(r, ang, cx, cy))
    d.polygon(pts, fill=rgba('#5b3a2a', 255), outline=rgba('#24140c', 255))
    d.line(pts + [pts[0]], fill=rgba('#24140c', 255), width=int(W * 0.012))
    # glowing cracks and craters
    cr = random.Random(9)
    for k in range(4):
        ang = cr.uniform(0, 360) + turn
        x, y = polar(rock_r * cr.uniform(0.2, 0.6), ang, cx, cy)
        s = rock_r * cr.uniform(0.12, 0.22)
        d.ellipse([x - s, y - s, x + s, y + s], fill=rgba('#3a2216', 255))
        d.ellipse([x - s * 0.5, y - s * 0.5, x + s * 0.5, y + s * 0.5], fill=rgba('#ff8a2a', 230))
    # hot front edge
    band(d, rock_r * 0.80, rock_r * 1.02, -55, 55, rgba('#ffcf6a', 150))
    return img


# ------------------------------------------------------------------ Impact
def impact(t):
    img = layer()
    d = ImageDraw.Draw(img)
    # scorched crater appearing underneath
    cr = R * 0.45 * min(1, t * 3)
    d.ellipse([C - cr, C - cr * 0.95, C + cr, C + cr * 0.95], fill=rgba('#2a1610', 200 * (1 - max(0, t - 0.6) / 0.4)))
    # smoke puffs drifting out later on
    rnd = random.Random(5)
    for k in range(14):
        ang = rnd.uniform(0, 360)
        dist = R * (0.25 + 0.55 * ease_out(t)) * rnd.uniform(0.7, 1.0)
        x, y = polar(dist, ang)
        s = R * (0.08 + 0.14 * t) * rnd.uniform(0.7, 1.2)
        a = 150 * min(1, t * 2.5) * (1 - t)
        img = over(img, lambda dd, x=x, y=y, s=s, a=a: dd.ellipse([x - s, y - s, x + s, y + s], fill=rgba('#5d5550', a)))
    d = ImageDraw.Draw(img)
    # fireball flash
    flash = R * (0.2 + 0.65 * ease_out(min(1, t * 2.2)))
    fa = 1 if t < 0.35 else max(0, 1 - (t - 0.35) / 0.45)
    for k, col, a in ((1.0, '#ff4a10', 200), (0.78, '#ff9a2a', 230), (0.5, '#fff1a8', 255), (0.25, '#ffffff', 255)):
        r = flash * k
        img = over(img, lambda dd, r=r, col=col, a=a: dd.ellipse([C - r, C - r, C + r, C + r], fill=rgba(col, a * fa)))
    d = ImageDraw.Draw(img)
    # shock ring
    ring = R * (0.3 + 0.68 * ease_out(t))
    d.ellipse([C - ring, C - ring, C + ring, C + ring], outline=rgba('#ffe2a0', 230 * (1 - t)), width=int(R * 0.035))
    # rock chunks thrown outward
    rk = random.Random(11)
    for k in range(16):
        ang = rk.uniform(0, 360)
        dist = R * (0.15 + 0.85 * ease_out(t)) * rk.uniform(0.6, 1.0)
        x, y = polar(dist, ang)
        s = R * rk.uniform(0.025, 0.05) * (1 - 0.5 * t)
        d.polygon([(x - s, y - s * 0.6), (x + s * 0.8, y - s), (x + s, y + s * 0.7), (x - s * 0.6, y + s)],
                  fill=rgba('#4a2c1e', 255 * (1 - t ** 2)))
        d.ellipse([x - s * 0.4, y - s * 0.4, x + s * 0.4, y + s * 0.4], fill=rgba('#ff8a2a', 230 * (1 - t)))
    return glow(img, W * 0.012, '#ff7a1a', 0.5)


def main():
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[2] / 'web' / 'img' / 'kingdragon'
    size = int(sys.argv[2]) if len(sys.argv) > 2 else 512
    makers = {
        'wingslash': lambda i, n: wingslash(i / (n - 1)),
        'wingspan': lambda i, n: wingspan(i / (n - 1)),
        'meteor': meteor,
        'impact': lambda i, n: impact(i / (n - 1)),
    }
    total = 0
    for name, n in FRAMES.items():
        d = out / name
        d.mkdir(parents=True, exist_ok=True)
        for i in range(n):
            img = makers[name](i, n).resize((size, size), Image.LANCZOS)
            img.save(d / f'{i}.png', optimize=True)
            total += 1
    print(f'drew {total} frames in {out}')


if __name__ == '__main__':
    main()
