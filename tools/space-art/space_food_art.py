"""Pictures for the space foods that aren't in make_space_art.py: asteroids, moons,
satellites, the light spectrum and the 118 chemical elements. Same flat cartoon style."""
import math
import random

from make_space_art import C, S, ball, glow, shade, sparkle, svg

# --- asteroids, moons, satellites ------------------------------------------------

def _lumpy(cx, cy, r, color, seed, cid):
    rnd = random.Random(seed)
    n = 14
    pts = []
    for i in range(n):
        a = 2 * math.pi * i / n
        rr = r * (0.78 + rnd.random() * 0.3)
        pts.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr))
    d = 'M' + ' '.join(f'{x:.1f},{y:.1f}' for x, y in pts) + 'Z'
    out = shade(color, 0.6)
    body = (f'<clipPath id="{cid}"><path d="{d}"/></clipPath>'
            f'<path d="{d}" fill="{shade(color, 0.8)}"/>'
            f'<g clip-path="url(#{cid})"><circle cx="{cx - r * 0.12}" cy="{cy - r * 0.12}" r="{r * 0.9}" fill="{color}"/>')
    for _ in range(5):
        a, dd, cr = rnd.random() * 6.3, rnd.random() * r * 0.55, r * (0.1 + rnd.random() * 0.13)
        x, y = cx + math.cos(a) * dd, cy + math.sin(a) * dd
        body += f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{cr:.0f}" fill="{shade(color, 0.78)}" stroke="{out}" stroke-width="{r * 0.03:.1f}"/>'
    body += '</g>'
    body += (f'<ellipse cx="{cx - r * 0.32}" cy="{cy - r * 0.36}" rx="{r * 0.28}" ry="{r * 0.16}" '
             f'transform="rotate(-35 {cx - r * 0.32} {cy - r * 0.36})" fill="#ffffff" opacity="0.3"/>')
    body += f'<path d="{d}" fill="none" stroke="{out}" stroke-width="{r * 0.08:.1f}" stroke-linejoin="round"/>'
    return body


def asteroid_rock():
    return svg(_lumpy(C, C, 210, '#8d7b6a', 3, 'ar'))


def asteroid_iron():
    return svg(_lumpy(C, C, 200, '#7d8a99', 11, 'ai'))


def _crater_ball(color, seed, cid):
    rnd = random.Random(seed)
    body = ball(C, C, 205, color, clip_id=cid)
    craters = ''
    for _ in range(9):
        a, d, cr = rnd.random() * 6.3, rnd.random() * 150, 14 + rnd.random() * 30
        x, y = C + math.cos(a) * d, C + math.sin(a) * d
        craters += f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{cr:.0f}" fill="{shade(color, 0.85)}" stroke="{shade(color, 0.66)}" stroke-width="5"/>'
    return body + f'<g clip-path="url(#{cid})">{craters}</g>' + ball(C, C, 205, color, shadow=False).replace(f'fill="{color}"', 'fill="none"', 1)


def moon_grey():
    return svg(_crater_ball('#c9c9cf', 4, 'mg'))


def moon_ice():
    return svg(_crater_ball('#bfe6f2', 8, 'mi'))


def satellite():
    panel = lambda x: (f'<rect x="{x}" y="206" width="150" height="100" rx="10" fill="#2f5fd0" stroke="#1d3a85" stroke-width="10"/>'
                       + ''.join(f'<line x1="{x + 37 * k}" y1="206" x2="{x + 37 * k}" y2="306" stroke="#1d3a85" stroke-width="5"/>' for k in (1, 2, 3))
                       + f'<line x1="{x}" y1="256" x2="{x + 150}" y2="256" stroke="#1d3a85" stroke-width="5"/>')
    arms = '<rect x="150" y="248" width="212" height="16" fill="#9aa3ad" stroke="#5b636c" stroke-width="6"/>'
    body = '<rect x="196" y="186" width="120" height="140" rx="18" fill="#e8c24a" stroke="#9a7a1c" stroke-width="10"/>'
    shine = '<rect x="212" y="200" width="30" height="60" rx="10" fill="#ffffff" opacity="0.35"/>'
    dish = ('<path d="M256 186 L256 140" stroke="#5b636c" stroke-width="8"/>'
            '<path d="M216 128 Q256 90 296 128 Z" fill="#dfe4ea" stroke="#5b636c" stroke-width="8"/>')
    return svg(panel(10) + panel(352) + arms + body + shine + dish)


