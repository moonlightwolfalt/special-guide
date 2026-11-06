var express = require('express');
var fs = require('fs');
var app = express();
var https = require('https');
var http = require('http');
var bodyParser = require('body-parser');
var certificate = fs.readFileSync('REMOVED_DUE_TO_SENSITIVE_DATA');
var privatekey = fs.readFileSync('REMOVED_DUE_TO_SENSITIVE_DATA');
const fetch = (...args) => import('node-fetch').then(({default: fetch}) => fetch(...args));
const { Client, GatewayIntentBits, Collection, time, EmbedBuilder } = require('discord.js');
const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.GuildMembers ] });
const { REST } = require('@discordjs/rest');
const { Routes } = require('discord.js');
const { restartServer } = require('./start.js');
var _ = require('lodash');
const googleKey = "REMOVED_DUE_TO_SENSITIVE_DATA";
var season = 0;

var credentials = {key: privatekey, cert: certificate};

var coinsperoutlevel = 2000;

//DB

var mysql = require('mysql2');
var connectionms = mysql.createPool({
  host     : '127.0.0.1',
  user     : 'REMOVED_DUE_TO_SENSITIVE_DATA',
  password : 'REMOVED_DUE_TO_SENSITIVE_DATA',
  database : 'REMOVED_DUE_TO_SENSITIVE_DATA',
  multipleStatements: false
});

var encode_utf8 = function(s) {
    return unescape(encodeURIComponent(s));
}

function testDB() {
    connectionms.query('SELECT * FROM users WHERE id=1', function (error, results, fields) {
        if (error) {
            console.log(error);
        } else {
            console.log("Connection estabilished!");
        }
    });
}

let seasondata = null;
let seasonlevels = -1;

function parseSeasons() {
    let data = fs.readFileSync('seasons.json');
    data = JSON.parse(data);
    data = data[season];
    seasondata = data;
    seasonlevels = seasondata.levels.free.length;
    console.log("Season "+season+" config loaded! Levels: "+seasonlevels);
}

