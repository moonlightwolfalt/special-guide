var express = require('express');
var fs = require('fs');
var app = express();
var https = require('https');
var http = require('http');
var bodyParser = require('body-parser');
var crypto = require('crypto');
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));
var _ = require('lodash');
// Shared key the game server sends with privileged requests.
const SERVER_KEY = process.env.SERVER_KEY || 'REMOVED_DUE_TO_SENSITIVE_DATA';
const googleKey = process.env.RECAPTCHA_SECRET || "";
// OAuth client ID of the web client. Google ID tokens must be issued for it.
const googleClientId = process.env.GOOGLE_CLIENT_ID || "";
if (!googleClientId) console.log("GOOGLE_CLIENT_ID is not set: Google login is off.");
if (!googleKey) console.log("RECAPTCHA_SECRET is not set: reCAPTCHA checks will fail, so leave the game server's ENABLE_CAPTCHA off.");
var season = 0;


var coinsperoutlevel = 2000;

// Unlimited coins: every account (players log in with Google) shows this balance and
// buys shop items without spending coins. INFINITE_COINS=0 turns it off.
const INFINITE_COINS = process.env.INFINITE_COINS !== '0';
const INFINITE_COIN_BALANCE = 999999999;
if (INFINITE_COINS) console.log("Unlimited coins are on (INFINITE_COINS=0 to turn off).");

function withCoinBalance(row) {
    if (INFINITE_COINS && row) row.coins = INFINITE_COIN_BALANCE;
    return row;
}

//DB

var mysql = require('mysql2');

// Database settings. Sevalla's internal database connection sets DB_URL and
// DB_HOST/DB_PORT/DB_DATABASE/DB_USERNAME/DB_PASSWORD; DB_USER and DB_NAME also work.
function dbSettings() {
    const env = process.env;
    const settings = {
        host: env.DB_HOST || '127.0.0.1',
        port: Number(env.DB_PORT) || 3306,
        user: env.DB_USERNAME || env.DB_USER || 'root',
        password: env.DB_PASSWORD || '',
        database: env.DB_DATABASE || env.DB_NAME || 'mope',
    };
    if (env.DB_URL && !env.DB_HOST) {
        try {
            const url = new URL(env.DB_URL);
            settings.host = url.hostname || settings.host;
            settings.port = Number(url.port) || settings.port;
            if (url.username) settings.user = decodeURIComponent(url.username);
            if (url.password) settings.password = decodeURIComponent(url.password);
            if (url.pathname.length > 1) settings.database = decodeURIComponent(url.pathname.slice(1));
            // Hosted MySQL usually requires TLS; providers write it in several ways.
            const q = url.searchParams;
            const mode = String(q.get('ssl') || q.get('ssl-mode') || q.get('sslmode') || q.get('tls') || '').toLowerCase();
            if (mode) settings._ssl = mode;
        } catch (err) {
            console.log("DB_URL is not a valid URL:", err.message);
        }
    }
    // DB_SSL wins over DB_URL. true/required: encrypted, with the server's certificate checked.
    // skip-verify: encrypted without checking (only for providers with self-signed certificates).
    const mode = String(env.DB_SSL || settings._ssl || '').toLowerCase();
    delete settings._ssl;
    if (/^(1|true|on|yes|require|required|verify_ca|verify_identity|verify-ca|verify-full|preferred)$/.test(mode)) {
        settings.ssl = { rejectUnauthorized: true, minVersion: 'TLSv1.2' };
    } else if (/^(skip-verify|insecure|no-verify)$/.test(mode)) {
        settings.ssl = { rejectUnauthorized: false, minVersion: 'TLSv1.2' };
    }
    if (settings.ssl && env.DB_SSL_CA) {
        // the provider's CA certificate: the PEM text itself, or a path to the file
        const ca = env.DB_SSL_CA.includes('-----BEGIN') ? env.DB_SSL_CA : (() => {
            try { return fs.readFileSync(env.DB_SSL_CA, 'utf8'); } catch (err) {
                console.log("DB_SSL_CA could not be read:", err.message);
                return null;
            }
        })();
        if (ca) settings.ssl.ca = ca;
    }
    return settings;
}

var dbConfig = dbSettings();
if (!process.env.DB_URL && !process.env.DB_HOST)
    console.log("DB_URL is not set: logins (including Google), coins and skins need a MySQL database. See the README.");
var connectionms = mysql.createPool({
  ...dbConfig,
  multipleStatements: false
});

var encode_utf8 = function(s) {
    return unescape(encodeURIComponent(s));
}

// What to change when the database can't be reached, for the usual causes.
function dbErrorHint(error) {
    const code = error.code || '';
    const msg = String(error.message || '');
    if (code === 'ECONNREFUSED' && /^(127\.0\.0\.1|localhost)$/.test(dbConfig.host))
        return "  No database on this machine: set DB_URL to your MySQL database, e.g. mysql://user:password@host:3306/dbname";
    if (code === 'ENOTFOUND' || code === 'EAI_AGAIN') return "  The database host name doesn't resolve: check the host in DB_URL.";
    if (code === 'ETIMEDOUT' || code === 'ECONNREFUSED')
        return "  The database didn't answer: check the host and port in DB_URL, and that the database allows connections from this server.";
    if (code === 'ER_ACCESS_DENIED_ERROR') return "  Wrong database user or password in DB_URL (special characters in the password must be URL-encoded, e.g. @ as %40)." +
        (dbConfig.ssl ? "" : " If your provider requires TLS, also add ?ssl=true to DB_URL.");
    if (code === 'ER_BAD_DB_ERROR') return "  That database name doesn't exist: create it, or fix the name at the end of DB_URL.";
    if (code === 'HANDSHAKE_NO_SSL_SUPPORT') return "  This database doesn't support TLS: remove ssl from DB_URL / DB_SSL.";
    if (/insecure transport|SSL connection is required|require_secure_transport|ER_SECURE_TRANSPORT_REQUIRED/i.test(msg + code))
        return "  This database requires TLS: add ?ssl=true to the end of DB_URL (or set DB_SSL=true).";
    if (/self[- ]signed|unable to verify|certificate/i.test(msg))
        return "  The database's TLS certificate isn't trusted: set DB_SSL_CA to your provider's CA certificate.";
    return null;
}

