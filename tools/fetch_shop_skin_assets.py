#!/usr/bin/env python3
"""Download every texture used by the shop skins on a Mope site.

The site does not list its folders, and its client builds most texture paths
at run time (heads, wings, tails, animation frames, ability icons). So this
script opens the site in headless Chromium and makes the site's own client
draw each shop skin in many states (ability on, flying, underwater, attacking,
every variant) across a few seconds of animation. Every texture path the
client asks for is recorded, then downloaded. A second pass tries names that
other skins of the same animal use (and any --names-from folder), and follows
numbered runs such as tongue0..tongue12, to catch textures used only in states
the sweep cannot reach.

Needs Playwright for Python:
    pip install playwright
    playwright install chromium      # or pass --browser /path/to/chrome

Usage:
    python tools/fetch_shop_skin_assets.py [--site URL] [--out DIR]
        [--source original|game|both] [--only ID[,ID...]] [--concurrency N]
        [--browser PATH] [--names-from DIR] [--list-only]

Output:
    DIR/skins/...            textures, in the site's own folder layout
    DIR/assets/s/0/skins/... the game's smaller copies (with --source game/both)
    DIR/skins.json           every skin and the files it uses
"""

import argparse
import concurrent.futures
import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path, PurePosixPath

try:
    from playwright.sync_api import sync_playwright
except ImportError:
    sys.exit("Playwright for Python is required: pip install playwright && playwright install chromium")

USER_AGENT = "mope-shop-skin-assets/1.0"

# --- reading the skin list out of client.js ---------------------------------

_STR = r"""(?:'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"|`(?:[^`\\$]|\\.)*`)"""
# case 'gold_kraken': return [a_kraken, 0x1, 'Golden\nKraken', 'ocean/kraken/1/kraken', !0x0];
_CASE_RE = re.compile(
    r"case\s*(" + _STR + r")\s*:\s*return\s*\[\s*([A-Za-z_$][\w$]*)\s*,\s*(0x[0-9a-fA-F]+|\d+)\s*,\s*("
    + _STR + r")\s*,\s*(" + _STR + r")\s*(?:,\s*(!0x0|!0x1|!0|!1|true|false)\s*)?\]",
    re.S,
)


def _js_string(lit):
    body = lit[1:-1]

    def esc(m):
        e = m.group(1)
        if e[0] == "x":
            return chr(int(e[1:], 16))
        if e[0] == "u":
            return chr(int(e[2:-1] if e[1] == "{" else e[1:], 16))
        return {"n": "\n", "r": "\r", "t": "\t", "b": "\b", "f": "\f", "v": "\v", "0": "\0", "\n": ""}.get(e, e)

    return re.sub(r"\\(x[0-9a-fA-F]{2}|u\{[0-9a-fA-F]+\}|u[0-9a-fA-F]{4}|[\s\S])", esc, body)


def parse_skins(client_js):
    skins = {}
    for m in _CASE_RE.finditer(client_js):
        sid = _js_string(m.group(1))
        if sid in skins:
            continue
        image = _js_string(m.group(5))
        # skins/<biome>/<animal>/<species>[/<sub>]/<file>: the numbers after the animal folder
        nums = [int(n) for n in re.findall(r"/(\d+)(?=/)", "/" + image)]
        skins[sid] = {
            "id": sid,
            "name": re.sub(r"\s*\n\s*", " ", _js_string(m.group(4))).strip(),
            "animalVar": m.group(2),
            "species": nums[0] if nums else int(m.group(3), 0),
            "subSpecies": nums[1] if len(nums) > 1 else 0,
            "shopImage": f"skins/{image}.png",
        }
    return list(skins.values())


# --- the in-page collector ----------------------------------------------------