def satellite_dish():
    body = '<rect x="206" y="236" width="100" height="120" rx="16" fill="#cfd6de" stroke="#6b737c" stroke-width="10"/>'
    legs = '<path d="M226 356 L196 420 M286 356 L316 420" stroke="#6b737c" stroke-width="12" stroke-linecap="round"/>'
    dish = ('<path d="M120 230 Q256 360 392 230 Q256 270 120 230Z" fill="#f2f4f7" stroke="#6b737c" stroke-width="10" transform="rotate(-25 256 250)"/>'
            '<path d="M256 250 L330 150" stroke="#6b737c" stroke-width="8" stroke-linecap="round"/>'
            '<circle cx="334" cy="146" r="16" fill="#ff5a4a" stroke="#a8241a" stroke-width="6"/>')
    light = '<circle cx="256" cy="290" r="14" fill="#4cd964" stroke="#237a35" stroke-width="5"/>'
    return svg(legs + body + dish + light)


# --- the light spectrum ----------------------------------------------------------
# name, wave colour, how many crests (longer waves have fewer), label
WAVELENGTHS = [
    ('radio', '#b07a4f', 1.2, 'RADIO'),
    ('microwave', '#c96a3f', 2, 'MICRO'),
    ('infrared', '#d43b2e', 3, 'IR'),
    ('red', '#ff3b30', 4, ''),
    ('orange', '#ff9500', 4.5, ''),
    ('yellow', '#ffd60a', 5, ''),
    ('green', '#34c759', 5.5, ''),
    ('blue', '#2f7bff', 6, ''),
    ('violet', '#9b5cff', 6.5, ''),
    ('ultraviolet', '#c86bff', 8, 'UV'),
    ('xray', '#7fe9ff', 10, 'X-RAY'),
    ('gamma', '#b6ff6b', 13, 'GAMMA'),
]


def photon(color, crests, label):
    d, g = glow(C, C, 240, color, 'pg', 0.55)
    pts = []
    for i in range(121):
        t = i / 120
        x = 60 + t * 392
        y = C + math.sin(t * crests * 2 * math.pi) * 70
        pts.append(f'{x:.1f},{y:.1f}')
    wave = 'M' + ' L'.join(pts)
    body = (g + ball(C, C, 150, shade(color, 1.25), outline=shade(color, 0.6), shadow=False)
            + f'<path d="{wave}" fill="none" stroke="{shade(color, 0.55)}" stroke-width="34" stroke-linecap="round" stroke-linejoin="round"/>'
            + f'<path d="{wave}" fill="none" stroke="{color}" stroke-width="20" stroke-linecap="round" stroke-linejoin="round"/>'
            + f'<path d="{wave}" fill="none" stroke="#ffffff" stroke-width="6" stroke-linecap="round" opacity="0.6"/>'
            + sparkle(150, 140, 16) + sparkle(370, 380, 12))
    if label:
        body += (f'<text x="{C}" y="{C + 205}" text-anchor="middle" font-family="Arial Black, Arial, sans-serif" font-weight="900" '
                 f'font-size="58" fill="#ffffff" stroke="{shade(color, 0.45)}" stroke-width="10" paint-order="stroke">{label}</text>')
    return svg(body, d)