// Create the users table if it is missing, retrying until the database is reachable.
function ensureSchema(attempt = 1) {
    const schema = fs.readFileSync(__dirname + '/../../users_structure.sql', 'utf8');
    const conn = mysql.createConnection({ ...dbConfig, multipleStatements: true });
    conn.query(schema, (error) => {
        conn.end(() => {});
        if (error) {
            const wait = Math.min(30, attempt * 2);
            console.log(`Database not ready (${error.code || error.message}) at ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}${dbConfig.ssl ? ' over TLS' : ''}; retrying in ${wait}s`);
            const hint = dbErrorHint(error);
            if (hint && attempt <= 3) console.log(hint);
            setTimeout(() => ensureSchema(attempt + 1), wait * 1000);
        } else {
            fixSeasonDataColumn();
        }
    });
}

// Battle-pass progress is a string of digits ("0100..."). Older copies of the schema
// made seasondata an integer, which turns it into 0, so convert that column to text.
function fixSeasonDataColumn() {
    connectionms.query(
        "SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'seasondata'",
        (error, rows) => {
            if (error) {
                console.log("Could not check the seasondata column:", error.message);
            } else if (rows.length && rows[0].DATA_TYPE !== 'varchar') {
                connectionms.query("ALTER TABLE `users` MODIFY `seasondata` varchar(225) DEFAULT NULL", (err) => {
                    if (err) console.log("Could not convert seasondata to text:", err.message);
                    else console.log("Converted users.seasondata to text so battle-pass progress is kept.");
                });
            }
            addMissingShopColumns();
        });
}

// Each shop item is a users column (1 = owned). users_structure.sql only creates the
// table when it is missing, so add columns for shop items added since then.
function addMissingShopColumns() {
    connectionms.query(
        "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users'",
        (error, rows) => {
            if (error) {
                console.log("Could not check the shop item columns:", error.message);
                console.log(`Database ready: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`);
                return;
            }
            const have = new Set(rows.map(r => r.COLUMN_NAME));
            const missing = shopitems.map(i => i.itemName).filter(n => /^\w+$/.test(n) && !have.has(n));
            if (!missing.length) {
                console.log(`Database ready: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`);
                return;
            }
            const sql = "ALTER TABLE `users` " + missing.map(n => "ADD COLUMN `" + n + "` int(11) DEFAULT NULL").join(", ");
            connectionms.query(sql, (err) => {
                if (err) console.log("Could not add shop item columns:", err.message);
                else console.log(`Added ${missing.length} shop item column(s) to users.`);
                console.log(`Database ready: ${dbConfig.host}:${dbConfig.port}/${dbConfig.database}`);
            });
        });
}

let seasondata = null;
let seasonlevels = -1;

function parseSeasons() {
    let data = fs.readFileSync(__dirname + '/seasons.json');
    data = JSON.parse(data);
    data = data[season];
    seasondata = data;
    seasonlevels = seasondata.levels.free.length;
    console.log("Season "+season+" config loaded! Levels: "+seasonlevels);
}

async function registerUser(token, social, code) {
    // console.log(code)
    var out = {
        success: false,
        reason: 'database'
    };
    let email, email_ver, name, picture, given_name, family_name, locale = null;
    let query1, query1add = null;
    if(social == "google") {
        if(!googleClientId) return { success: false, reason: 'login_off' };
        if(!token) return { success: false, reason: 'invalid_token' };
        // Google checks the token's signature and expiry; we check it was issued for this game.
        let data;
        try {
            let response = await fetch('https://oauth2.googleapis.com/tokeninfo?id_token=' + encodeURIComponent(token));
            data = await response.json();
        } catch (err) {
            console.log("Google token check failed:", err.message);
            return { success: false, reason: 'google_unreachable' };
        }
        if(!data || data.error_description || data.error) return { success: false, reason: 'invalid_token' };
        if(data.aud !== googleClientId) {
            console.log("Rejected Google token issued for another app:", data.aud);
            return { success: false, reason: 'wrong_client_id' };
        }
        if(data.iss !== "accounts.google.com" && data.iss !== "https://accounts.google.com") return { success: false, reason: 'invalid_token' };
        if(String(data.email_verified) !== "true" || !data.email) return { success: false, reason: 'email_not_verified' };
        // The ID token only proves who the player is; the session uses its own random token.
        token = crypto.randomBytes(32).toString('hex');

        email = data.email;
        email_ver = data.email_verified;
        name = data.name;
        picture = data.picture;
        given_name = data.given_name;
        family_name = data.family_name;
        locale = data.locale;
        query1 = `SELECT * FROM users WHERE email=?`;
        query1add = [email];
    }
    if(query1 == null)
        return out;
    await connectionms.promise().query(query1, query1add)
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            let massiv = [email,social,email_ver,name,picture,locale,token,+new Date()+21600000];
            await connectionms.promise().query('INSERT INTO `users`(`email`, `social`, `email_isVerified`, `name`, `profilePicUrl`, `locale`, `password_token`, `expirydate`) VALUES (?,?,?,?,?,?,?,?)', massiv)
            .then(async ([rows,fields]) => {
                await connectionms.promise().query(query1, query1add)
                .then( ([rows,fields]) => {
                    if(!rows || !rows.length) return out;
                    out = {
                        success: true,
                        login_userId: rows[0].id,
                        login_passwordToken: rows[0].password_token,
                        login_name: rows[0].name,
                        login_profilePicURL: rows[0].profilePicUrl
                    }
                    // console.log("da");
                    return out;
                })
                .catch( (err) => {
                    console.log(err);
                    console.log("errr");
                    out = {
                        success: false,
                        reason: 'database'
                    }
                    return out;
                });
            })
            .catch( (err) => {
                console.log(err);
                console.log("errr");
                out = {
                    success: false,
                    reason: 'database'
                }
                return out;
            });
        } else {
            let query2, query2add = null;
            query2add = [token, +new Date()+21600000];
            if(social == "google") {
                query2 = `UPDATE \`users\` SET \`password_token\`=?, \`expirydate\`=? WHERE \`email\`=?`;
                query2add.push(email);
            }
            if(query2 == null)
                return out;
            await connectionms.promise().query(query2, query2add)
            .then( ([]) => {
                out = {
                    success: true,
                    login_userId: rows[0].id,
                    login_passwordToken: token,
                    login_name: rows[0].name,
                    login_profilePicURL: rows[0].profilePicUrl
                }
                // console.log("da");
                return out;
            })
            .catch( (err) => {
                console.log(err);
                console.log("errr");
                out = {
                    success: false,
                    reason: 'database'
                }
                return out;
            });
        }
    })
    .catch( (err) => {
        console.log(err);
        console.log("errr");
        out = {
            success: false,
            reason: 'database'
        }
        return out;
    });
    return out;
}

