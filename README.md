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
| `/game` | game server WebSocket (internal port 18080, `GAME_PORT`) |

Open http://localhost:8000 to play. Press Ctrl+C to stop everything. Set `REBUILD_GAME=1` to rebuild the game server jar.

## Deploy to Render

The repo includes a `render.yaml` Blueprint and a `Dockerfile`. Render sets `PORT`, ends HTTPS at its edge, and the whole game runs behind that one address (the game connects with `wss://…/game`).

1. In Render, choose **New → Blueprint** and pick this repository and branch.
2. Render reads `render.yaml` and creates one Docker web service. Fill in the values it asks for, or leave them empty:
   - `DB_URL`: a MySQL database for logins, coins and skins, for example `mysql://user:password@host:3306/dbname`. Render has no managed MySQL, so use an external one. The account server creates its table on startup.
   - `GOOGLE_CLIENT_ID`: for Google login. Add `https://<your-service>.onrender.com` to that client's authorized JavaScript origins.
3. Click **Apply**. The first build takes a few minutes. Then open the service's `.onrender.com` URL.

`render.yaml` uses the **Standard** plan (2 GB) and one instance. The servers use about 400 MB at startup and more with players, so 512 MB plans are likely to run out of memory, and a second instance would split players into separate worlds. To try a smaller plan, change `plan` and set `JAVA_OPTS` (for example `-Xmx200m`).

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
| `PORT`, `WEB_PORT` | public port for everything | `8000`, or `8080` in the Docker image |
| `GOOGLE_CLIENT_ID` | web client and account server: turns on Google login | off |
| `ACCOUNT_SERVER_URL` | web client: where it reaches the account server | `/api/account` |
| `MASTER_SERVER_URL` | web client: where it reads the server list and player count | `/api/master` |
| `GAME_SERVERS` | web client: JSON list of game servers, e.g. `[{"name":"EU 1","host":"game.example.com","port":443,"ssl":true}]` | 15 locations in USA, South America, Europe, Russia, Asia and Australia (see `GAME_SERVERS` in `web/client.js`), all on this site's `/game` address |
| `DB_HOST`, `DB_PORT`, `DB_DATABASE` (or `DB_NAME`), `DB_USERNAME` (or `DB_USER`), `DB_PASSWORD`, or one `DB_URL` (`mysql://user:pass@host:3306/db`) | account server | `127.0.0.1`, `3306`, `mope`, `root`, empty |
| `DB_SSL` | account server: `true` to connect to MySQL over TLS with the certificate checked, `skip-verify` to encrypt without checking. Or add `?ssl=true` to `DB_URL`. | off |
| `DB_SSL_CA` | account server: your database provider's CA certificate (the PEM text or a file path), when it isn't publicly trusted | none |
| `SERVER_KEY` | account server (key the game server sends) | placeholder |
| `INFINITE_COINS` | account server: every account shows 999,999,999 coins and buys shop items for free; set to `0` for normal coin balances | on |
| `ENABLE_CAPTCHA` | game server: kick players who don't pass reCAPTCHA v3 within 60 s | off |
| `RECAPTCHA_SITE_KEY`, `RECAPTCHA_SECRET` | web client and account server: your reCAPTCHA v3 keys (needed when `ENABLE_CAPTCHA` is on) | empty |
| `CAPTCHA_HOSTNAME` | game server: the site hostname reCAPTCHA must report | any |
| `CAPTCHA_MIN_SCORE` | game server: reCAPTCHA v3 score (0.0 bot to 1.0 human) below which players are kicked | `0.5` |
| `ADSENSE_CLIENT` | web client: your Google AdSense publisher ID (`ca-pub-…`); turns on ads and `/ads.txt` | off |
| `ADSENSE_SLOT_RECT`, `ADSENSE_SLOT_BANNER` | web client: AdSense ad unit IDs for the 300×250 menu rectangle and the 728×90 bottom banner | empty |
| `JAVA_OPTS` | game server JVM flags | `-XX:MaxRAMPercentage=50` |
| `FORCE_HTTPS` | set to `0` to stop redirecting `http://` visits (seen via `X-Forwarded-Proto`) to `https://` | on |
| `MASTER_KEY`, `GAME_SERVER_NAME`, `GAME_SERVER_KEY` | master server (`GAME_SERVER_NAME` must match the game server's host) | placeholders, `localhost` |
| `SSL_KEY`, `SSL_CERT` | account and master servers: file paths that turn on HTTPS (ports 2083 and 2096) | off |

## Database (needed for logins, coins and skins)

Accounts are stored in MySQL (or MariaDB). Without a database, Google sign-in fails with "couldn't save your account (database problem)".

- **Sevalla:** create a MySQL database in the same location as the app. On the database's **Networking** tab, add an internal connection to the app and turn on **Add environment variables to the application**. That sets the `DB_*` variables for you.
- **Render** has no MySQL, so use a hosted one and set `DB_URL` to the connection string it gives you, for example `mysql://user:password@host:3306/dbname`. Most hosted MySQL services require TLS: add `?ssl=true` to the end. If the provider gives you a CA certificate, put its text in `DB_SSL_CA`.
- Special characters in the password must be URL-encoded in `DB_URL`, for example `@` as `%40` and `#` as `%23`.

The account server creates the `users` table itself. If it can't connect, its log says why: a wrong password, an unknown host, a database that requires TLS, or an untrusted certificate.

## Google login

1. In [Google Cloud Console](https://console.cloud.google.com/apis/credentials), create an **OAuth client ID** of type **Web application**.
2. Under **Authorized JavaScript origins**, add every address players open the site at, for example `http://localhost:8000` and `https://your-domain.com`. No redirect URI is needed.
3. Start the servers with the client ID:

   ```
   GOOGLE_CLIENT_ID=1234-abc.apps.googleusercontent.com node server.js
   ```

Without `GOOGLE_CLIENT_ID` the login button is hidden and the account server refuses Google logins.

The client uses Google Identity Services. The account server checks each Google ID token with Google, accepts it only if it was issued for `GOOGLE_CLIENT_ID` to a verified email, and then gives the player a random session token. If you host `web/` with a different web server, put the client ID in `web/config.js`.

Google login needs the database (`DB_URL` or the `DB_*` settings): that's where accounts, coins and skins are kept. On the Google Cloud **OAuth consent screen**, set the app name, support email and your site's domain, then **publish** the app. While it's in testing, only the test users you list can sign in.

## reCAPTCHA (anti-bot check)

With it on, the game server asks each player's browser for a reCAPTCHA v3 token and kicks players who don't pass within 60 seconds, or whose score is below `CAPTCHA_MIN_SCORE`.

1. At [Google reCAPTCHA admin](https://www.google.com/recaptcha/admin/create), create a site with type **Score based (v3)**. Add your domain, for example `your-domain.com`, plus `localhost` for testing.
2. Set the two keys and switch the check on:

   ```
   RECAPTCHA_SITE_KEY=6Lc...site RECAPTCHA_SECRET=6Lc...secret ENABLE_CAPTCHA=true node server.js
   ```

3. Optional: set `CAPTCHA_HOSTNAME=your-domain.com` so tokens made on other sites are refused.

Only turn on `ENABLE_CAPTCHA` once both keys are set. Without them every player is kicked, and `server.js` warns about this at startup.

## Google AdSense

Ads appear in the two ad slots on the start menu: a 300×250 rectangle and a 728×90 banner along the bottom. The banner is skipped on screens narrower than 760 px. The start menu also shows on the death screen and is hidden while playing, so ads never cover the game. Each slot is filled once per page load, because AdSense doesn't allow reloading ads.

1. Sign up at [Google AdSense](https://adsense.google.com/) and add your site. AdSense must approve the site before ads show.
2. Under **Ads → By ad unit**, create two **Display** ad units with fixed sizes, 300×250 and 728×90, and note each unit's ID (the `data-ad-slot` number).
3. Set your publisher ID and the two unit IDs:

   ```
   ADSENSE_CLIENT=ca-pub-1234567890123456 ADSENSE_SLOT_RECT=1111111111 ADSENSE_SLOT_BANNER=2222222222 node server.js
   ```

`server.js` then also serves `/ads.txt`, which AdSense requires. If you host `web/` with a different web server, put the IDs in `web/config.js` and publish `ads.txt` yourself: `google.com, pub-1234567890123456, DIRECT, f08c47fec0942fa0`.

In AdSense, turn off **Auto ads** for this site, or at least anchor and vignette ads. These float over the page and would cover the game. For visitors in the EEA, UK and Switzerland, set up a consent message under **Privacy & messaging**. Ads on `localhost` run in AdSense test mode, so local testing doesn't count as real impressions.

Discord login, account linking and the Discord bot were removed.

## Fetch the shop skins

`tools/fetch-shop-skins.js` downloads the shop skin list (id, name, image) and every skin image from a running site. It reads the names and image paths from the site's `client.js`, so it needs no login.

```
node tools/fetch-shop-skins.js                       # from https://mope.trygames.party
node tools/fetch-shop-skins.js --site http://localhost:8000 --out my-skins
node tools/fetch-shop-skins.js --no-images           # only write the list
```

It writes `shop-skins/skins.json` and the images under `shop-skins/images/`, in the same folders as the site's `skins/`. Prices, categories and ownership come from the account server and need a logged-in player: set `MOPE_USER_ID` and `MOPE_PASSWORD_TOKEN` to add them.

## Fetch the mope.io pumpkins

`tools/fetch-pumpkins.js` downloads the pumpkin textures from mope.io: 7 kinds (normal, jack, gold, emerald, ruby, diamond, onyx), 5 frames each, as WebP. The files are served from `mope.io/assets/terrain/pumpkin/`, not from s3.mope.io.

```
node tools/fetch-pumpkins.js            # writes pumpkins/<kind>/<frame>.webp and pumpkins/pumpkins.json
```

## Pumpkin kinds

Pumpkins spawn in the land biome when `pumpEnabled` is on. Each kind's spawn rate, cap and rewards are set in `backup_server/server/src/main/java/io/mopesbox/Objects/ETC/PumpkinType.java`. The client draws kind `<id>` from `web/img/pumpkin/<id>/<0-4>.png`.

| Id | Kind | Rarity | Spawns every | Goal reward |
|----|------|--------|--------------|-------------|
| 0 | Normal | common | 30 s (up to 2) | 150 coins, 50 EXP |
| 1 | Golden | common+ | 100 s | 150 coins, 50 EXP |
| 6 | Emerald (green) | uncommon | 3 min | 200 coins, 65 EXP |
| 2 | Sapphire (blue) | rare | 5 min | 250 coins, 80 EXP |
| 3 | Amethyst (purple) | epic | 10 min | 400 coins, 120 EXP |
| 4 | Amber (orange) | legendary | 20 min | 600 coins, 180 EXP |
| 7 | Ruby (red) | mythic | 30 min | 750 coins, 220 EXP |
| 8 | Diamond | mythic+ | 40 min | 875 coins, 260 EXP |
| 5 | Black Hole | rarest | 60 min | 1000 coins, 300 EXP |

Admins can spawn one with the `pump <id>` command.

## Shop skins

Each shop skin is defined in five places, which must agree:

1. `AnimalSkins.java`: `<id>_skin`, the animal, tier, ability and skin number.
2. `Account.java`: an `int <id>` field (1 = owned). `getSkins()` reads every field that matches a skin.
3. `users_structure.sql`: a `<id>` column. On startup the account server adds a column for any shop item missing from an existing `users` table.
4. `backup_account/accountserver/index.js`: the `shopitems` entry with price and tab (`gold`, `locked`, `halloween` or `community`).
5. `web/client.js`: a `getAnimalInfo` case with the shop name and image, plus the images under `web/skins/`.

The 73 skins imported from mope.trygames.party also have an `EXTRA_SKINS` entry in `web/client.js`. It gives each one its in-game name. For animals whose drawing code ignores the skin number (fox, rabbit, trout, crab and others), it also points the sprite at the skin's own image.

## Fetch every shop skin texture (Python)

`tools/fetch_shop_skin_assets.py` downloads every texture the shop skins use: bodies, heads, wings, tails, animation frames and ability icons. The site doesn't list its folders and its client builds texture paths at run time. So the script opens the site in headless Chromium and makes the site's own client draw each skin in many states, recording every texture it asks for. A second pass tries names that other skins of the same animal use, and follows numbered runs like `tongue0`…`tongue12`.

```
pip install playwright
playwright install chromium          # or pass --browser /path/to/chrome
python tools/fetch_shop_skin_assets.py                       # all skins from mope.trygames.party
python tools/fetch_shop_skin_assets.py --names-from web/skins    # also learn names from this repo's skins
python tools/fetch_shop_skin_assets.py --only skin_kaijuRex,gold_trex --source both
```

It writes the textures to `shop-skin-assets/` in the site's own folder layout, plus `shop-skin-assets/skins.json`, which lists each skin's files. By default it saves the full-size `skins/…` originals, falling back to the smaller `assets/s/0/…` copies the game loads. `--source game` saves only the game copies, and `--source both` keeps both.
