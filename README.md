# special-guide

Mope 2020 server dump: web client, master server, account server and Java game server.

## Run everything

Needs Node.js 18+, Java 17+, Maven, and a MySQL database created from `users_structure.sql`.

```
node server.js
```

On the first run this installs the Node packages and builds the game server jar. It then starts:

| Part | Folder | Address |
|------|--------|---------|
| Web client | `web/` | http://localhost:8000 |
| Master server | `backup_master/masterserver` | http://localhost:14001 |
| Account server | `backup_account/accountserver` | http://localhost:14002 |
| Game server | `backup_server/server` | port 8080 (set in `config.txt`) |

Press Ctrl+C to stop all of them. Set `REBUILD_GAME=1` to rebuild the game server jar.

## Settings (environment variables)

| Variable | Used by | Default |
|----------|---------|---------|
| `WEB_PORT` | web client | `8000` |
| `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME` | account server | `127.0.0.1`, `root`, empty, `mope` |
| `SERVER_KEY` | account server (key the game server sends) | placeholder |
| `RECAPTCHA_SECRET` | account server | empty |
| `MASTER_KEY`, `GAME_SERVER_NAME`, `GAME_SERVER_KEY` | master server | placeholders |
| `SSL_KEY`, `SSL_CERT` | account and master servers: file paths that turn on HTTPS (ports 2083 and 2096) | off |

Login is Google only. Discord login, account linking and the Discord bot were removed.
