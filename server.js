#!/usr/bin/env node
// Starts every part of the game with one command:
//   master server  (backup_master/masterserver)    -> http://localhost:14001
//   account server (backup_account/accountserver)  -> http://localhost:14002
//   game server    (backup_server/server, Java)    -> ws://localhost:8080
//
// Everything is reachable through one public port (PORT, else WEB_PORT, else 8000):
//   /             -> the web client (web/)
//   /api/account  -> account server
//   /api/master   -> master server
//   /game         -> game server (WebSocket)
// so the game runs behind a single address, as hosts like Sevalla require.
//
// Usage: node server.js
// Settings come from environment variables (see README.md).

const { spawn, spawnSync } = require('child_process');
const fs = require('fs');
const http = require('http');
const net = require('net');
const path = require('path');

const ROOT = __dirname;
const MASTER_DIR = path.join(ROOT, 'backup_master', 'masterserver');
const ACCOUNT_DIR = path.join(ROOT, 'backup_account', 'accountserver');
const GAME_DIR = path.join(ROOT, 'backup_server', 'server');
const GAME_JAR = path.join(GAME_DIR, 'target', 'MopeSBoxServer-1.0-SNAPSHOT.jar');
const WEB_DIR = path.join(ROOT, 'web');
const WEB_PORT = Number(process.env.PORT) || Number(process.env.WEB_PORT) || 8000;

// Internal servers that the public port forwards to.
const ACCOUNT_PORT = 14002;
const MASTER_PORT = 14001;
const GAME_PORT = Number(process.env.GAME_PORT) || 8080;
const HTTP_ROUTES = { '/api/account': ACCOUNT_PORT, '/api/master': MASTER_PORT };
const GAME_PATH = '/game';

const children = [];
let shuttingDown = false;

function log(name, text) {
    for (const line of String(text).split('\n')) {
        if (line.trim()) console.log(`[${name}] ${line}`);
    }
}

// Run a setup step to completion (npm install, mvn package) before starting anything.
function runSetup(name, cmd, args, cwd) {
    log(name, `${cmd} ${args.join(' ')}`);
    const result = spawnSync(cmd, args, { cwd, stdio: 'inherit' });
    if (result.status !== 0) {
        log(name, `setup failed (${cmd} exited with ${result.error ? result.error.message : result.status})`);
        process.exit(1);
    }
}

function ensureNodeModules(name, dir) {
    if (!fs.existsSync(path.join(dir, 'node_modules'))) {
        runSetup(name, 'npm', ['install', '--no-audit', '--no-fund'], dir);
    }
}

function ensureGameJar() {
    if (!fs.existsSync(GAME_JAR) || process.env.REBUILD_GAME === '1') {
        runSetup('game', 'mvn', ['-q', 'clean', 'package', '-DskipTests'], GAME_DIR);
    }
}

function start(name, cmd, args, cwd) {
    const child = spawn(cmd, args, { cwd, env: process.env });
    child.stdout.on('data', (d) => log(name, d));
    child.stderr.on('data', (d) => log(name, d));
    child.on('error', (err) => log(name, `failed to start: ${err.message}`));
    child.on('exit', (code, signal) => {
        if (!shuttingDown) log(name, `stopped (${signal || 'exit code ' + code})`);
    });
    children.push(child);
    return child;
}

const MIME = {
    '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8', '.json': 'application/json', '.map': 'application/json', '.txt': 'text/plain; charset=utf-8',
    '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.gif': 'image/gif',
    '.svg': 'image/svg+xml', '.ico': 'image/x-icon', '.webp': 'image/webp',
    '.mp3': 'audio/mpeg', '.ogg': 'audio/ogg', '.wav': 'audio/wav',
    '.woff': 'font/woff', '.woff2': 'font/woff2', '.ttf': 'font/ttf',
};

// Client settings, built from environment variables so nothing is hardcoded in web/.
function clientConfig() {
    const config = {
        googleClientId: process.env.GOOGLE_CLIENT_ID || '',
        accountServerUrl: process.env.ACCOUNT_SERVER_URL || '/api/account',
        masterServerUrl: process.env.MASTER_SERVER_URL || '/api/master',
        // The default server list reaches the game through this page's address at this path.
        gamePath: GAME_PATH,
        // JSON list of {name, host, port, ssl, region, path}; empty means the default list.
        gameServers: [],
    };
    if (process.env.GAME_SERVERS) {
        try {
            config.gameServers = JSON.parse(process.env.GAME_SERVERS);
        } catch (err) {
            log('web', `GAME_SERVERS is not valid JSON: ${err.message}`);
        }
    }
    return `window.MOPE_CONFIG = ${JSON.stringify(config)};\n`;
}

// Which internal server a request path belongs to, and the path to send it.
function matchRoute(url) {
    for (const prefix of Object.keys(HTTP_ROUTES)) {
        if (url === prefix || url.startsWith(prefix + '/') || url.startsWith(prefix + '?')) {
            const rest = url.slice(prefix.length);
            return { port: HTTP_ROUTES[prefix], path: rest.startsWith('/') ? rest : '/' + rest };
        }
    }
    return null;
}