async function registerUser(token, social, code) {
    // console.log(code)
    var out = {
        success: false
    };
    let email, email_ver, name, picture, given_name, family_name, locale = null;
    let dsID, dsTag, mfaEnabled = null;
    let query1, query1add = null;
    if(social == "google") {
        let response = await fetch('https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=' + token, {
            method: 'GET',
            body: null,
            headers: {'Content-Type': 'application/json'}
        });
        let data = await response.json();
        if(!data || data.error_description) {
            return out;
        }

        email = data.email;
        email_ver = data.email_verified;
        name = data.name;
        picture = data.picture;
        given_name = data.given_name;
        family_name = data.family_name;
        locale = data.locale;
        query1 = `SELECT * FROM users WHERE email=?`;
        query1add = [email];
    } else if(social == 'discord' && token) {
        const tokenResponseData = await fetch('https://discord.com/api/oauth2/token', {
				method: 'POST',
				body: new URLSearchParams({
					client_id: "REMOVED_DUE_TO_SENSITIVE_DATA",
					client_secret: "REMOVED_DUE_TO_SENSITIVE_DATA",
					code: token,
					grant_type: 'authorization_code',
					redirect_uri: `https://mope2020.fun`,
					scope: 'identify',
				}).toString(),
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
				},
			});


			const oauthData = await tokenResponseData.json();

            // console.log(oauthData)


            
            
        let response = await fetch('https://discord.com/api/users/@me', {
            method: 'GET',
            body: null,
            headers: {
                'Content-Type': 'application/json',
                authorization: `${oauthData.token_type} ${oauthData.access_token}`,

            }
        });
        let data = await response.json();
        // console.log(data);
        if(!data) {
            return out;
        }
        if(!data.id || !data.avatar || !data.username) return out;
        dsID = data.id;
        dsTag = data.username + '#' + data.discriminator;
        // mfaEnabled = data.mfa_enabled == 1 ? "true" : "false";
        email = "none";
        email_ver = "false";
        name = dsTag;
        picture = "https://cdn.discordapp.com/avatars/"+data.id+"/"+data.avatar;
        given_name = data.username;
        family_name = data.discriminator;
        locale = data.locale;
        query1 = `SELECT * FROM users WHERE discordID=?`;
        query1add = [dsID];
    }
    if(query1 == null)
        return out;
    await connectionms.promise().query(query1, query1add)
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            let massiv = [email,social,email_ver,name,picture,locale,token,+new Date()+21600000];
            if(social == "discord") {
                massiv.push(dsID);
                massiv.push(dsTag);
                // massiv.push(mfaEnabled);
            }
            await connectionms.promise().query('INSERT INTO `users`(`email`, `social`, `email_isVerified`, `name`, `profilePicUrl`, `locale`, `password_token`, `expirydate`'+ (social == "discord" ? ", `discordID`, `discordTag`" : "") +') VALUES (?,?,?,?,?,?,?,?'+ (social == "discord" ? ",?,?" : "") +')', massiv)
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
                        success: false
                    }
                    return out;
                });
            })
            .catch( (err) => {
                console.log(err);
                console.log("errr");
                out = {
                    success: false
                }
                return out;
            });
        } else {
            let query2, query2add = null;
            query2add = [token, +new Date()+21600000];
            if(social == "google") {
                query2 = `UPDATE \`users\` SET \`password_token\`=?, \`expirydate\`=? WHERE \`email\`=?`;
                query2add.push(email);
            } else if(social == "discord") {
                query2 = `UPDATE \`users\` SET \`password_token\`=?, \`expirydate\`=? WHERE \`discordID\`=?`;
                query2add.push(dsID);
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
                    success: false
                }
                return out;
            });
        }
    })
    .catch( (err) => {
        console.log(err);
        console.log("errr");
        out = {
            success: false
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

async function discordCode(id, token) {
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
            if(rows[0].discordTag == null) {
                let code = Math.floor(Math.random() * (9999 - 1000 + 1)) + 1000;
                await connectionms.promise().query(`UPDATE \`users\` SET \`discordCode\`=? WHERE id=? AND password_token=?`, [code, id, token])
                .then((rows) => {
                    out = {
                        success: true,
                        code: code
                    }
                    return out;
                })
                .catch( (err) => {
                    console.log(err);
                    out = {
                        success: false
                    }
                    return out;
                });
            } else {
                out = {
                    success: false,
                    type: 1,
                    code: rows[0].discordTag
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

async function setDiscord(id, discord) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=?`, [id])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1)) {
            out = {
                success: false
            }
            return out;
        } else {
            if(rows[0].discordTag == null) {
                await connectionms.promise().query(`UPDATE \`users\` SET \`discordTag\`=?,\`discordID\`=?,\`discordCode\`=NULL WHERE id=?`, [discord.username + '#' + discord.discriminator, discord.id, id])
                .then((rows) => {
                    out = {
                        success: true
                    }
                    return out;
                })
                .catch( (err) => {
                    console.log(err);
                    out = {
                        success: false
                    }
                    return out;
                });
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

async function unlinkDiscord(id) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=?`,[id])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1) || (rows[0] && rows[0].social == "discord")) {
            out = {
                success: false,
                reason: ((rows[0] && rows[0].social) == "discord" ? "This account registered by Discord." : (rows[0] && rows[0].banned == 1) ? "This account is banned." : (rows.length == 0) ? "Account not exist." : null)
            }
            return out;
        } else {
            if(rows[0].discordTag != null) {
                let code = Math.floor(Math.random() * (9999 - 1000 + 1)) + 1000;
                await connectionms.promise().query(`UPDATE \`users\` SET \`discordTag\`=NULL,\`discordID\`=NULL,\`discordCode\`=NULL WHERE id=?`, [id])
                .then((rows) => {
                    out = {
                        success: true
                    }
                    return out;
                })
                .catch( (err) => {
                    console.log(err);
                    out = {
                        success: false,
                        reason: err
                    }
                    return out;
                });
            } else {
                out = {
                    success: false,
                    reason: "No discord tag linked"
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

async function getDiscordCode(id) {
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=?`, [id])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1)) {
            out = {
                success: false
            }
            return out;
        } else {
            out = {
                success: true,
                code: rows[0].discordCode
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
                coins: rows[0].coins,
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
                let cst = getCost(itemName);
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
    var out = {
        success: false
    };
    await connectionms.promise().query(`SELECT * FROM users WHERE id=? AND password_token=?`, [id, token])
    .then( async ([rows,fields]) => {
        if(rows.length == 0 || (rows[0] && rows[0].banned == 1)) {
            out = shopitems;
            return out;
        } else {
            let modshopitems = [];
            let shopitemssorted = shopitems;
            for(var i in shopitemssorted) {
                if(shopitemssorted[i].isExclusive && rows[0][shopitems[i].itemName] != 1) {
                    var index = shopitemssorted.indexOf(shopitemssorted[i]);
                    if (index > -1) {
                        shopitemssorted.splice(index, 1);
                    }
                }
            }
            shopitemssorted.sort((prev, next) => {
                if(rows[0][next.itemName] == 1) {
                    return -1;
                } else {
                    return 1;
                }
            });
            for(var i = 0; i < shopitems.length; i++) {
                let item = shopitems[i];
                if(rows[0][shopitems[i].itemName] == 1) {
                    item.purchasedByPlayer = true;
                } else {
                    item.purchasedByPlayer = false;
                }
                modshopitems.push(item);
            }
            out = modshopitems;
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
            out = rows[0];
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
            out = rows[0];
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
    if(pvk == 'REMOVED_DUE_TO_SENSITIVE_DATA') {
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
    if(pvk == 'REMOVED_DUE_TO_SENSITIVE_DATA') {
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
    let isAccLog = false;
    let channelID = "1244783880133939211"
    if(type.startsWith("package") || type.startsWith("AccountServer") || type.startsWith("")){
        channelID = "1255305554037968957"
        isAccLog = true;
    }
    let embed = new EmbedBuilder()
    .setTitle("💾 LOG 💾")
    .setColor("Random")
    .addFields(
        {name: 'Action Type', value: `${type}`},
        {name: 'Executor (Account ID)', value: `${executor}`},
        {name: 'Target ID', value: `${target}`},
    )
    if(isAccLog)
        embed.setDescription(`${reason}`);
    else embed.addFields({name: 'Reason', value: `${reason}`})

    client.channels.cache.get(channelID).send({
        embeds: [embed]
    }).catch(e => console.log(e));

    
}

function warnUser(pvk, id, reason, executor) {
    if(pvk == 'REMOVED_DUE_TO_SENSITIVE_DATA') {
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
    if(pvk == 'REMOVED_DUE_TO_SENSITIVE_DATA') {
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
    if(pvk == 'REMOVED_DUE_TO_SENSITIVE_DATA') {
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
    if(pvk == 'REMOVED_DUE_TO_SENSITIVE_DATA') {
        connectionms.promise().query(`UPDATE \`users\` SET \`banned\`=0, \`banreason\`=NULL, \`banexecutor\`=NULL, \`banTime\`=NULL WHERE \`id\`=?`, [id])
        .then( async ([rows,fields]) => {
            //
        })
        .catch( (err) => {
            console.log(err);
        });
    }
}

function discordAuth() {
    //
}

testDB();

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
                unBanUser("REMOVED_DUE_TO_SENSITIVE_DATA", row.id);
                client.channels.cache.get("1244783880133939211").send(`Account with ID: \`${row.id}\` unbanned\nReason: Ban time expired.`)
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

app.get('/discord', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await discordCode(encode_utf8(request.query.userId), encode_utf8(request.query.passwordToken));
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

const handleSend = (req, res) => {
    let token = req.body.token;
    let url = `https://www.google.com/recaptcha/api/siteverify?secret=${googleKey}&response=${token}`;

    fetch(url, {
        method: 'post'
    })
        .then(response => response.json())
        .then(google_response => res.json({ google_response }))
        .catch(error => res.json({ error }));
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

const port = 2083;
var httpsServer = https.createServer(credentials, app);
httpsServer.listen(port);
console.log(`Listening at ${port}`);

var localserv = http.createServer(app);
localserv.listen(14002);
console.log(`Local listening at 14002`);

//discord

console.log("Discord Initializing...");

const dcToken = "REMOVED_DUE_TO_SENSITIVE_DATA";

const clientId = 'REMOVED_DUE_TO_SENSITIVE_DATA';
const guildId = 'REMOVED_DUE_TO_SENSITIVE_DATA';

const commands = [];
const commandFiles = fs.readdirSync('./commands').filter(file => file.endsWith('.js'));

for (let file of commandFiles) {
	let command = require(`./commands/${file}`);
    commands.push(command.data.toJSON());
}

const rest = new REST({ version: '10' }).setToken(dcToken);

(async () => {
	try {
		console.log('Started refreshing application (/) commands.');

		await rest.put(
			Routes.applicationCommands(clientId),
			{ body: commands },
		);

		console.log('Successfully reloaded application (/) commands.');
	} catch (error) {
		console.error(error);
	}
})();

var adminsIDs = [];

async function fetchAdmins() {
    await connectionms.promise().query(`SELECT * FROM \`users\` WHERE \`discordAdmin\`=1 AND discordID IS NOT NULL`)
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            console.log("no discord admins");
        } else {
            adminsIDs = [];
            for(var i = 0; i< rows.length; i++) {adminsIDs.push(rows[i].discordID);}
        }
    })
    .catch( (err) => {
        console.log(err);
    });
}

async function getAccountByDiscord(dcid) {
    var out = null;
    await connectionms.promise().query(`SELECT * FROM \`users\` WHERE \`discordID\`='${dcid}'`)
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            out = null;
            return out;
        } else {
            out = rows[0];
            return out;
        }
    })
    .catch( (err) => {
        console.log(err);
        out = null;
        return out;
    });
    return out;
}

