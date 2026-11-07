// Measures how fast the v1.mope.io server turns animals, so this server can match it.
//
// How to use (in your own browser, playing normally):
//   1. Open https://v1.mope.io and stay on the menu (don't press Play yet).
//   2. Open the developer console (F12 -> Console), paste this whole file, press Enter.
//   3. Press Play. Hold the mouse far from your animal and move it in big, fast circles
//      for about 10 seconds, so the animal is always turning as fast as it can.
//   4. Type  v1TurnReport()  in the console and press Enter. Copy the table it prints.
//   5. Repeat with animals of other tiers (each time: reload, paste, play, report),
//      for example mouse, a middle tier and a top tier, and note which animal each was.
//
// It only watches the angles the game already receives from the server; it sends nothing.
// It works by noticing every time the game stores an object's target angle (nAngle).
(() => {
    if (window.v1TurnReport) return console.log('Already installed. Play, circle the mouse, then run v1TurnReport().');
    const targets = new WeakMap();
    const stats = [];
    const statsOf = new WeakMap();
    const TWO_PI = Math.PI * 2;

    Object.defineProperty(Object.prototype, 'nAngle', {
        configurable: true,
        get() { return targets.get(this); },
        set(value) {
            const prev = targets.get(this);
            targets.set(this, value);
            if (typeof value !== 'number' || typeof prev !== 'number') return;
            const now = performance.now();
            let s = statsOf.get(this);
            if (!s) {
                s = { obj: this, updates: 0, last: now, steps: [], gaps: [] };
                statsOf.set(this, s);
                stats.push(s);
            }
            // v1 keeps angles in radians; the shortest way round between two angles
            let d = Math.abs(value - prev) % TWO_PI;
            if (d > Math.PI) d = TWO_PI - d;
            s.gaps.push(now - s.last);
            s.last = now;
            s.updates++;
            if (d > 1e-6) s.steps.push(d * 180 / Math.PI);
        },
    });

    const quantile = (a, q) => {
        if (!a.length) return 0;
        const b = [...a].sort((x, y) => x - y);
        return b[Math.min(b.length - 1, Math.floor(q * b.length))];
    };

    window.v1TurnReport = () => {
        const rows = stats
            .filter(s => s.steps.length >= 20)
            .sort((a, b) => b.steps.length - a.steps.length)
            .slice(0, 8)
            .map(s => {
                const tickMs = quantile(s.gaps, 0.5);
                // turning flat out, every update moves by the server's limit
                const maxStep = quantile(s.steps, 0.95);
                return {
                    animalType: s.obj.animalType,
                    species: s.obj.animalSpecies,
                    radius: Math.round(s.obj.rad || 0),
                    updates: s.updates,
                    'ms per update': +tickMs.toFixed(1),
                    'max degrees per update': +maxStep.toFixed(2),
                    'max degrees per second': tickMs ? +(maxStep * 1000 / tickMs).toFixed(1) : null,
                };
            });
        console.log('Your own animal is usually the row with the most updates and the steadiest maximum.');
        console.table(rows);
        return rows;
    };
    console.log('Installed. Press Play, move the mouse in big fast circles for ~10 s, then run v1TurnReport().');
})();