async function verifyUser(id, token) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            out = {
                success: false
            }
            return out;
        } else {
            if(rows[0].expirydate > +new Date()) {
                await connectionms.promise().query(`UPDATE \`users\` SET \`expirydate\`=? WHERE id=? AND password_token=?`, [+new Date()+21600000, id, token])
                .catch( (err) => {
                    console.log(err);
                });
                out = {
                    success: true,
                    pfp: rows[0].profilePicUrl
                }
                return out;
            } else {
                out = {
                    success: false
                }
                return out;
            }
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

function isTaken(data, level, gold) {
    if(gold == true) level += seasonlevels;
    data = data.split('');
    if(level > data.length) return false;
    if(Number(data[level-1]) == 0) return false;
    else return true;
}

function setLevel(data, level, gold, value) {
    if(gold == true) level += seasonlevels;
    data = data.split('');
    data[level-1] = value;
    data = data.join('');
    return data;
}

function isValidSeasonData(data) {
    if(!data) return false;//not hard to fix rlly, kys russianmope
    data = data.split('');
    if(data.length != seasonlevels*2) return false;
    return true;
}

function constructSeasonData() {
    let data = "";
    for(var i = 0; i < seasonlevels*2; i++) {
        data += "0";
    }
    return data;
}

async function getBattlePass(id, token) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1)) {
            out = {
                success: false
            }
            return out;
        } else {
            if(!isValidSeasonData(rows[0].seasondata)) {
                await connectionms.promise().query(`UPDATE \`users\` SET \`seasondata\`=? WHERE id=?`, [constructSeasonData(), id])
                .then((rows) => {})
                .catch( (err) => {
                    console.log(err);
                });
                rows[0].seasondata = constructSeasonData();
            }
            let tempdata = seasondata;
            let temp = tempdata.levels.free;
            let tempgold = tempdata.levels.gold;
            let finaldata = {};
            finaldata.maxlevel = temp.length;
            finaldata.season_number = season;
            finaldata.haspass = rows[0].seasonpass == 1;
            finaldata.end = tempdata.end;
            finaldata.name = tempdata.name;
            finaldata.levels = {};
            finaldata.levels.free = [];
            finaldata.levels.gold = [];
            for(var i in temp) {
                i = Number(i);
                finaldata.levels.free[i] = _.clone(temp[i]);
                finaldata.levels.free[i].id = i;
                if(i+1 <= rows[0].level) {
                    finaldata.levels.free[i].cantake = !isTaken(rows[0].seasondata, i+1, false);
                    finaldata.levels.free[i].taken = isTaken(rows[0].seasondata, i+1, false);
                } else {
                    finaldata.levels.free[i].taken = false;
                    finaldata.levels.free[i].cantake = false;
                }
                if(finaldata.levels.free[i].bonus == "coins") {
                    delete finaldata.levels.free[i].bonus;
                    finaldata.levels.free[i].type = 0;
                } else if(finaldata.levels.free[i].bonus == "ani") {
                    delete finaldata.levels.free[i].bonus;
                    finaldata.levels.free[i].type = Number(finaldata.levels.free[i].ani);
                }
            }
            for(var i in tempgold) {
                i = Number(i);
                finaldata.levels.gold[i] = _.clone(tempgold[i]);
                finaldata.levels.gold[i].id = i;
                if(i+1 <= rows[0].level) {
                    finaldata.levels.gold[i].cantake = !isTaken(rows[0].seasondata, i+1, true);
                    finaldata.levels.gold[i].taken = isTaken(rows[0].seasondata, i+1, true);
                } else {
                    finaldata.levels.gold[i].taken = false;
                    finaldata.levels.gold[i].cantake = false;
                }
                if(finaldata.levels.gold[i].bonus == "coins") {
                    delete finaldata.levels.gold[i].bonus;
                    finaldata.levels.gold[i].type = 0;
                } else if(finaldata.levels.gold[i].bonus == "ani") {
                    delete finaldata.levels.gold[i].bonus;
                    finaldata.levels.gold[i].type = Number(finaldata.levels.gold[i].ani);
                }
            }
            out = {
                success: true,
                data: finaldata
            }
            return out;
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

async function loadAccountData(id, token) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            out = {
                success: false
            }
            return out;
        } else {
            out = {
                coins: INFINITE_COINS ? INFINITE_COIN_BALANCE : rows[0].coins,
                level: rows[0].level,
                xp: rows[0].xp,
                xpNextLvl: getNextLevelXP(rows[0].level),
                banned: rows[0].banned,
                reason: rows[0].banned == 1 ? rows[0].banreason || "no reason" : "null"
            }
            return out;
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

var shopitems = [
    {
        coinsPrice: 20000,
        shopCategory: 'gold',
        itemName: "gold_blackDragon"
    },
    {
        coinsPrice: 20000,
        shopCategory: 'gold',
        itemName: "gold_kingDragon"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_Dragon"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_kraken"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_yeti"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_trex"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_kingCrab"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'gold',
        itemName: "gold_phoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'gold',
        itemName: "gold_pterodactyl"
    },
    {
        coinsPrice: 10000,
        shopCategory: 'gold',
        itemName: "gold_iceMonster"
    },
    {
        coinsPrice: 10000,
        shopCategory: 'gold',
        itemName: "gold_dinoMonster"
    },
    {
        coinsPrice: 10000,
        shopCategory: 'gold',
        itemName: "gold_seaMonster" // stoppped here
    },
    {
        coinsPrice: 10000,
        shopCategory: 'gold',
        itemName: "gold_landMonster"
    },
    {
        coinsPrice: 10000,
        shopCategory: 'gold',
        itemName: "gold_giantScorion"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'gold',
        itemName: "gold_bluewhale"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'gold',
        itemName: "gold_elephant"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'gold',
        itemName: "gold_cassowary"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'gold',
        itemName: "gold_giantspider"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'gold',
        itemName: "gold_blackwidow"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'gold',
        itemName: "gold_mammoth"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_hippo"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_ostrich"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_boa"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_komodo"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_killerwhale"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'gold',
        itemName: "gold_sabertooth"
    },
    {
        coinsPrice: 14000,
        shopCategory: 'locked',
        itemName: "skin_orangeScorpion"
    },
    {
        coinsPrice: 20000,
        shopCategory: 'locked',
        itemName: "skin_blueDragon"
    },
    {
        coinsPrice: 25000,
        shopCategory: 'locked',
        itemName: "skin_kingRipper"
    },
    {
        coinsPrice: 25000,
        shopCategory: 'locked',
        itemName: "skin_kingStan"
    },
    {
        coinsPrice: 25000,
        shopCategory: 'locked',
        itemName: "skin_kingShah"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_dragon_gray"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'locked',
        itemName: "skin_kraken_cyan"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_yeti_emerald"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_snowman" // new station
    },
    {
        coinsPrice: 5000,
        shopCategory: 'locked',
        itemName: "skin_trex_gray"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_kingCrab"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'locked',
        itemName: "skin_phoenix_blue"
    },
    {
        coinsPrice: 9000,
        shopCategory: 'locked',
        itemName: "skin_pterodactyl_green"
    },
    {
        coinsPrice: 20000,
        shopCategory: 'locked',
        itemName: "skin_lavaStrider"
    },
    {
        coinsPrice: 35000,
        shopCategory: 'locked',
        itemName: "skin_boneKing"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_dragon_terminator"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_dragon_red"
    },
    {
        coinsPrice: 13000,
        shopCategory: 'locked',
        itemName: "skin_iceMonster"
    },
    {
        coinsPrice: 13000,
        shopCategory: 'locked',
        itemName: "skin_dinoMonster"
    },
    {
        coinsPrice: 13000,
        shopCategory: 'locked',
        itemName: "skin_seaMonster"
    },
    {
        coinsPrice: 13000,
        shopCategory: 'locked',
        itemName: "skin_landMonster"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'locked',
        itemName: "skin_bluewhale"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_elephant"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_cassowary"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_giantspider"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_blackwidow"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_mammoth"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_hippo"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_ostrich"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_boa"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_komodo"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'locked',
        itemName: "skin_killerwhale"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_sabertooth"
    },
    {
        coinsPrice: 6000,
        shopCategory: 'locked',
        itemName: "skin_tigerShark"
    },
    {
        coinsPrice: 7000,
        shopCategory: 'locked',
        itemName: "skin_hamerHeadShark"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_panda"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_zonkey"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_okapi"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_giantCroc"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_purpleMouse"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_starMouse",
        isExclusive: true
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_winterMouse",
        isExclusive: true
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_prismaToucan"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'gold',
        itemName: "gold_mouse"
    },
    {
        coinsPrice: 25000,
        shopCategory: 'locked',
        itemName: "skin_queenScarlet"
    },
    {
        coinsPrice: 100000,
        shopCategory: 'community',
        itemName: "skin_queenRipper",
        author: "Ahmetcan"
    },
    {
        coinsPrice: 100000,
        shopCategory: 'community',
        itemName: "skin_sapphireMouse",
        author: "krkika"
    },
    {
        coinsPrice: 100000,
        shopCategory: 'community',
        itemName: "skin_amethystMouse",
        author: "krkika"
    },
    {
        coinsPrice: 100000,
        shopCategory: 'community',
        itemName: "skin_emeraldMouse",
        author: "krkika"
    },
    {
        coinsPrice: 100000,
        shopCategory: 'community',
        itemName: "skin_diamondMouse",
        author: "krkika"
    },
    {
        coinsPrice: 200000,
        shopCategory: 'community',
        itemName: "skin_lordofice",
        author: "Nike"
    },
    {
        coinsPrice: 250000,
        shopCategory: 'community',
        itemName: "skin_kingMelon",
        author: "Nike"
    },
    {
        coinsPrice: 49999,
        shopCategory: 'community',
        itemName: "skin_partycrock",
        author: "Croccin"
    },
    {
        coinsPrice: 99999,
        shopCategory: 'community',
        itemName: "skin_retrocrocodilealligator",
        author: "Croccin"
    },
    {
        coinsPrice: 199999,
        shopCategory: 'community',
        itemName: "skin_rubblealligator",
        author: "Croccin"
    },
    {
        coinsPrice: 111111,
        shopCategory: 'community',
        itemName: "skin_rubyblackdragon",
        author: "Krkika"
    },
    {
        coinsPrice: 35000,
        shopCategory: 'gold',
        itemName: "skin_kingGolder"
    },
    {
        coinsPrice: 100000,
        shopCategory: 'community',
        itemName: "skin_kingInferno",
        author: "Ahmetcan"
    },
    {
        coinsPrice: 25000,
        shopCategory: 'locked',
        itemName: "skin_queenCeleste"
    },
    {
        coinsPrice: 15000,
        shopCategory: 'locked',
        itemName: "skin_bigFoot"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_woollyRhino"
    },
    //hello
    {
        coinsPrice: 30300,
        shopCategory: 'community',
        itemName: "skin_banditMouse",
        author: "Myst"
    },
    {
        coinsPrice: 86400,
        shopCategory: 'community',
        itemName: "skin_chocolatebirthdaycakeMouse",
        author: "Krkika"
    },
    {
        coinsPrice: 86400,
        shopCategory: 'community',
        itemName: "skin_strawberrybirthdaycakeMouse",
        author: "Krkika"
    },
    {
        coinsPrice: 50505,
        shopCategory: 'community',
        itemName: "skin_chocolateDragon",
        author: "Krkika"
    },
    {
        coinsPrice: 50000,
        shopCategory: 'community',
        itemName: "skin_neonDragon",
        author: "Krkika"
    },
    {
        coinsPrice: 220222,
        shopCategory: 'community',
        itemName: "skin_galaxyPhoenix",
        author: "Myst"
    },
    {
        coinsPrice: 500000,
        shopCategory: 'community',
        itemName: "skin_meltedMonster",
        author: "Myst"
    },
    {
        coinsPrice: 200000,
        shopCategory: 'community',
        itemName: "skin_giftboxKraken",
        author: "Myst"
    },
 
    // Imported from mope.trygames.party
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "astralPhoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "bonfire_phoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_dragon_3_3"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_dragon_3_9"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_dragon_202_0"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_dragon_224_0"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_leatherBackTurtle"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_celestialCobra"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_brazenBull"
    },
    {
        coinsPrice: 13000,
        shopCategory: 'locked',
        itemName: "skin_luckyCharmMonster"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_iceToucan"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_clowoudLion"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_sunsetClowoudLion"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_stormClowoudLion"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_kaijuRex"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_warTHog"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_velvetChimp"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_aquaPhoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_icePhoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_ashPhoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_redGiantPhoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_copperPhoenix"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_goofyDragon"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_desertDragon"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_forgeDragon"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_mythicalSerpent"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_fireyDragon"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'locked',
        itemName: "skin_technoSaber"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_iceFox"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_roseDragon"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_purpleHaze"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_clownfish"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_tullyMonster"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'locked',
        itemName: "skin_vampireSquid"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_cuttlefish"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_deluxeShrimp"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_monochromeRabbit"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_nightFox"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'locked',
        itemName: "skin_plagueRaptor"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'locked',
        itemName: "skin_titanBison"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'locked',
        itemName: "skin_dowagerVarande"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'locked',
        itemName: "skin_emeprorVarande"
    },
    {
        coinsPrice: 13000,
        shopCategory: 'locked',
        itemName: "skin_rubyDevil"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'locked',
        itemName: "skin_metalicCrab"
    },
    {
        coinsPrice: 25000,
        shopCategory: 'locked',
        itemName: "skin_queenFlame"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_wyrmous"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_equusthulhu"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_frostaline"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_pufferMace"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'halloween',
        itemName: "halloween_carnageodactyl"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'halloween',
        itemName: "halloween_mrDollar"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_wendigoDeer"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'halloween',
        itemName: "halloween_cactiRat"
    },
    {
        coinsPrice: 5000,
        shopCategory: 'halloween',
        itemName: "halloween_hiPower"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'halloween',
        itemName: "halloween_puumpkat"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_doomedarius"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'halloween',
        itemName: "halloween_roboMouse"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_uncleSam"
    },
    {
        coinsPrice: 2000,
        shopCategory: 'halloween',
        itemName: "halloween_zombieMole"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_flammyHart"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_gummyViper"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_grimRipper"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_bigCookie"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'halloween',
        itemName: "halloween_gargolgon"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_candyRus"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_mantilian"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_phantomRay"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_suitedToucan"
    },
    {
        coinsPrice: 4000,
        shopCategory: 'halloween',
        itemName: "halloween_vulgeist"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_burlone"
    },
    {
        coinsPrice: 3000,
        shopCategory: 'halloween',
        itemName: "halloween_unicornze"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_snowgirl"
    },
    {
        coinsPrice: 8000,
        shopCategory: 'locked',
        itemName: "skin_pterodactyl_blue"
    },
];