async function getAccountByID(id) {
    var out = null;
    await connectionms.promise().query(`SELECT * FROM \`users\` WHERE \`id\`='${id}'`)
    .then( async ([rows,fields]) => {
        if(rows.length == 0) {
            out = null;
            return out;
        } else {
            out = rows[0];
            return out;
        }
    })
    .catch( (err) => {
        console.log(err);
        out = null;
        return out;
    });
    return out;
}

function checkAdmin(id) {
    for(var i in adminsIDs) {
        if(Number(adminsIDs[i]) == Number(id)) return true;
    }
    return false;
}

fetchAdmins();

var emoji_mopecoin = ":moneybag:";

client.on('ready', () => {
    console.log(`Discord is ready!`);
    client.user.setPresence({ activities: [{ type: "WATCHING", name: '/help' }] });
});

client.on('interactionCreate', async interaction => {
	if (!interaction.isCommand()) return;

    try {
        var whiteList = ["828722395485044756", "1087895305254608896", "664806978991685633", "724553482626924625", "383861914301759489", "831202707728629770", "819295905526054942"];

        if(interaction.commandName === "ban"){
            if(!whiteList.includes(interaction.user.id)) return interaction.reply({
                content: "Not enought permissions.",
                ephemeral: true
            });
            let id = interaction.options.getNumber("id")
            let reason = interaction.options.getString("reason")
            let time = interaction.options.getNumber("time")
            let myAccount = await getAccountByDiscord(interaction.user.id)
            if(!myAccount) return interaction.reply({
                content: "Your Discord account wasn't found, please use the command /auth.",
                ephemeral: true
            })
            if(id == 1 || id == 81) return interaction.reply({
                content: "Not enought permissions.",
                ephemeral: true
            });
            let acc = await getAccountByID(id);
            if(!acc) return interaction.reply({content: "Account not found"});

            if(acc.banned == 1) return interaction.reply({
                content: "Account already banned.",
                ephemeral: true
            })
         
            banUser("REMOVED_DUE_TO_SENSITIVE_DATA", id, reason, myAccount.id, time);
            client.users.cache.get("828722395485044756").send({
                content: `Account banned\nID: \`${id}\`\nReason: \`\`\`yml\n${reason}\`\`\`\nTime: \`${time}m\` (0m = Permanent ban)\nExecutorId: \`${myAccount.id}\``
            })


        }
        if(interaction.commandName === "unban"){
            if(!whiteList.includes(interaction.user.id)) return interaction.reply({
                content: "Not enought permissions.",
                ephemeral: true
            });
            let id = interaction.options.getNumber("id")
            let myAccount = await getAccountByDiscord(interaction.user.id)
            if(!myAccount) return interaction.reply({
                content: "Your Discord account wasn't found, please use the command /auth."
            })
            let acc = await getAccountByID(id);
            if(!acc) return interaction.reply({content: "Account not found"});

            if(acc.banned == 0) return interaction.reply({
                content: "Account isn't banned.",
                ephemeral: true
            })
         
            unBanUser("REMOVED_DUE_TO_SENSITIVE_DATA", id);
            client.users.cache.get("828722395485044756").send({
                content: `Account unbanned\nID:\`${id}\`\nExecutorId:\`${myAccount.id}\``
            })
            return interaction.reply({
                content: "Account unbanned."
            })

        }

        if(interaction.commandName === "checkaccount"){
            if(!whiteList.includes(interaction.user.id)) return interaction.reply({
                content: "Not enought permissions.",
                ephemeral: true
            });
            let id = interaction.options.getNumber("id")
            let acc = await getAccountByID(id);
            if(!acc) return interaction.reply({content: "Account not found"});
            let msg = `💾 ${acc.name}'s account 💾`
            msg+=`\nCoins \`${acc.coins}\``
            msg+=`\nadmin \`${acc.admin}\``
            msg+=`\nlimited \`${acc.limited}\``
            msg+=`\nbanned \`${acc.banned == null ? "0" : acc.banned}\``
            if(acc.discordTag !== null) msg+=`\nDiscord User\`${acc.discordTag}\``

            return interaction.reply({
                content: msg,
                ephemeral: true
            }
            )

        }
        if(interaction.commandName === "restart"){
            return;
            let whiteList1 = ["828722395485044756", "724553482626924625", "1087895305254608896", "383861914301759489", "664806978991685633"]
            if(!whiteList1.includes(interaction.user.id)){ return interaction.reply({
                content: "Not enought permissions.",
                ephemeral: true
            });
        }
         
            restartServer();
            

             await interaction.reply({
                content: "Server should be restarting.",
                ephemeral: true
            }
            )

        }
        if (interaction.commandName === 'ping') {
            await interaction.reply({ content: 'Pong!', ephemeral: true });
        } else if (interaction.commandName === 'auth') {
            let acc = await getAccountByDiscord(interaction.user.id);
            if(!acc) {
                let code = interaction.options.getString('code');
                let id = interaction.options.getString('id');
                if(!code || !id) return await interaction.reply({ content: 'something goes wrong...', ephemeral: true });
                let validcode = await getDiscordCode(id);
                if(validcode.code == code) {
                    let res = await setDiscord(id, interaction.user);
                    if(res.success == true) await interaction.reply({ content: `Successful linked! Refresh page in your game.`, ephemeral: true }); // yesd.io нужно вернуть команду на получение инфы про других игроков эфф
                    else await interaction.reply({ content: 'Link failed!', ephemeral: true });
                }
                else await interaction.reply({ content: `Wrong code!`, ephemeral: true });
            } else {
                await interaction.reply({ content: `Your Discord account already linked with game account id ` + acc.id + '!', ephemeral: true });
            }
        } else if (interaction.commandName === 'refresh') {
            if(!checkAdmin(interaction.user.id)) {
                await interaction.reply({ content: 'You can\'t do this!', ephemeral: true });
            } else {
                fetchAdmins();
                await interaction.reply({ content: 'Refreshed!', ephemeral: true });
            }
        } else if (interaction.commandName === 'stats') {
            let acc = await getAccountByDiscord(interaction.user.id);
            if(!acc) {
                await interaction.reply({ content: 'Your Discord account not linked to game account!', ephemeral: true });
            } else {
                let msg = ':floppy_disk: Your game account:\n\nNick: ' + acc.name + '\nCoins: ' + acc.coins + ' ' + emoji_mopecoin;
                msg += "\nYour admin lvl is"+acc.admin;
                if(acc.admin > 0) msg += "\nYour default nick: `" + acc.persistentName + '`';
                await interaction.reply({ content: msg, ephemeral: true });
            }
        } else if (interaction.commandName === 'unlink') {
            if(!checkAdmin(interaction.user.id)) {
                await interaction.reply({ content: 'You can\'t do this!', ephemeral: true });
            } else {
                let type = interaction.options.getString('type');
                if(type == 'discord') {
                    let user = interaction.options.getUser('tag');
                    if(!user) return await interaction.reply({ content: 'Please, specify the tag!', ephemeral: true });
                    let id = user.id;
                    let acc = await getAccountByDiscord(id);
                    if(!acc) {
                        await interaction.reply({ content: 'This Discord account is not linked to any game account!', ephemeral: true });
                    } else {
                        let ae = await unlinkDiscord(acc.id);
                        if(ae.success) await interaction.reply({ content: 'Unlinked.', ephemeral: true });
                        else await interaction.reply({ content: 'Something goes wrong when unlinking. Aborted.'+(ae.reason ? "\nReason: "+ae.reason : ""), ephemeral: true });
                    }
                } else {
                    let user = interaction.options.getString('id');
                    if(!user) return await interaction.reply({ content: 'Please, specify the id!', ephemeral: true });
                    let id = user;
                    let acc = await getAccountByID(id);
                    if(!acc) {
                        await interaction.reply({ content: 'This game account is not linked to any Discord account!', ephemeral: true });
                    } else {
                        let ae = await unlinkDiscord(id);
                        if(ae.success) await interaction.reply({ content: 'Unlinked.', ephemeral: true });
                        else await interaction.reply({ content: 'Something goes wrong when unlinking. Aborted.'+(ae.reason ? "\nReason: "+ae.reason : ""), ephemeral: true });
                    }
                }
            }
        } else {
            await interaction.reply({ content: 'something goes wrong...', ephemeral: true });
        }
    }catch(e) {
        console.error(e);
    }
});
var lastT = null;
// setInterval(() => {sendServerStatus("1195134136227205230")}, 60 * 1000)
// async function sendServerStatus(channelID){
//     let channel = await client.channels.cache.get(channelID);
//     let message = await channel.messages.fetch({limit: 10}).then((msg) => msg.filter(m => m.author.id === client.user.id).last());
//     let embed = new EmbedBuilder()
//     .setTitle("Server Status")
//     .setColor('Red')
//     .addFields(
//         {name: 'Players', value: '0'},//<a:online:1195128022739726398> 
//         {name: 'Status', value: '<a:offline:1195128150045229106>'},
//         // {name: 'Last update', value: '???'},
//         {name: 'Next Update', value: '???'},
//         {name: 'Current GameMode', value: "???"}
//     )
//     .setThumbnail(client.user.displayAvatarURL({forceStatic: true}));

