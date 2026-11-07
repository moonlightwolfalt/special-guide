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
| `DEVELOPER_EMAILS` | account server: comma-separated Google account emails that always get the top staff level (super developer). One developer account is built in, stored only as a SHA-256 hash because the repo is public | none |
| `CHANGELOG_REPO`, `CHANGELOG_BRANCH` | web server: the GitHub repository and branch whose commits the menu's changelog shows when there's no local `.git` (Docker images) | `moonlightwolfalt/special-guide`, `main` |
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

## Fetch the mope.io shop skins

`tools/fetch-mope-io-skins.ts` (run with [Bun](https://bun.sh)) downloads the current mope.io shop skin images:

- **s3.mope.io** (`mope-prod/items/`): every skin folder (its textures and its shop picture `<animal>.ui.webp`) and the skinned abilities (`items/abilities/…`). The bucket can be listed, so the script gets every file without guessing names.
- **mope.io** (`assets/animals/<biome>/<animal>/`): the plain animal textures. A skin folder only holds the parts the skin changes, and the rest of the animal comes from here. Also the shop picture of each subspecies and the coin and gem icons.
- **api.mope.io/store**: the shop catalog (price, currency, rarity, animal), plus names from s3.mope.io's translations.

```
bun tools/fetch-mope-io-skins.ts                      # everything, into mope-io-skins/
bun tools/fetch-mope-io-skins.ts --only fox,dragon    # skins for some animals or item ids
bun tools/fetch-mope-io-skins.ts --no-base            # only the s3 skin folders
bun tools/fetch-mope-io-skins.ts --list-only          # only write skins.json
```

Files keep their server paths under `mope-io-skins/s3/` and `mope-io-skins/mope.io/`, and `mope-io-skins/skins.json` lists every skin with its files. Files already downloaded are skipped, so running it again only fetches new skins. A full run is about 3,200 WebP files (64 MB).

### Add them to the shop

`tools/import_mope_io_skins.py` turns the fetched skins into shop skins for this game. They're in the shop's **MopeIO** tab:

```
bun tools/fetch-mope-io-skins.ts
python3 tools/import_mope_io_skins.py      # needs Pillow (pip install pillow)
```

For each skin it converts the WebP textures to the PNG file names the 2020 client draws, under `web/skins/<animal>/<skin number>/`, and registers the skin in the web client, the account server's shop list, `users_structure.sql`, `AnimalSkins.java` and `Account.java`. New skins get numbers from 210 up, kept in `tools/mope_io_imported.json`. The generated code sits between `mope.io skins` marker comments, so running the script again after fetching new skins replaces it cleanly.

Prices are the mope.io coin prices. Gem prices are ×25 coins, and skins mope.io no longer sells cost 10,000.

`tools/mope_io_animals.json` maps each animal's mope.io texture names to the 2020 ones, found by comparing the default animals pixel by pixel. For about 30 animals the 2020 client only swaps the main sprite for a skin, so their heads, wings and other parts keep the default look. The 2020 client draws the stingray's wings and tail, and the swordfish's nose and tail, as separate parts, while a mope.io skin's body is the whole animal. Their skins are drawn as one image over the area those parts cover (`full` in `EXTRA_SKINS`), so they don't flap or swish. The script skips skins that are already in the shop under the same name, skins that look exactly like the default animal, and skins whose main sprite can't be converted. It also skips the kakapo and turkey, which the 2020 client can't draw.

## Fetch the mope.io fire frames

`tools/fetch-fire-frames.js` downloads every fire frame from s3.mope.io: the fireball animation frames of all 31 styles (the default plus one per dragon, phoenix and lava monster skin), and the phoenix's 9 fire tornado images.

```
node tools/fetch-fire-frames.js            # writes fire-frames/ and fire-frames/fire-frames.json
node tools/fetch-fire-frames.js --no-tornado
```

Each fireball style has `fireball/<style>/dynamic/<n>.webp` (the moving fireball) and `fireball/<style>/static/<n>.webp`, 5 frames each (the king dragon has 6 static ones). `fire-frames.json` lists every style with its frames in order. Files already downloaded are skipped.

The import script also gives skins their exact fire when `fire-frames/` is there (run `tools/fetch-fire-frames.js` first). Each fireball style goes to its shop skin: 26 dragon, black dragon, king dragon, phoenix and lava monster skins, including older shop skins with the same name. Their frames go to `web/img/fireball/<style>/` (the fireball) and `web/img/fire/<style>/` (the fire it leaves), with styles from 100 up kept in `tools/mope_io_fire_styles.json`. The game server picks the style from the generated `Objects/Fire/FireStyles.java`. This changes only the picture: damage still follows the fireball's own number. The phoenix's fire tornado now follows its skin too, and 9 phoenix skins get their mope.io tornado. Left out: the plain fireball and the plain King Dragon fire (already the 2020 ones), and three styles no shop skin uses.

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

## Changelog

The menu's update panel lists the repository's commits, grouped by date and linked to GitHub, above the older hand-written notes. `server.js` serves them at `/changelog.json`. They come from `git log` when the folder has its `.git` history. In Docker images, which leave `.git` out, they come from GitHub's API for `CHANGELOG_REPO`, cached for 10 minutes (`CHANGELOG_CACHE_MINUTES`). That works without a key while the repository is public.

## Outer space

**The space biome.** Past the right ocean is a 2,500-wide space biome, which makes the map 11,000 × 7,500. A force field separates it from the ocean. The field opens every 5 minutes for 60 seconds, and everyone gets a message when it opens and when it closes.
- While it's open, anyone can cross.
- Players who crossed can stay in space as long as they like, even after it closes, and they don't get thirsty there. Upgrading keeps them in space.
- Once they're back on the normal map, they have to wait for the next opening.
- While closed, the field is red and striped; while open, it's green. Either way it shows a countdown.
- Developers can jump in with `goto:space`.
- Settings in `backup_server/server/config.txt`: `spaceWidth` (0 removes the biome), `spaceOpenEverySeconds` and `spaceOpenForSeconds`.

**Space food.** Space is full of food that any animal can eat: planets, stars, star systems, black holes, quasars, galaxies, galaxy clusters, superclusters, cluster filaments, the observable universe, asteroids, moons, satellites, the light spectrum from radio waves to gamma rays, and all 118 chemical elements. That's 148 kinds, about 230 at a time, and eaten food grows back after a while.
- Atoms (the 118 elements) are eaten on the first touch. Everything else takes several separate touches: 2 for light and asteroids, up to 12 for the observable universe. Staying on it counts once, so move off and touch it again. Each touch shrinks it a little, and a ring of gold segments shows the touches left. The XP comes with the last touch.
- Eating one gives a fixed amount plus a share of your current XP.
- The best pumpkin goal gives a fifth of your XP, so every share is bigger than that: from 21% for light and 22% for elements, up to 100% (double your XP) for the observable universe.
- The list of foods, with each one's size, XP and how many float around, is in `tools/space-art/space_foods.py`. After changing it, run `python3 tools/space-art/space_foods.py`. That regenerates the server's `SpaceFoodKind.java`, the client's `SPACE_FOODS` list and the pictures in `web/img/space/food/`.

**Beyond the edge.** Outside the whole map, on every side, the client also draws space with the bigger cosmic objects drifting in it. That part can't be entered: the map's edge is a barrier.

## XP limit

A player's XP goes up to 4,294,967,295, the most the client's XP field can hold. It used to stop at 2,000,000,000. The server keeps XP as a 64-bit number, and sends the full value for your XP, the leaderboard and end-of-game stats. Developers can set it with `xp:<amount>`.

## Turning speed

Animals turn on the game server, at most a set number of degrees per tick (`calculateAngle` in `Animal.java`). `turnSpeedPerTier` in `backup_server/server/config.txt` sets the limit for tiers 1 to 17, as degrees per tick (the server runs 20 ticks a second). It's 360 for every tier, so every animal turns instantly: no turn is over 180°. Lower a tier's number to make it turn more slowly. The client then eases its drawn angle toward the server's the same way v1.mope.io does: 10% of the gap per 1/120 s, at any frame rate.

To match v1's server turning speed, measure it with `tools/measure-v1-turn-speed.js`. The steps are at the top of that file: paste it into the browser console on v1.mope.io, play, and it prints how many degrees per update the server turns each animal.