var exclusiveitems = [];

for(var i in shopitems) {
    if(shopitems[i].isExclusive) {
        exclusiveitems.push(shopitems[i]);
    }
}

shopitems.sort((prev, next) => prev.coinsPrice - next.coinsPrice);

var levelxps = [
    500,
    500,
    500,
    600,
    650,
    700,
    700,
    800,
    800,
    1000
]

function getNextLevelXP(level) {
    return levelxps[level-1] == null ? levelxps[levelxps.length-1] : levelxps[level-1];
}

function getCost(itemName) {
    for(var i in shopitems) {
        if(shopitems[i].itemName == itemName) return shopitems[i].coinsPrice;
    }
    return false;
}

async function getBattlePassReward(id, token, level, gold) {
    level = Number(level);
    gold = gold == "true";
    var out = {
        success: false
    };
    if(level > seasonlevels) return out;
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1)) {
            out = {
                success: false,
                reason: 'error'
            }
            return out;
        } else {
            if(!isValidSeasonData(rows[0].seasondata)) {
                out = {
                    success: false,
                    reason: 'baddata'
                }
                return out;
            } else if(gold && !rows[0].seasonpass) {
                out = {
                    success: false,
                    reason: 'nopass'
                }
                return out;
            } else if(isTaken(rows[0].seasondata, level, gold)) {
                out = {
                    success: false,
                    reason: 'alreadytaken'
                }
                return out;
            } else if(rows[0].level < level) {
                out = {
                    success: false,
                    reason: 'lowlevel'
                }
                return out;
            } else {
                let bonus = getBonus(level, gold);
                let newseasondata = setLevel(rows[0].seasondata, level, gold, 1);
                await connectionms.promise().query(`UPDATE \`users\` SET \`seasondata\`=?${bonus} WHERE id=? AND password_token=?`, [newseasondata, id, token])
                .then( ([]) => {
                    out = {
                        success: true
                    }
                    return out;
                })
                .catch( (err) => {
                    console.log(err);
                    console.log("errr");
                    out = {
                        success: false,
                        reason: 'error'
                    }
                    return out;
                });
            }
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

function getBonus(level, gold) {
    let lvl = seasondata.levels;
    if(gold) lvl = lvl.gold;
    else lvl = lvl.free;
    lvl = lvl[level-1];
    let bonus = lvl.bonus;
    if(bonus == "coins") {
        let sql = ", `coins`=`coins`+"+lvl.value;
        return sql;
    } else if(bonus == "skin") {
        let sql = ", `"+lvl.dbskin+"`=1";
        return sql;
    }
    return "";
}

// Buys every listed shop item the player doesn't own yet, in one update, so it's all
// or nothing. The client sends the items its shop shows, so the price matches what the
// player saw; only real, non-exclusive shop items are bought.
async function buyAllItems(id, token, requested) {
    const wanted = new Set(String(requested || '').split(',').map(s => s.trim()).filter(Boolean));
    const exclusive = new Set(exclusiveitems.map(i => i.itemName));
    const items = shopitems.filter(i => wanted.has(i.itemName) && !i.isExclusive && !exclusive.has(i.itemName)
        && /^\w+$/.test(i.itemName) && typeof i.coinsPrice === 'number' && i.coinsPrice >= 0);
    if (!items.length) return { success: false, reason: 'error' };
    try {
        const [rows] = await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token]);
        if (!rows.length || rows[0].banned == 1) return { success: false, reason: 'error' };
        const toBuy = items.filter(i => rows[0][i.itemName] != 1);
        if (!toBuy.length) return { success: false, reason: 'alreadyowned' };
        const cost = INFINITE_COINS ? 0 : toBuy.reduce((sum, i) => sum + i.coinsPrice, 0);
        if (rows[0].coins < cost) return { success: false, reason: 'cantafford', cost, coins: rows[0].coins };
        const sets = toBuy.map(i => "`" + i.itemName + "`=1").join(', ');
        // coins>=? guards against spending the same coins twice in parallel requests
        const [result] = await connectionms.promise().query(
            `UPDATE \`users\` SET ${sets}, \`coins\`=coins-? WHERE id=? AND password_token=? AND coins>=?`,
            [cost, id, token, cost]);
        if (!result.affectedRows) return { success: false, reason: 'cantafford', cost };
        return { success: true, bought: toBuy.length, cost };
    } catch (err) {
        console.log("Buy all failed:", err.message);
        return { success: false, reason: 'error' };
    }
}

