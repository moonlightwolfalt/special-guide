// Milestones and the dragon skill tree (client side).
//
// The game server (io.mopesbox.Milestones) sends message type 120:
//   sub 1: the tree      {rings:[{ring,name,size,xp}], skills:[{id,ring,kind,name,lines,icon,cd?}]}
//   sub 2: player state  {animal,name,level,maxLevel,thresholds,next,tree,picks:[3],points,q:[{id,name,icon,cd,left}],milestones:[{level,title,image}]}
//   sub 3: milestone hit {animal,name,level,title,lines,image,xp}
// and reads type 120 back: 1 = send tree and state, 2 = pick (uint16 skill id), 3 = cast Q abilities.
//
// T opens the tree, Q casts the new abilities picked from it (client.js forwards both keys).
// There are two trees: "dragon" (3 rings: 45, 25, 10 skills) and "titan" (the 5m tier: 1 ring of 3).
//
// The dragons' milestone abilities are Wing Slash (Z), Wingspan (X) and Shoot Meteor (C): action 4 with
// the ability's index. King Dragons have all three. Black Dragons get one slot at milestone I and one at
// milestone II (2 at most) and choose what goes in it: action 5 with the index. On touch screens the
// abilities also get big buttons on the right (#msMobile). Their effects are game objects of type 175 (o_kingDragonFx) that client.js hands to drawFx() below,
// which plays the frames made by tools/kingdragon_fx/make_fx_frames.py.
(function () {
    'use strict';
    var MSG = 120;
    var trees = {};       // sub 1, by track
    var tree = null;      // the current animal's tree
    var state = null;     // sub 2
    var requested = {};
    var selected = -1;    // skill id shown in the details box
    var qReady = {};      // skill id -> time (ms) it can be cast again
    var kdReady = [0, 0, 0]; // dragon abilities Z/X/C -> time (ms) usable again
    var mobile;              // big touch buttons on the right
    var TOUCH = ('ontouchstart' in window) || navigator.maxTouchPoints > 0;

    // frame folders and counts: must match FRAMES in make_fx_frames.py
    var FX = [['wingslash', 8], ['wingspan', 10], ['meteor', 6], ['impact', 10]];
    var KD_ICON = { wingslash: 'img/kingdragon/wingslash/4.png', wingspan: 'img/kingdragon/wingspan/6.png', meteor: 'img/kingdragon/meteor/0.png' };
    var fxPreloaded = false;
    var hud, panel, toast, toastTimer;
    var SVGNS = 'http://www.w3.org/2000/svg';

    // ring geometry in the 1000x1000 tree drawing: outer ring 1 (45), middle 2 (25), inner 3 (10)
    var RING_R = { 1: 432, 2: 298, 3: 168 };
    var NODE_R = { 1: 24, 2: 29, 3: 36 };
    // the titan tree is one ring of 3 big nodes
    var TITAN_R = 280, TITAN_NODE = 82;

    function ringR(r) { return tree.rings.length === 1 ? TITAN_R : RING_R[r]; }
    function nodeR(r) { return tree.rings.length === 1 ? TITAN_NODE : NODE_R[r]; }

    function skillById(id) {
        if (!tree) return null;
        for (var i = 0; i < tree.skills.length; i++) if (tree.skills[i].id === id) return tree.skills[i];
        return null;
    }

    function send(action, id) {
        if (typeof MsgWriter !== 'function' || typeof wsSendMsg !== 'function') return;
        var m = new MsgWriter(action === 2 ? 4 : action >= 4 ? 3 : 2);
        m.writeUInt8(MSG);
        m.writeUInt8(action);
        if (action === 2) m.writeUInt16(id);
        if (action >= 4) m.writeUInt8(id);
        wsSendMsg(m);
    }

    // ---------------------------------------------------------------- King Dragon effects
    function preloadFx() {
        if (fxPreloaded) return;
        fxPreloaded = true;
        FX.forEach(function (f) {
            for (var i = 0; i < f[1]; i++) new Image().src = 'img/kingdragon/' + f[0] + '/' + i + '.png';
        });
    }

    // Draws an effect object; ctx is already at the object's centre. loadImg is client.js's image cache.
    function drawFx(ctx, obj, elapsed, loadImg) {
        var f = FX[obj.fxKind];
        if (!f) return;
        var frame = obj.fxKind === 2
            ? Math.floor(elapsed / 70) % f[1]                                    // the meteor loops
            : Math.min(f[1] - 1, Math.floor(elapsed / Math.max(1, obj.fxLife / f[1])));
        var img = loadImg('img/kingdragon/' + f[0] + '/' + Math.max(0, frame) + '.png');
        if (!img) return;
        var r = obj.rad;
        ctx.save();
        ctx.rotate(obj.fxAngle || 0);
        ctx.drawImage(img, -r, -r, 2 * r, 2 * r);
        ctx.restore();
    }

    function hasAbility(i) {
        return !!state && (state.da || []).some(function (a) { return a.i === i; });
    }

    // Wing Slash (0), Wingspan (1), Shoot Meteor (2), for dragons that have it
    function castKD(i) {
        if (i == null || !hasAbility(i)) return;
        if ((kdReady[i] || 0) > Date.now()) return;
        send(4, i);
    }

    // Black Dragon: put ability i in an open slot
    function chooseAbility(i) {
        if (!state || !(state.daChoose || []).some(function (a) { return a.i === i; })) return;
        send(5, i);
    }

    function fmtXP(xp) {
        var m = xp / 1e6;
        return (Math.round(m * 100) / 100) + 'm';
    }

    function esc(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    // ---------------------------------------------------------------- glyphs
    // Small white icons drawn inside each node, on a -10..10 box.
    var GLYPH = {
        volley: '<path d="M-7 4 l3-9 l3 9z M-1.5 2 l3-10 l3 10z M4 4 l3-9 l3 9z" transform="translate(-1.5 2)"/>',
        flame: '<path d="M0-9 C3-4 7-1 6 4 C5 8 -5 8 -6 4 C-7 0 -2-3 0-9z"/>',
        burn: '<path d="M-5-8 C-2-4 0-3 -1 1 C2-1 2-4 1-7 C6-3 8 2 5 6 C2 9 -4 9 -6 5 C-8 1 -7-4 -5-8z"/>',
        recharge: '<path d="M0-8 A8 8 0 1 1 -8 0 L-5 0 A5 5 0 1 0 0-5 L0-2 L-5-6.5 L0-11z"/>',
        lava: '<path d="M0-9 C4-3 7 1 7 4 A7 6 0 0 1 -7 4 C-7 1 -4-3 0-9z"/>',
        bite: '<path d="M-8-6 L8-6 L5 0 L2-4 L0 1 L-2-4 L-5 0z M-8 7 L8 7 L5 1 L2 5 L0 0 L-2 5 L-5 1z"/>',
        heart: '<path d="M0 8 C-9 2 -9-6 -4-7 C-2-7.5 -0.5-6 0-4.5 C0.5-6 2-7.5 4-7 C9-6 9 2 0 8z"/>',
        nova: '<circle r="3.2"/><path d="M0-10 l1.6 4.5h-3.2z M0 10 l1.6-4.5h-3.2z M-10 0 l4.5 1.6v-3.2z M10 0 l-4.5 1.6v-3.2z M-7-7 l4.2 2 l-2 2.2z M7 7 l-4.2-2 l2-2.2z M7-7 l-2 4.2 l-2.2-2z M-7 7 l2-4.2 l2.2 2z"/>',
        rear: '<path d="M8-6 L-2-6 L-2-9 L-9 0 L-2 9 L-2 6 L8 6z"/>',
        field: '<circle r="8" fill="none" stroke="#fff" stroke-width="2.6" stroke-dasharray="3.5 2.6"/><path d="M0-4 C2-1 3 0 3 2 A3 3 0 0 1 -3 2 C-3 0 -2-1 0-4z"/>',
        gust: '<path d="M-9-4 H4 A3 3 0 1 0 1-7 M-9 1 H7 A3 3 0 1 1 4 4 M-9 6 H0" fill="none" stroke="#fff" stroke-width="2.4" stroke-linecap="round"/>',
        dash: '<path d="M-9-7 L1 0 L-9 7z M-1-7 L9 0 L-1 7z"/>',
        shield: '<path d="M0-9 L8-6 C8 2 5 6 0 9 C-5 6 -8 2 -8-6z"/>',
        heal: '<path d="M-3-8 H3 V-3 H8 V3 H3 V8 H-3 V3 H-8 V-3 H-3z"/>'
    };

    function el(tag, attrs, parent) {
        var e = document.createElementNS(SVGNS, tag);
        for (var k in attrs) e.setAttribute(k, attrs[k]);
        if (parent) parent.appendChild(e);
        return e;
    }

    // ---------------------------------------------------------------- DOM
    function build() {
        if (hud) return;
        hud = document.createElement('div');
        hud.id = 'msHud';
        hud.style.display = 'none';
        document.body.appendChild(hud);

        panel = document.createElement('div');
        panel.id = 'skillTreePanel';
        panel.style.display = 'none';
        panel.innerHTML =
            '<div class="st-card" role="dialog" aria-label="Skill tree">' +
            '<div class="st-head"><div><div class="st-title" id="stTitle">Skill Tree</div>' +
            '<div class="st-sub" id="stSub"></div></div>' +
            '<button class="st-close" id="stClose" aria-label="Close">&times;</button></div>' +
            '<div class="st-body"><div class="st-wheel" id="stWheel"></div>' +
            '<div class="st-side"><div class="st-legend">' +
            '<span><i class="st-dot rework"></i>Ability rework (always on)</span>' +
            '<span><i class="st-dot new"></i>New ability (press Q)</span></div>' +
            '<div class="st-info" id="stInfo"></div>' +
            '<div class="st-badges" id="stBadges"></div></div></div></div>';
        document.body.appendChild(panel);
        panel.addEventListener('mousedown', function (e) {
            if (e.target === panel) close();
        });
        document.getElementById('stClose').onclick = close;

        toast = document.createElement('div');
        toast.id = 'msToast';
        document.body.appendChild(toast);

        mobile = document.createElement('div');
        mobile.id = 'msMobile';
        mobile.style.display = 'none';
        document.body.appendChild(mobile);
        if (TOUCH) document.body.classList.add('ms-touch');

        document.addEventListener('keydown', function (e) {
            if (e.keyCode === 27 && isOpen()) {
                e.preventDefault();
                close();
            }
        });
        setInterval(renderQ, 200);
    }

    function isOpen() {
        return panel && panel.style.display !== 'none';
    }

    function close() {
        if (panel) panel.style.display = 'none';
    }

    function alive() {
        return typeof isAliveInGame === 'undefined' || !!isAliveInGame;
    }

    // ---------------------------------------------------------------- HUD (badges, tree button, Q bar)
    function renderHud() {
        build();
        if (!state || !state.animal || !alive()) {
            hud.style.display = 'none';
            mobile.style.display = 'none';
            close();
            return;
        }
        hud.style.display = '';
        var h = '<div class="ms-row">';
        for (var i = 1; i <= state.maxLevel; i++) {
            var got = state.milestones[i - 1];
            h += got
                ? '<img class="ms-badge" src="' + esc(got.image) + '" alt="" title="' + esc(got.title) + '">'
                : '<span class="ms-badge locked" title="Milestone at ' + fmtXP(state.thresholds[i - 1]) + ' XP"></span>';
        }
        h += '<div class="ms-text"><b>' + esc(state.name) + '</b><br>' +
            (state.next ? 'Next milestone: ' + fmtXP(state.next) + ' XP' : 'All milestones reached') + '</div></div>';
        if (state.tree) {
            h += '<button class="ms-tree-btn" id="msTreeBtn">' + (state.track === 'titan' ? 'Titan skills' : 'Skill tree') + ' <kbd>T</kbd>' +
                (state.points > 0 ? '<span class="ms-points">' + state.points + '</span>' : '') + '</button>';
            if (state.q.length) {
                h += '<div class="ms-q" id="msQ">';
                for (var j = 0; j < state.q.length; j++) {
                    var q = state.q[j];
                    h += '<button class="ms-q-btn" data-id="' + q.id + '" title="' + esc(q.name) + ' (Q)">' +
                        '<svg viewBox="-12 -12 24 24"><g fill="#fff">' + (GLYPH[q.icon] || GLYPH.nova) + '</g></svg>' +
                        '<span class="ms-q-cd"></span></button>';
                }
                h += '<kbd class="ms-q-key">Q</kbd></div>';
            }
        }
        var da = state.da || [], daChoose = state.daChoose || [];
        if (da.length || daChoose.length) preloadFx();
        if (da.length) {
            h += '<div class="ms-q ms-kd">';
            da.forEach(function (k) {
                h += '<button class="ms-q-btn ms-kd-btn" data-kd="' + k.i + '" title="' + esc(k.name) + ' (' + k.key + ')">' +
                    '<img src="' + KD_ICON[k.icon] + '" alt=""><span class="ms-q-cd"></span><kbd>' + k.key + '</kbd></button>';
            });
            h += '</div>';
        }
        if (daChoose.length) {
            // Black Dragon: a milestone opened an ability slot
            h += '<div class="ms-choose"><div class="ms-choose-title">Choose a milestone ability (' +
                (state.daUsed || 0) + '/' + (state.daMax || 2) + ')</div>';
            daChoose.forEach(function (k) {
                h += '<button class="ms-choose-btn" data-choose="' + k.i + '"><img src="' + KD_ICON[k.icon] + '" alt="">' +
                    '<span>' + esc(k.name) + '<small>key ' + k.key + '</small></span></button>';
            });
            h += '</div>';
        }
        hud.innerHTML = h;
        var kds = hud.querySelectorAll('.ms-kd-btn');
        for (var kk = 0; kk < kds.length; kk++) kds[kk].onclick = function () { castKD(+this.getAttribute('data-kd')); };
        var chs = hud.querySelectorAll('.ms-choose-btn');
        for (var ch = 0; ch < chs.length; ch++) chs[ch].onclick = function () {
            this.disabled = true;
            chooseAbility(+this.getAttribute('data-choose'));
        };
        renderMobile();
        var btn = document.getElementById('msTreeBtn');
        if (btn) btn.onclick = function () { toggle(); };
        var qs = hud.querySelectorAll('.ms-q-btn:not(.ms-kd-btn)');
        for (var k = 0; k < qs.length; k++) qs[k].onclick = cast;
        renderQ();
    }

    // big touch buttons on the right for the dragon abilities and the Q abilities
    function renderMobile() {
        if (!TOUCH) return;
        var da = state.da || [], q = state.q || [];
        if (!da.length && !q.length) {
            mobile.style.display = 'none';
            return;
        }
        mobile.style.display = '';
        var h = '';
        da.forEach(function (k) {
            h += '<button class="ms-q-btn ms-kd-btn ms-big" data-kd="' + k.i + '" aria-label="' + esc(k.name) + '">' +
                '<img src="' + KD_ICON[k.icon] + '" alt=""><span class="ms-q-cd"></span><span class="ms-big-name">' + esc(k.name) + '</span></button>';
        });
        if (q.length) {
            h += '<button class="ms-q-btn ms-big ms-big-q" data-id="' + q[0].id + '" aria-label="Q abilities">' +
                '<svg viewBox="-12 -12 24 24"><g fill="#fff">' + (GLYPH[q[0].icon] || GLYPH.nova) + '</g></svg>' +
                '<span class="ms-q-cd"></span><span class="ms-big-name">Q</span></button>';
        }
        mobile.innerHTML = h;
        var bs = mobile.querySelectorAll('button');
        for (var i = 0; i < bs.length; i++) {
            var tap = function (e) {
                // keep the touch away from the joystick and the game canvas
                e.preventDefault();
                e.stopPropagation();
                var kd = this.getAttribute('data-kd');
                if (kd != null) castKD(+kd); else cast();
            };
            bs[i].addEventListener('touchstart', tap, { passive: false });
            bs[i].addEventListener('click', tap);
        }
    }

    function renderQ() {
        if (!hud || !state || !state.q) return;
        var now = Date.now();
        var qs = document.querySelectorAll('#msHud .ms-q-btn, #msMobile .ms-q-btn');
        for (var i = 0; i < qs.length; i++) {
            var id = qs[i].getAttribute('data-id');
            var kd = qs[i].getAttribute('data-kd');
            var left = Math.max(0, (kd != null ? kdReady[kd] : qReady[id] || 0) - now);
            var cd = qs[i].querySelector('.ms-q-cd');
            cd.textContent = left > 0 ? Math.ceil(left / 1000) : '';
            qs[i].classList.toggle('cooling', left > 0);
        }
    }

    // ---------------------------------------------------------------- the circular tree
    function nodeAngle(skill, index, count) {
        // evenly round the ring, clockwise from the top (see ringSkills for the order)
        return -90 + (index + 0.5) * 360 / count;
    }

    function renderTree() {
        build();
        var wheel = document.getElementById('stWheel');
        var sub = document.getElementById('stSub');
        if (!tree || !state) {
            wheel.innerHTML = '<div class="st-loading">Loading...</div>';
            return;
        }
        var lvl = state.level;
        var nRings = tree.rings.length;
        var rings = [];
        for (var ri = 1; ri <= nRings; ri++) rings.push(ri);
        document.getElementById('stTitle').textContent = tree.title || 'Skill Tree';
        sub.innerHTML = esc(state.name) + ' &middot; ' +
            (state.points > 0 ? '<b class="st-pts">' + state.points + ' pick' + (state.points > 1 ? 's' : '') + ' ready</b>'
                : (lvl < nRings ? (nRings > 1 ? 'Next ring opens' : 'Opens') + ' at ' + fmtXP(tree.rings[lvl].xp) + ' XP' : 'Tree complete'));

        wheel.innerHTML = '';
        var svg = el('svg', { viewBox: '0 0 1000 1000', class: 'st-svg' }, wheel);
        var defs = el('defs', {}, svg);
        var glow = el('filter', { id: 'stGlow', x: '-50%', y: '-50%', width: '200%', height: '200%' }, defs);
        el('feGaussianBlur', { stdDeviation: '6', result: 'b' }, glow);
        var mg = el('feMerge', {}, glow);
        el('feMergeNode', { in: 'b' }, mg);
        el('feMergeNode', { in: 'SourceGraphic' }, mg);
        var g = el('g', { transform: 'translate(500 500)' }, svg);

        // ring bands: left half rework (orange), right half new ability (blue)
        rings.forEach(function (r) {
            var open = lvl >= r;
            var R = ringR(r), w = nodeR(r) * 2 + 20;
            el('circle', { r: R, fill: 'none', stroke: open ? 'rgba(255,255,255,.10)' : 'rgba(255,255,255,.04)', 'stroke-width': w }, g);
            el('circle', { r: R + w / 2, fill: 'none', stroke: 'rgba(255,255,255,.08)', 'stroke-width': 2 }, g);
        });
        // spokes between the halves
        if (nRings > 1) el('line', { x1: 0, y1: -500, x2: 0, y2: 500, stroke: 'rgba(255,255,255,.10)', 'stroke-width': 2, 'stroke-dasharray': '6 8' }, g);

        // links from each pick to the next ring's pick
        var pickPos = [];
        rings.forEach(function (r) {
            var skills = ringSkills(r);
            skills.forEach(function (s, i) {
                if (state.picks[r - 1] === s.id) {
                    var a = nodeAngle(s, i, skills.length) * Math.PI / 180;
                    pickPos.push([Math.cos(a) * ringR(r), Math.sin(a) * ringR(r)]);
                }
            });
        });
        for (var p = 1; p < pickPos.length; p++)
            el('line', { x1: pickPos[p - 1][0], y1: pickPos[p - 1][1], x2: pickPos[p][0], y2: pickPos[p][1], stroke: '#ffd23f', 'stroke-width': 5, 'stroke-linecap': 'round', opacity: 0.8 }, g);

        rings.forEach(function (r) {
            var skills = ringSkills(r);
            var open = lvl >= r;
            var picked = state.picks[r - 1];
            skills.forEach(function (s, i) {
                var a = nodeAngle(s, i, skills.length) * Math.PI / 180;
                var x = Math.cos(a) * ringR(r), y = Math.sin(a) * ringR(r);
                var st = picked === s.id ? 'picked' : picked !== -1 ? 'passed' : open ? 'open' : 'locked';
                var node = el('g', { transform: 'translate(' + x.toFixed(1) + ' ' + y.toFixed(1) + ')', class: 'st-node ' + s.kind + ' ' + st + (selected === s.id ? ' sel' : ''), tabindex: 0 }, g);
                el('circle', { r: nodeR(r), class: 'st-node-bg', filter: st === 'open' || st === 'picked' ? 'url(#stGlow)' : '' }, node);
                var sc = nodeR(r) / 13;
                var gl = el('g', { transform: 'scale(' + sc.toFixed(2) + ')', fill: '#fff' }, node);
                gl.innerHTML = GLYPH[s.icon] || GLYPH.flame;
                var t = el('title', {}, node);
                t.textContent = s.name;
                node.addEventListener('click', function () { select(s.id); });
                node.addEventListener('keydown', function (e) {
                    if (e.keyCode === 13 || e.keyCode === 32) { e.preventDefault(); select(s.id); }
                });
            });
            if (nRings === 1) {
                // few big nodes: write each skill's name under it
                skills.forEach(function (s, i) {
                    var a = nodeAngle(s, i, skills.length) * Math.PI / 180;
                    var t = el('text', { x: (Math.cos(a) * ringR(r)).toFixed(1), y: (Math.sin(a) * ringR(r) + nodeR(r) + 36).toFixed(1), class: 'st-node-name' }, g);
                    t.textContent = s.name;
                });
                return;
            }
            // ring label at the top
            var lab = el('text', { x: 0, y: -ringR(r) - nodeR(r) - 16, class: 'st-ring-label' + (open ? ' open' : '') }, g);
            lab.textContent = tree.rings[r - 1].name.replace('Ring ', '') + (open ? '' : ' · ' + fmtXP(tree.rings[r - 1].xp));
        });

        // centre
        el('circle', { r: 92, fill: 'rgba(0,0,0,.35)', stroke: 'rgba(255,255,255,.15)', 'stroke-width': 3 }, g);
        var c1 = el('text', { y: -8, class: 'st-center-big' }, g);
        c1.textContent = state.points > 0 ? state.points : '✓';
        var c2 = el('text', { y: 30, class: 'st-center-small' }, g);
        c2.textContent = state.points > 0 ? (state.points > 1 ? 'picks ready' : 'pick ready') : (lvl < nRings ? 'all picked' : 'complete');

        renderInfo();
        renderBadges();
    }

    // a ring's skills, reworks first (left half), then new abilities (right half)
    function ringSkills(r) {
        var rw = [], nw = [];
        tree.skills.forEach(function (s) {
            if (s.ring !== r) return;
            (s.kind === 'rework' ? rw : nw).push(s);
        });
        // new abilities go clockwise from the top (right half), reworks counter-clockwise from the top (left half)
        return nw.concat(rw.reverse());
    }

    function select(id) {
        selected = id;
        renderTree();
    }

    function renderInfo() {
        var box = document.getElementById('stInfo');
        var s = selected >= 0 ? skillById(selected) : null;
        if (!s) {
            box.innerHTML = tree.rings.length === 1
                ? '<p class="st-hint">Pick one of these 3 skills at your 7.25m milestone. Your pick lasts until you die.</p>'
                : '<p class="st-hint">Pick one skill from each ring. Ring I opens at your first milestone, ' +
                'and each milestone after opens the next ring. Your picks last until you die.</p>';
            return;
        }
        var r = s.ring, lvl = state.level, picked = state.picks[r - 1];
        var h = '<div class="st-info-name ' + s.kind + '">' + esc(s.name) + '</div>' +
            '<div class="st-info-kind">' + (s.kind === 'rework' ? 'Ability rework' : 'New ability') + ' &middot; ' + esc(tree.rings[r - 1].name) + '</div><ul>';
        s.lines.forEach(function (l) { h += '<li>' + esc(l) + '</li>'; });
        h += '</ul>';
        if (picked === s.id) h += '<div class="st-status ok">Learned</div>';
        else if (picked !== -1) h += '<div class="st-status">You already picked ' + esc((skillById(picked) || {}).name || '') + (tree.rings.length > 1 ? ' from this ring' : '') + '</div>';
        else if (lvl < r) h += '<div class="st-status">Opens at ' + fmtXP(tree.rings[r - 1].xp) + ' XP</div>';
        else h += '<button class="st-learn" id="stLearn">Learn ' + esc(s.name) + '</button>';
        box.innerHTML = h;
        var b = document.getElementById('stLearn');
        if (b) b.onclick = function () {
            b.disabled = true;
            send(2, s.id);
        };
    }

    function renderBadges() {
        var box = document.getElementById('stBadges');
        if (!state.milestones.length) {
            box.innerHTML = '';
            return;
        }
        var h = '<div class="st-badges-title">Milestones</div>';
        state.milestones.forEach(function (m) {
            h += '<div class="st-badge"><img src="' + esc(m.image) + '" alt=""><span>' + esc(m.title) + '</span></div>';
        });
        box.innerHTML = h;
    }

    function toggle() {
        build();
        if (!state || !state.tree) return;
        if (isOpen()) {
            close();
            return;
        }
        panel.style.display = '';
        if (!tree) send(1);
        selected = -1;
        renderTree();
    }

    function cast() {
        if (!state || !state.q || !state.q.length) return;
        send(3);
    }

    // ---------------------------------------------------------------- milestone popup
    function showToast(m) {
        build();
        var h = '<img src="' + esc(m.image) + '" alt=""><div><div class="ms-toast-kicker">' + esc(m.name) +
            ' milestone &middot; ' + fmtXP(m.xp) + ' XP</div><div class="ms-toast-title">' + esc(m.title) + '</div><ul>';
        m.lines.forEach(function (l) { h += '<li>' + esc(l) + '</li>'; });
        toast.innerHTML = h + '</ul></div>';
        toast.classList.remove('show');
        void toast.offsetWidth;
        toast.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(function () { toast.classList.remove('show'); }, 7000);
    }

    // ---------------------------------------------------------------- messages
    function onMessage(msg) {
        var sub = msg.readUInt8();
        var data;
        try {
            data = JSON.parse(msg.readString());
        } catch (e) {
            return;
        }
        if (sub === 1) {
            trees[data.track] = data;
            if (state && state.track === data.track) tree = data;
        } else if (sub === 2) {
            if (!state || state.track !== data.track) selected = -1;
            state = data;
            tree = trees[state.track] || null;
            var now = Date.now();
            qReady = {};
            (state.q || []).forEach(function (q) { qReady[q.id] = now + q.left; });
            kdReady = [0, 0, 0];
            (state.da || []).forEach(function (k) { kdReady[k.i] = now + k.left; });
            if (state.tree && !tree && !requested[state.track]) {
                requested[state.track] = true;
                send(1);
            }
            if (!state.animal) selected = -1;
        } else if (sub === 3) {
            showToast(data);
            return;
        }
        renderHud();
        if (isOpen()) renderTree();
    }

    window.SkillTree = { onMessage: onMessage, toggle: toggle, cast: cast, close: close, castKD: castKD, drawFx: drawFx };

    // hide everything when the player dies or leaves
    setInterval(function () {
        if (hud && (hud.style.display !== 'none' || mobile.style.display !== 'none') && !alive()) {
            state = null;
            renderHud();
        }
    }, 500);
})();