# --- the elements ----------------------------------------------------------------
# symbol, name, group colour key
ELEMENTS = [
    ('H', 'Hydrogen', 'nonmetal'), ('He', 'Helium', 'noble'), ('Li', 'Lithium', 'alkali'), ('Be', 'Beryllium', 'alkaline'),
    ('B', 'Boron', 'metalloid'), ('C', 'Carbon', 'nonmetal'), ('N', 'Nitrogen', 'nonmetal'), ('O', 'Oxygen', 'nonmetal'),
    ('F', 'Fluorine', 'halogen'), ('Ne', 'Neon', 'noble'), ('Na', 'Sodium', 'alkali'), ('Mg', 'Magnesium', 'alkaline'),
    ('Al', 'Aluminium', 'post'), ('Si', 'Silicon', 'metalloid'), ('P', 'Phosphorus', 'nonmetal'), ('S', 'Sulfur', 'nonmetal'),
    ('Cl', 'Chlorine', 'halogen'), ('Ar', 'Argon', 'noble'), ('K', 'Potassium', 'alkali'), ('Ca', 'Calcium', 'alkaline'),
    ('Sc', 'Scandium', 'transition'), ('Ti', 'Titanium', 'transition'), ('V', 'Vanadium', 'transition'), ('Cr', 'Chromium', 'transition'),
    ('Mn', 'Manganese', 'transition'), ('Fe', 'Iron', 'transition'), ('Co', 'Cobalt', 'transition'), ('Ni', 'Nickel', 'transition'),
    ('Cu', 'Copper', 'transition'), ('Zn', 'Zinc', 'transition'), ('Ga', 'Gallium', 'post'), ('Ge', 'Germanium', 'metalloid'),
    ('As', 'Arsenic', 'metalloid'), ('Se', 'Selenium', 'nonmetal'), ('Br', 'Bromine', 'halogen'), ('Kr', 'Krypton', 'noble'),
    ('Rb', 'Rubidium', 'alkali'), ('Sr', 'Strontium', 'alkaline'), ('Y', 'Yttrium', 'transition'), ('Zr', 'Zirconium', 'transition'),
    ('Nb', 'Niobium', 'transition'), ('Mo', 'Molybdenum', 'transition'), ('Tc', 'Technetium', 'transition'), ('Ru', 'Ruthenium', 'transition'),
    ('Rh', 'Rhodium', 'transition'), ('Pd', 'Palladium', 'transition'), ('Ag', 'Silver', 'transition'), ('Cd', 'Cadmium', 'transition'),
    ('In', 'Indium', 'post'), ('Sn', 'Tin', 'post'), ('Sb', 'Antimony', 'metalloid'), ('Te', 'Tellurium', 'metalloid'),
    ('I', 'Iodine', 'halogen'), ('Xe', 'Xenon', 'noble'), ('Cs', 'Caesium', 'alkali'), ('Ba', 'Barium', 'alkaline'),
    ('La', 'Lanthanum', 'lanthanide'), ('Ce', 'Cerium', 'lanthanide'), ('Pr', 'Praseodymium', 'lanthanide'), ('Nd', 'Neodymium', 'lanthanide'),
    ('Pm', 'Promethium', 'lanthanide'), ('Sm', 'Samarium', 'lanthanide'), ('Eu', 'Europium', 'lanthanide'), ('Gd', 'Gadolinium', 'lanthanide'),
    ('Tb', 'Terbium', 'lanthanide'), ('Dy', 'Dysprosium', 'lanthanide'), ('Ho', 'Holmium', 'lanthanide'), ('Er', 'Erbium', 'lanthanide'),
    ('Tm', 'Thulium', 'lanthanide'), ('Yb', 'Ytterbium', 'lanthanide'), ('Lu', 'Lutetium', 'lanthanide'), ('Hf', 'Hafnium', 'transition'),
    ('Ta', 'Tantalum', 'transition'), ('W', 'Tungsten', 'transition'), ('Re', 'Rhenium', 'transition'), ('Os', 'Osmium', 'transition'),
    ('Ir', 'Iridium', 'transition'), ('Pt', 'Platinum', 'transition'), ('Au', 'Gold', 'transition'), ('Hg', 'Mercury', 'transition'),
    ('Tl', 'Thallium', 'post'), ('Pb', 'Lead', 'post'), ('Bi', 'Bismuth', 'post'), ('Po', 'Polonium', 'post'),
    ('At', 'Astatine', 'halogen'), ('Rn', 'Radon', 'noble'), ('Fr', 'Francium', 'alkali'), ('Ra', 'Radium', 'alkaline'),
    ('Ac', 'Actinium', 'actinide'), ('Th', 'Thorium', 'actinide'), ('Pa', 'Protactinium', 'actinide'), ('U', 'Uranium', 'actinide'),
    ('Np', 'Neptunium', 'actinide'), ('Pu', 'Plutonium', 'actinide'), ('Am', 'Americium', 'actinide'), ('Cm', 'Curium', 'actinide'),
    ('Bk', 'Berkelium', 'actinide'), ('Cf', 'Californium', 'actinide'), ('Es', 'Einsteinium', 'actinide'), ('Fm', 'Fermium', 'actinide'),
    ('Md', 'Mendelevium', 'actinide'), ('No', 'Nobelium', 'actinide'), ('Lr', 'Lawrencium', 'actinide'), ('Rf', 'Rutherfordium', 'transition'),
    ('Db', 'Dubnium', 'transition'), ('Sg', 'Seaborgium', 'transition'), ('Bh', 'Bohrium', 'transition'), ('Hs', 'Hassium', 'transition'),
    ('Mt', 'Meitnerium', 'unknown'), ('Ds', 'Darmstadtium', 'unknown'), ('Rg', 'Roentgenium', 'unknown'), ('Cn', 'Copernicium', 'transition'),
    ('Nh', 'Nihonium', 'unknown'), ('Fl', 'Flerovium', 'unknown'), ('Mc', 'Moscovium', 'unknown'), ('Lv', 'Livermorium', 'unknown'),
    ('Ts', 'Tennessine', 'unknown'), ('Og', 'Oganesson', 'unknown'),
]
assert len(ELEMENTS) == 118