async function buyItem(id, token, itemName) {
    var out = {
        success: false
    };
    if(getCost(itemName) == false) {
        return out;
    }
    for(var i in exclusiveitems) {
        if(exclusiveitems[i].itemName == itemName) {
            out = {
                success: false,
                reason: 'error'
            }
            return out;
        }
    }
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1)) {
            out = {
                success: false,
                reason: 'error'
            }
            return out;
        } else {
            if(rows[0][itemName] == 1) {
                out = {
                    success: false,
                    reason: 'alreadyowned'
                }
                return out;
            } else {
                let cst = INFINITE_COINS ? 0 : getCost(itemName);
                if(rows[0].coins < cst) {
                    out = {
                        success: false,
                        reason: 'cantafford'
                    }
                    return out;
                }
                await connectionms.promise().query(`UPDATE \`users\` SET \`${itemName}\`='1', \`coins\`=coins-${cst} WHERE id=? AND password_token=?`, [id, token])
                .then( ([]) => {
                    out = {
                        success: true
                    }
                    // console.log("da");
                    return out;
                })
                .catch( (err) => {
                    console.log(err);
                    console.log("errr");
                    out = {
                        success: false,
                        reason: 'error'
                    }
                    return out;
                });
            }
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

async function loadShopData(id, token) {
    // Built fresh for each request: the shared shopitems list must never change, or
    // players would see each other's purchases and exclusive items would leak out.
    let row = null;
    try {
        const [rows] = await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token]);
        if (rows.length && rows[0].banned != 1) row = rows[0];
    } catch (err) {
        console.log("Loading the shop failed:", err.message);
        return { success: false };
    }
    const owned = item => row != null && row[item.itemName] == 1;
    // exclusive items are only listed for players who already have them; owned items first
    return shopitems
        .filter(item => !item.isExclusive || owned(item))
        .map(item => ({ ...item, purchasedByPlayer: owned(item) }))
        .sort((a, b) => Number(b.purchasedByPlayer) - Number(a.purchasedByPlayer));
}