function proxyHttp(req, res, route) {
    const upstream = http.request({
        host: '127.0.0.1',
        port: route.port,
        method: req.method,
        path: route.path,
        headers: { ...req.headers, host: `localhost:${route.port}` },
    }, (up) => {
        res.writeHead(up.statusCode, up.headers);
        up.pipe(res);
    });
    upstream.on('error', (err) => {
        if (!res.headersSent) res.writeHead(502, { 'Content-Type': 'text/plain' });
        res.end(`Server unavailable: ${err.message}`);
    });
    req.pipe(upstream);
}

// Forward a WebSocket upgrade on /game to the game server as raw TCP.
function proxyGameSocket(req, socket, head) {
    const upstream = net.connect(GAME_PORT, '127.0.0.1', () => {
        const lines = [`${req.method} / HTTP/${req.httpVersion}`];
        for (let i = 0; i < req.rawHeaders.length; i += 2) {
            const name = req.rawHeaders[i];
            const value = name.toLowerCase() === 'host' ? `localhost:${GAME_PORT}` : req.rawHeaders[i + 1];
            lines.push(`${name}: ${value}`);
        }
        upstream.write(lines.join('\r\n') + '\r\n\r\n');
        if (head && head.length) upstream.write(head);
        upstream.pipe(socket);
        socket.pipe(upstream);
    });
    const close = () => { socket.destroy(); upstream.destroy(); };
    upstream.on('error', close);
    socket.on('error', close);
    upstream.on('close', () => socket.destroy());
    socket.on('close', () => upstream.destroy());
}

// Public server: the web client plus the forwarded routes.
function startWebServer() {
    const server = http.createServer((req, res) => {
        const route = matchRoute(req.url);
        if (route) {
            proxyHttp(req, res, route);
            return;
        }
        let urlPath;
        try {
            urlPath = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
        } catch {
            res.writeHead(400).end('Bad request');
            return;
        }
        if (urlPath === '/config.js') {
            res.writeHead(200, { 'Content-Type': 'text/javascript; charset=utf-8', 'Cache-Control': 'no-store' });
            res.end(clientConfig());
            return;
        }
        let file = path.normalize(path.join(WEB_DIR, urlPath));
        if (file !== WEB_DIR && !file.startsWith(WEB_DIR + path.sep)) {
            res.writeHead(403).end('Forbidden');
            return;
        }
        if (fs.existsSync(file) && fs.statSync(file).isDirectory()) file = path.join(file, 'index.html');
        fs.readFile(file, (err, data) => {
            if (err) {
                res.writeHead(404, { 'Content-Type': 'text/html; charset=utf-8' });
                fs.createReadStream(path.join(WEB_DIR, '404.html')).on('error', () => res.end('Not found')).pipe(res);
                return;
            }
            res.writeHead(200, { 'Content-Type': MIME[path.extname(file).toLowerCase()] || 'application/octet-stream' });
            res.end(data);
        });
    });
    server.on('upgrade', (req, socket, head) => {
        const urlPath = req.url.split('?')[0];
        if (urlPath === GAME_PATH || urlPath === GAME_PATH + '/') proxyGameSocket(req, socket, head);
        else socket.destroy();
    });
    server.on('error', (err) => {
        log('web', err.code === 'EADDRINUSE'
            ? `port ${WEB_PORT} is already in use (is another copy of server.js running?)`
            : `server error: ${err.message}`);
        shutdown(1);
    });
    server.listen(WEB_PORT, '0.0.0.0', () => log('web', `game ready at http://localhost:${WEB_PORT}`));
    return server;
}

function killChildren(signal) {
    for (const child of children) {
        if (child.exitCode === null && child.signalCode === null) child.kill(signal);
    }
}

function shutdown(code = 0) {
    if (shuttingDown) return;
    shuttingDown = true;
    console.log('\nStopping all servers...');
    killChildren('SIGTERM');
    // Give the servers a moment to exit, then force-stop anything still running.
    setTimeout(() => {
        killChildren('SIGKILL');
        process.exit(code);
    }, 3000);
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));
process.on('SIGHUP', () => shutdown(0));
// Never leave servers running behind if the launcher itself crashes.
process.on('uncaughtException', (err) => {
    console.error(err);
    shutdown(1);
});
process.on('exit', () => killChildren('SIGKILL'));

ensureNodeModules('master', MASTER_DIR);
ensureNodeModules('account', ACCOUNT_DIR);
ensureGameJar();

start('master', process.execPath, ['index.js'], MASTER_DIR);
start('account', process.execPath, ['index.js'], ACCOUNT_DIR);
start('game', 'java', ['-Dfile.encoding=UTF-8', '-jar', GAME_JAR], GAME_DIR);
startWebServer();