COLLECTOR_JS = r"""
() => {
  const seen = new Set();
  const orig = window.getLoadedImg;
  window.getLoadedImg = function (p) { if (typeof p === 'string') seen.add(p); return orig.apply(this, arguments); };
  const DRAW = ['draw', 'customDraw', 'drawAnimalSkin', 'drawUnderSkinTail', 'drawUnderSkinImg', 'drawSkinImg', 'basicDrawSkinImg',
    'drawSkinCustomization', 'drawOnTopOfSkinImg', 'drawPropsUnderSkin', 'drawPropsTopOfSkin', 'drawUnderEffects',
    'drawTopEffects', 'drawWhenUnderwater', 'drawUnderwaterCustomization', 'loadAnimalSkinImg'];
  const STATES = [{}, {flag_usingAbility: true}, {flag_flying: true}, {flag_underWater: true}, {isAttacking: true},
    {flag_usingAbility: true, flag_flying: true}, {flag_usingAbility: true, flag_underWater: true}];
  for (let s = 1; s <= 6; s++) { STATES.push({specType: s}); STATES.push({specType: s, flag_usingAbility: true}); }

  window.collectSkin = function (type, species, sub) {
    seen.clear();
    const errors = new Set();
    const cv = document.createElement('canvas'); cv.width = cv.height = 256;
    const realCtx = window.ctx, realT = window.timestamp;
    window.ctx = cv.getContext('2d');
    try {
      for (const st of STATES) {
        let o;
        try { o = GameObjType.createGameObjOfOType(o_animal, type); } catch (e) { errors.add('create: ' + e.message); break; }
        Object.assign(o, { animalType: type, animalSpecies: species, animalSubSpecies: sub, rad: 60, oRad: 60, nRad: 60,
          x: 128, y: 128, ox: 128, oy: 128, nx: 128, ny: 128, angle: 0, specType: 0 }, st);
        try { o.animalInfo && o.animalInfo(); } catch (e) {}
        try { const p = o.getSkinImagePathForShop && o.getSkinImagePathForShop(); if (p) seen.add(p); } catch (e) {}
        for (let a = 0; a < 160; a++) {
          try { const ai = o.getAbilityInfo(a); if (ai && typeof ai.abilImg === 'string') seen.add(ai.abilImg); } catch (e) {}
        }
        // animation frames are picked from the clock, so step through a few seconds of it
        for (let t = 0; t < 3000; t += 40) {
          window.timestamp = (realT || 0) + t;
          for (const f of DRAW) {
            if (typeof o[f] !== 'function') continue;
            window.ctx.save();
            try { o[f](window.ctx); } catch (e) { errors.add(f + ': ' + String(e.message).slice(0, 80)); }
            window.ctx.restore();
          }
        }
      }
    } finally {
      window.ctx = realCtx; window.timestamp = realT;
    }
    return { paths: [...seen], errors: [...errors] };
  };
}
"""

_PREFIX_RE = re.compile(r"^(?:\./)?(?:assets/[st]/\d+/)?")
_IMAGE_EXT = (".png", ".webp", ".jpg", ".jpeg", ".gif", ".svg")


def canonical(path, site):
    """Site-relative texture path without the season/theme prefix, or None if unusable."""
    p = path.strip()
    if p.startswith(site + "/"):
        p = p[len(site) + 1:]
    elif re.match(r"^https?://", p):
        return None  # another host
    p = p.split("?")[0].split("#")[0].lstrip("/")
    p = _PREFIX_RE.sub("", p)
    # broken paths the client builds when a part is missing for a skin
    if not p or "undefined" in p or "null" in p or "//" in p or not p.lower().endswith(_IMAGE_EXT):
        return None
    parts = PurePosixPath(p).parts
    if ".." in parts:
        return None
    return p


# --- downloading ---------------------------------------------------------------

def _is_image(data):
    return (data[:8] == b"\x89PNG\r\n\x1a\n" or data[:4] == b"RIFF" and data[8:12] == b"WEBP"
            or data[:3] == b"\xff\xd8\xff" or data[:6] in (b"GIF87a", b"GIF89a") or data.lstrip()[:4] == b"<svg")