async function getAccountData(id) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=?`,[id])
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            // console.log('eerrre');
            out = {
                success: false
            }
            return out;
        } else {
            out = withCoinBalance(rows[0]);
            return out;
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

async function reqloadAccountData(id, token) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`,[id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            // console.log('eerrre');
            out = {
                success: false
            }
            return out;
        } else {
            out = withCoinBalance(rows[0]);
            return out;
        }
    })
    .catch( (err) => {
        console.log("err");
        console.log(err);
        out = {
            success: false
        }
        return out;
    });
    return out;
}

function addBPEXP(pvk, id, token, xp) {
    xp = Number(xp);
    if(pvk == SERVER_KEY) {
        connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`,[id, token])
        .then( async ([rows,fields]) => {
            if(rows.length > 0) {
                let sql = "";
                if(rows[0].xp+xp > getNextLevelXP(rows[0].level)) {
                    if(rows[0].level < seasonlevels) {
                        let newxp = rows[0].xp+xp;
                        let level = rows[0].level;
                        while(newxp > getNextLevelXP(level)) {
                            newxp -= getNextLevelXP(level);
                            level++;
                        }
                        if(level > seasonlevels) level = seasonlevels;
                        sql = "UPDATE `users` SET `xp`="+newxp+", `level`="+level+" WHERE `id`=?";
                    } else {
                        let newxp = rows[0].xp+xp;
                        let coins = coinsperoutlevel;
                        while(newxp > getNextLevelXP(rows[0].level)) {
                            newxp -= getNextLevelXP(rows[0].level);
                            coins += coinsperoutlevel;
                        }
                        sql = "UPDATE `users` SET `xp`="+newxp+", `coins`=`coins`+"+coins+" WHERE `id`=?";
                    }
                } else {
                    sql = "UPDATE `users` SET `xp`=`xp`+"+xp+" WHERE `id`=?";
                }
                if(sql.length > 1) {
                    connectionms.promise().query(sql, [id])
                    .then( async ([rows,fields]) => {
                        //
                    })
                    .catch( (err) => {
                        console.log(err);
                    });
                }
            }
        })
        .catch( (err) => {
            console.log("err");
            console.log(err);
        });
    }
}

function addCoins(pvk, id, token, coins) {
    if(pvk == SERVER_KEY) {
        connectionms.promise().query(`UPDATE \`users\` SET \`coins\`= coins + ${coins} WHERE \`id\`=?`, [id])
        .then( async ([rows,fields]) => {
            //
        })
        .catch( (err) => {
            console.log(err);
        });
    }
}
function sendLog(type, executor, target, reason){
    console.log(`[log] ${type} | executor: ${executor} | target: ${target} | ${reason}`);
}