//     try{
//     fetch('http\://localhost\:14001').then(response => {
//         if(!response.ok)
//         return console.log("Something went wrong...")

//         let result =  response.json();
//         return result;
//     }).then(async data => {
//         if(!data) return console.log("Something went wrong..");
//         embed.data.fields[0].value = data.totalPlayersOnline;
//         // data.servers.map(())
//         embed.data.fields[1].value = data.servers[0].ofl ? "<a:offline:1195128150045229106>" : "<a:online:1195128022739726398>";
//         // lastT =  await time(new Date(Date.now()), "R") 
//         // embed.data.fields[2].value = lastT;
//         let nextUpd = new Date(Date.now())
//         embed.data.fields[2].value = await time(new Date(nextUpd.setSeconds(nextUpd.getMinutes() + 1)), "R")
//         embed.data.fields[3].value = data.servers[0].gm == 2 ? "Battle Royale" : data.servers[0].gm == 0 ? "Normal" : data.servers[0].gm

//         if(data.servers[0].ofl){
//              embed.setColor('Red')
//              embed.data.fields[0].value = "0"
//         }
//         if(!data.servers[0].ofl) embed.setColor('Green')
//         if(message){
//         message.edit({embeds: [], content: "Loading..."});
//         message.edit({embeds: [embed], content: ""});
//         }
//         if(!message) channel.send({embeds: [embed]})
//     }).catch(() => {})
//     }catch(e){
//         console.log(e);
//     }


// }

client.login(dcToken);

parseSeasons();

