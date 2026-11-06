# special-guide

Mope 2020 server dump: web client, master server, account server and Java game server.

## Run everything

Needs Node.js 18+, Java 17+ and Maven. Logins, coins and skins also need a MySQL database; the account server creates its table automatically.

```
node server.js
```

On the first run this installs the Node packages and builds the game server jar. It then starts every part and serves them all on one port (`PORT`, else `WEB_PORT`, else 8000):

| Path | Goes to |
|------|---------|
| `/` | web client (`web/`) |
| `/api/account` | account server (internal port 14002) |
| `/api/master` | master server (internal port 14001) |
| `/game` | game server WebSocket (internal port 8080, set in `config.txt`) |

Open http://localhost:8000 to play. Press Ctrl+C to stop everything. Set `REBUILD_GAME=1` to rebuild the game server jar.

## Deploy to Sevalla

The repo includes a `Dockerfile` that builds the game server and runs `server.js`. Sevalla sets `PORT`, and the whole game runs behind that one address.

1. In Sevalla, create an **Application** from this GitHub repository and branch.
2. Under **Settings → Build**, set the build strategy to **Dockerfile** (path `Dockerfile`, context `.`).
3. Optional, for logins, coins and skins: create a **MySQL database** in Sevalla in the same location as the app. On the database's **Networking** tab, add an **internal connection** to the app and turn on **Add environment variables to the application**. That sets `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` and `DB_URL`, which the account server reads. You don't need to run any SQL: the account server creates the `users` table on startup (from `users_structure.sql`) and retries until the database is reachable.
4. Under **Environment variables**, add `GOOGLE_CLIENT_ID` for Google login, and add your Sevalla domain (`https://….sevalla.app`) to that client's authorized JavaScript origins.
5. Deploy. Players open the app's URL. The site is served over HTTPS and the game connects with `wss://` on the same address.

Run only one instance of the app, because all players share the one game server it starts.

## Settings (environment variables)

| Variable | Used by | Default |
|----------|---------|---------|
| `PORT`, `WEB_PORT` | public port for everything | `8000` |
| `GOOGLE_CLIENT_ID` | web client and account server: turns on Google login | off |
| `ACCOUNT_SERVER_URL` | web client: where it reaches the account server | `/api/account` |
| `MASTER_SERVER_URL` | web client: where it reads the server list and player count | `/api/master` |
| `GAME_SERVERS` | web client: JSON list of game servers, e.g. `[{"name":"EU 1","host":"game.example.com","port":443,"ssl":true}]` | 15 locations in USA, South America, Europe, Russia, Asia and Australia (see `GAME_SERVERS` in `web/client.js`), all on this site's `/game` address |
| `DB_HOST`, `DB_PORT`, `DB_DATABASE` (or `DB_NAME`), `DB_USERNAME` (or `DB_USER`), `DB_PASSWORD`, or one `DB_URL` (`mysql://user:pass@host:3306/db`) | account server | `127.0.0.1`, `3306`, `mope`, `root`, empty |
| `SERVER_KEY` | account server (key the game server sends) | placeholder |
| `RECAPTCHA_SECRET` | account server | empty |
| `MASTER_KEY`, `GAME_SERVER_NAME`, `GAME_SERVER_KEY` | master server (`GAME_SERVER_NAME` must match the game server's host) | placeholders, `localhost` |
| `SSL_KEY`, `SSL_CERT` | account and master servers: file paths that turn on HTTPS (ports 2083 and 2096) | off |

## Google login

1. In [Google Cloud Console](https://console.cloud.google.com/apis/credentials), create an **OAuth client ID** of type **Web application**.
2. Under **Authorized JavaScript origins**, add every address players open the site at, for example `http://localhost:8000` and `https://your-domain.com`. No redirect URI is needed.
3. Start the servers with the client ID:

   ```
   GOOGLE_CLIENT_ID=1234-abc.apps.googleusercontent.com node server.js
   ```

Without `GOOGLE_CLIENT_ID` the login button is hidden and the account server refuses Google logins.

The client uses Google Identity Services. The account server checks each Google ID token with Google, accepts it only if it was issued for `GOOGLE_CLIENT_ID` to a verified email, and then gives the player a random session token. If you host `web/` with a different web server, put the client ID in `web/config.js`.

Discord login, account linking and the Discord bot were removed.