function warnUser(pvk, id, reason, executor) {
    if(pvk == SERVER_KEY) {
        connectionms.promise().query(`SELECT * FROM \`users\` WHERE \`id\`=?`, [id])
        .then( async ([rows,fields]) => {
            if(rows[0].warns+1 > 2) {
                connectionms.promise().query(`UPDATE \`users\` SET \`admin\`=0 WHERE \`id\`=?`, [id])
                .then( async ([rows,fields]) => {
                    //
                })
                .catch( (err) => {
                    console.log(err);
                });
            }
        })
        .catch( (err) => {
            console.log(err);
        });
        var wrs = ""+executor+"-"+reason+",";
        connectionms.promise().query(`UPDATE \`users\` SET \`warns\`=\`warns\`+1, \`warn_executors\`=CONCAT(warn_executors, ?) WHERE \`id\`=?`, [wrs, id])
        .then( async ([rows,fields]) => {
            //
        })
        .catch( (err) => {
            console.log(err);
        });
    }
}

function banUser(pvk, id, reason, executor, time) {
    time = parseInt(time);
    if(time <= 0){
    if(pvk == SERVER_KEY) {
        connectionms.promise().query(`UPDATE \`users\` SET \`banned\`=1, \`banreason\`=?, \`banexecutor\`=? WHERE \`id\`=?`, [reason, executor, id])
        .then( async ([rows,fields]) => {
            //
        })
        .catch( (err) => {
            console.log(err);
        });
    }
}
if(time > 0){
    if(pvk == SERVER_KEY) {
        let parserTime = new Date()
        parserTime.setMinutes(parserTime.getMinutes() + time);
        connectionms.promise().query(`UPDATE \`users\` SET \`banned\`=1, \`banreason\`=?, \`banexecutor\`=?, \`banTime\`=? WHERE \`id\`=?`, [reason, executor, parserTime, id])
        .then( async ([rows,fields]) => {
            //
        })
        .catch( (err) => {
            console.log(err);
        });
    }
}
}
function unBanUser(pvk, id) {
    if(pvk == SERVER_KEY) {
        connectionms.promise().query(`UPDATE \`users\` SET \`banned\`=0, \`banreason\`=NULL, \`banexecutor\`=NULL, \`banTime\`=NULL WHERE \`id\`=?`, [id])
        .then( async ([rows,fields]) => {
            //
        })
        .catch( (err) => {
            console.log(err);
        });
    }
}