def fetch(url, retries=3, timeout=30):
    """Image bytes, or None when the site has no such file."""
    quoted = urllib.parse.quote(url, safe=":/?&=%#+")
    for attempt in range(retries + 1):
        try:
            req = urllib.request.Request(quoted, headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(req, timeout=timeout) as res:
                data = res.read()
            return data if _is_image(data) else None
        except urllib.error.HTTPError as e:
            if e.code < 500 or attempt == retries:
                return None
        except (urllib.error.URLError, TimeoutError, ConnectionError, OSError):
            if attempt == retries:
                return None
        time.sleep(2 ** attempt)
    return None


def save(out_dir, rel, data):
    dest = out_dir.joinpath(*PurePosixPath(rel).parts)
    if out_dir.resolve() not in dest.resolve().parents:
        return False
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_bytes(data)
    return True


def download_one(site, out_dir, rel, source):
    """Returns {"original": bool, "game": bool} for what was saved."""
    got = {"original": False, "game": False}
    want_original = source in ("original", "both")
    want_game = source in ("game", "both")
    original_path, game_path = rel, f"assets/s/0/{rel}"
    if want_original:
        if (out_dir / original_path).exists():
            got["original"] = True
        else:
            data = fetch(f"{site}/{original_path}")
            if data:
                got["original"] = save(out_dir, original_path, data)
    if want_game or (source == "original" and not got["original"]):
        if (out_dir / game_path).exists():
            got["game"] = True
        else:
            data = fetch(f"{site}/{game_path}")
            if data:
                # with --source original, the game copy stands in at the original's path
                target = game_path if want_game else original_path
                got["game"] = save(out_dir, target, data)
    return got


# --- main ----------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--site", default="https://mope.trygames.party")
    ap.add_argument("--out", default="shop-skin-assets")
    ap.add_argument("--source", choices=["original", "game", "both"], default="original",
                    help="original: full-size skins/ files, falling back to the game copy; "
                         "game: the smaller assets/s/0/ copies the game loads; both: keep both")
    ap.add_argument("--only", help="comma-separated skin ids to fetch")
    ap.add_argument("--concurrency", type=int, default=8)
    ap.add_argument("--browser", help="path to a Chrome/Chromium binary (default: Playwright's own)")
    ap.add_argument("--list-only", action="store_true", help="record the texture paths but download nothing")
    ap.add_argument("--names-from", help="a local skins folder (e.g. web/skins) to learn more texture names from")
    args = ap.parse_args()
    site = args.site.rstrip("/")
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    print(f"Fetching {site}/client.js ...")
    req = urllib.request.Request(f"{site}/client.js", headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=120) as res:
        client_js = res.read().decode("utf-8", "replace")
    skins = parse_skins(client_js)
    if args.only:
        wanted = set(args.only.split(","))
        skins = [s for s in skins if s["id"] in wanted]
    if not skins:
        sys.exit("No shop skins found.")
    print(f"Found {len(skins)} shop skins. Opening the site to record their textures ...")

    site_host = urllib.parse.urlparse(site).netloc
    with sync_playwright() as p:
        launch = {"executable_path": args.browser} if args.browser else {}
        browser = p.chromium.launch(**launch)
        page = browser.new_page()
        # only the site itself: no ads, analytics or game servers
        page.route("**/*", lambda r: r.continue_() if urllib.parse.urlparse(r.request.url).netloc == site_host
                   and r.request.resource_type not in ("websocket",) else r.abort())
        page.goto(site + "/", wait_until="load", timeout=120000)
        page.wait_for_function("typeof GameObjType !== 'undefined' && typeof getLoadedImg === 'function'"
                               " && typeof o_animal !== 'undefined'", timeout=120000)
        page.evaluate(COLLECTOR_JS)

        for i, s in enumerate(skins, 1):
            animal_type = page.evaluate("v => typeof window[v] === 'number' ? window[v] : null", s["animalVar"])
            s["animalType"] = animal_type
            paths = set()
            if animal_type is not None:
                args_js = [animal_type, s["species"], s["subSpecies"]]
                r = page.evaluate("([t, sp, sub]) => collectSkin(t, sp, sub)", args_js)
                paths.update(r["paths"])
                # some parts are only asked for once an earlier part has loaded: wait, then draw again
                try:
                    page.wait_for_load_state("networkidle", timeout=15000)
                except Exception:
                    pass
                r = page.evaluate("([t, sp, sub]) => collectSkin(t, sp, sub)", args_js)
                paths.update(r["paths"])
                s["drawErrors"] = r["errors"][:5]
            rels = {canonical(x, site) for x in paths}
            rels.add(canonical(s["shopImage"], site))
            s["requested"] = sorted(r for r in rels if r)
            print(f"  [{i}/{len(skins)}] {s['id']}: {len(s['requested'])} texture paths")
        browser.close()

    drawn = sorted({r for s in skins for r in s["requested"]})
    if args.list_only:
        for s in skins:
            s["drawn"] = s.pop("requested")
        write_report(out_dir, site, args.source, skins)
        print(f"Recorded {len(drawn)} texture paths. Wrote {out_dir / 'skins.json'}")
        return

    results = {}

    def download(paths, label):
        paths = [r for r in paths if r not in results]
        if not paths:
            return
        done = 0
        with concurrent.futures.ThreadPoolExecutor(max_workers=max(1, args.concurrency)) as pool:
            futures = {pool.submit(download_one, site, out_dir, rel, args.source): rel for rel in paths}
            for fut in concurrent.futures.as_completed(futures):
                results[futures[fut]] = fut.result()
                done += 1
                if done % 100 == 0 or done == len(paths):
                    print(f"  {label} {done}/{len(paths)}", end="\r", flush=True)
        print()

    found = lambda r: any(results.get(r, {}).values())

    print(f"Downloading {len(drawn)} drawn texture paths ...")
    download(drawn, "drawn")

    # Second pass: textures the client only uses in states the sweep cannot reach
    # (ability timers, older animation frames). Try names that other skins of the
    # same animal use, in each skin's folder.
    names = names_by_animal(r for r in drawn if found(r))
    if args.names_from:
        names_from_tree(Path(args.names_from), names)
    for s in skins:
        s["guessed"] = []
        dirs = {split_skin_path(r)[:2] for r in s["requested"] if split_skin_path(r)}
        cands = {f"{d}/{n}" for key, d in dirs for n in names.get(key, ())}
        s["_cands"] = sorted(cands - set(s["requested"]))
    guesses = sorted({c for s in skins for c in s["_cands"]})
    print(f"Trying {len(guesses)} more names other skins of the same animals use ...")
    download(guesses, "names")

    # Third pass: continue numbered runs (tongue0..tongue12, tail_1..tail_8).
    for round_no in range(40):
        nxt = set()
        for r in [r for r in results if found(r)]:
            m = re.match(r"^(.*?)(\d+)(\.\w+)$", r)
            if not m:
                continue
            stem, num, ext = m.groups()
            for k in (int(num) + 1, int(num) + 2, 0, 1):
                c = f"{stem}{k}{ext}"
                if c not in results:
                    nxt.add(c)
        if not nxt:
            break
        download(sorted(nxt), f"runs {round_no + 1}")

    extra = {r for r in results if found(r)} - set(drawn)
    for s in skins:
        dirs = {split_skin_path(r)[1] for r in s["requested"] if split_skin_path(r)}
        s["drawn"] = [r for r in s.pop("requested") if found(r)]
        s["guessed"] = sorted(r for r in extra if split_skin_path(r) and split_skin_path(r)[1] in dirs)
        s.pop("_cands", None)
        s["files"] = sorted(set(s["drawn"]) | set(s["guessed"]))
    write_report(out_dir, site, args.source, skins)
    drawn_found = sum(1 for r in drawn if found(r))
    print(f"Saved {drawn_found + len(extra)} textures for {len(skins)} skins to {out_dir}/: "
          f"{drawn_found} the client drew, {len(extra)} more found by name. Wrote {out_dir / 'skins.json'}")


# skins/<animal folders>/<species>[/<sub>]/<file>  ->  (animal key, skin folder, file)
_SKIN_PATH_RE = re.compile(r"^(skins/.+?)/(\d+(?:/\d+)?)/(.+)$")


def split_skin_path(rel):
    m = _SKIN_PATH_RE.match(rel)
    if m:
        key, num, name = m.groups()
        return key, f"{key}/{num}", name
    # skins kept straight in the animal's folder, e.g. skins/land/okapi/okapi.png
    if rel.startswith("skins/") and rel.count("/") >= 2:
        folder, name = rel.rsplit("/", 1)
        return folder, folder, name
    return None


def names_by_animal(paths, names=None):
    names = names if names is not None else {}
    for r in paths:
        sp = split_skin_path(r)
        if sp:
            names.setdefault(sp[0], set()).add(sp[2])
    return names


def names_from_tree(root, names):
    """Learn texture names from a local skins folder laid out like the site's (e.g. web/skins)."""
    if not root.is_dir():
        print(f"  --names-from {root} is not a folder; skipping")
        return
    base = root.name if root.name == "skins" else "skins"
    paths = []
    for f in root.rglob("*"):
        if f.is_file() and f.suffix.lower() in _IMAGE_EXT:
            paths.append(f"{base}/" + f.relative_to(root).as_posix())
    names_by_animal(paths, names)


def write_report(out_dir, site, source, skins):
    (out_dir / "skins.json").write_text(json.dumps({
        "site": site, "fetchedAt": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "source": source, "count": len(skins), "skins": skins,
    }, indent=2))

if __name__ == "__main__":
    main()