GROUP_COLOURS = {
    'alkali': '#ff6b5a', 'alkaline': '#ffa94d', 'transition': '#ffd43b', 'post': '#9ad36a', 'metalloid': '#4ecdc4',
    'nonmetal': '#5ab0ff', 'halogen': '#7d8cff', 'noble': '#c07dff', 'lanthanide': '#ff7ad9', 'actinide': '#ff8fb1',
    'unknown': '#c2c7cf',
}


def element_tile(number, symbol, name, group):
    col = GROUP_COLOURS[group]
    out = shade(col, 0.55)
    size = 200 if len(symbol) == 1 else 170
    name_size = 52 if len(name) <= 9 else max(30, int(52 * 9 / len(name)))
    body = (f'<rect x="36" y="36" width="440" height="440" rx="70" fill="{shade(col, 0.82)}"/>'
            f'<rect x="36" y="36" width="410" height="410" rx="64" fill="{col}"/>'
            f'<ellipse cx="150" cy="110" rx="90" ry="34" transform="rotate(-20 150 110)" fill="#ffffff" opacity="0.35"/>'
            f'<rect x="36" y="36" width="440" height="440" rx="70" fill="none" stroke="{out}" stroke-width="22"/>'
            f'<text x="80" y="122" font-family="Arial Black, Arial, sans-serif" font-weight="900" font-size="64" fill="{out}">{number}</text>'
            f'<text x="{C}" y="{C + 70}" text-anchor="middle" font-family="Arial Black, Arial, sans-serif" font-weight="900" '
            f'font-size="{size}" fill="#ffffff" stroke="{out}" stroke-width="16" paint-order="stroke">{symbol}</text>'
            f'<text x="{C}" y="430" text-anchor="middle" font-family="Arial, sans-serif" font-weight="700" '
            f'font-size="{name_size}" fill="{out}">{name}</text>')
    return svg(body)


def all_food_art():
    """{file name: svg} for every picture this module draws."""
    art = {
        'asteroid_rock': asteroid_rock(), 'asteroid_iron': asteroid_iron(),
        'moon_grey': moon_grey(), 'moon_ice': moon_ice(),
        'satellite': satellite(), 'satellite_dish': satellite_dish(),
    }
    for name, color, crests, label in WAVELENGTHS:
        art[f'light_{name}'] = photon(color, crests, label)
    for i, (sym, name, group) in enumerate(ELEMENTS, 1):
        art[f'element_{i:03d}_{sym}'] = element_tile(i, sym, name, group)
    return art