ensureSchema();

// registerUser('sas');

//app.use()
const getRawBody = require('raw-body');

app.use((req, res, next) => {
    if(req.url === "/"){ 
        next();
        return;
    }
    if(req.url === "/sendLog"){ 
        next();
        return;
    }
    if(req.headers.host == "localhost:14002"){
        next();
        return;
    }
    
    sendLog("AccountServer request", "0", "0", `Type ${req.method}\nURL ${req.url}\nHeaders: ${JSON.stringify(req.headers)}`)
    next();
});
var parsedServers = [];
var updateBan = async function(){
    let rowss = [];
    await connectionms.promise().query(`SELECT * FROM \`users\` WHERE \`banned\`=1 AND banTime IS NOT NULL`)
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
    
        } else {
            rows.map((row) => {
            let actualDate = new Date();
            let bannedTime = new Date(row.banTime)
            if(actualDate > bannedTime){
                unBanUser(SERVER_KEY, row.id);
                console.log(`Account with ID: ${row.id} unbanned. Reason: Ban time expired.`);
            }
        })
            
        }
    })
    .catch( (err) => {
        console.log(err);
            });

}
setInterval(updateBan, 10000)
// var html = JSON.stringify(htm);

var jsonParser = bodyParser.json();

app.get('/', function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('failed');
})

app.get('/reglog', async ({ query }, response) => {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await registerUser(encode_utf8(query.accessToken), encode_utf8(query.socialNetwork));
    if(!resp.success) console.log(`Google sign-in failed: ${resp.reason || 'unknown'}`);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/verify', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await verifyUser(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken));
    // console.log(resp);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/battlepass', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await getBattlePass(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken));
    // console.log(resp);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/loadAccountData', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await loadAccountData(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken));
    // console.log(resp);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/loadShopItems', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await loadShopData(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken));
    // console.log(resp);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/buyAllShopItems', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await buyAllItems(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken), request.query.items);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/buyShopItem', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await buyItem(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken), encode_utf8(request.query.itemName));
    // console.log(resp);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

app.get('/getReward', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await getBattlePassReward(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken), request.query.level, request.query.gold);
    // console.log(resp);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(resp));
})

// Checks a reCAPTCHA v3 token with Google for the game server. Always answers with a
// google_response object, so the game server can read the result even when the check fails.
const handleSend = async (req, res) => {
    const token = req.body && typeof req.body.token === 'string' ? req.body.token : '';
    if (!googleKey) {
        return res.json({ google_response: { success: false, 'error-codes': ['missing-input-secret'] } });
    }
    try {
        const response = await fetch('https://www.google.com/recaptcha/api/siteverify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ secret: googleKey, response: token }),
        });
        const google_response = await response.json();
        res.json({ google_response });
    } catch (err) {
        console.log("reCAPTCHA check failed:", err.message);
        res.json({ google_response: { success: false, 'error-codes': ['siteverify-unreachable'] } });
    }
};
app.post('/verifykey', jsonParser, handleSend);

app.post('/', jsonParser, function(request, response) {
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('failed');
})

app.post('/reqplr', jsonParser, async function(request, response) {
    // console.log(request.body.id);
    let reesp = await reqloadAccountData(encode_utf8(request.body.id), encode_utf8(request.body.token));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(reesp));
})

app.post('/sendLog', jsonParser, async function(request, response) {
    setTimeout(() => {
    sendLog(encode_utf8(request.body.requestType), encode_utf8(request.body.executor), encode_utf8(request.body.targetId), encode_utf8(request.body.reason));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('');
    }, 2000)
})
app.post('/a2pcoins', jsonParser, async function(request, response) {
    addCoins(encode_utf8(request.body.pvk), encode_utf8(request.body.id), encode_utf8(request.body.token), encode_utf8(request.body.coins));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('');
})

app.post('/addbpexp', jsonParser, async function(request, response) {
    addBPEXP(encode_utf8(request.body.pvk), encode_utf8(request.body.id), encode_utf8(request.body.token), encode_utf8(request.body.xp));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('');
})

app.post('/banuserpzl', jsonParser, async function(request, response) {
    banUser(encode_utf8(request.body.pvk), encode_utf8(request.body.id), encode_utf8(request.body.reason), encode_utf8(request.body.executor), encode_utf8(request.body.time));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('');
})

app.post('/warnuserpzl', jsonParser, async function(request, response) {
    warnUser(encode_utf8(request.body.pvk), encode_utf8(request.body.id), encode_utf8(request.body.reason), encode_utf8(request.body.executor));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end('');
})

// HTTPS is optional: set SSL_KEY and SSL_CERT to file paths to enable it.
if (process.env.SSL_KEY && process.env.SSL_CERT) {
    const port = 2083;
    const credentials = {key: fs.readFileSync(process.env.SSL_KEY), cert: fs.readFileSync(process.env.SSL_CERT)};
    https.createServer(credentials, app).listen(port);
    console.log(`Listening at ${port}`);
}

var localserv = http.createServer(app);
localserv.listen(14002);
console.log(`Local listening at 14002`);


parseSeasons();

