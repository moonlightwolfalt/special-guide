const Discord = require('discord.js')
var express = require('express');
var app = express();
const  bodyParser = require('body-parser');

const fs = require('fs')
const https = require('https')
const http = require('http')
const client = new Discord.Client({
    intents: ['Guilds', 'GuildMessages', 'MessageContent', 'GuildMembers']
})
var mysql = require('mysql2');
var connectionms = mysql.createPool({
  host     : '127.0.0.1',
  user     : 'REMOVED_DUE_TO_SENSITIVE_DATA',
  password : 'REMOVED_DUE_TO_SENSITIVE_DATA',
  database : 'REMOVED_DUE_TO_SENSITIVE_DATA',
  multipleStatements: false
});
var privatekey = fs.readFileSync('REMOVED_DUE_TO_SENSITIVE_DATA');
var certificate = fs.readFileSync('REMOVED_DUE_TO_SENSITIVE_DATA');

var credentials = {key: privatekey, cert: certificate};
var encode_utf8 = function(s) {
    return unescape(encodeURIComponent(s));
}
const port = 7040;
var httpsServer = https.createServer(credentials, app);
var jsonParser = bodyParser.json();

httpsServer.listen(port);
console.log(`Listening at ${port}`);

var localserv = http.createServer(app);
localserv.listen(7030);

function testDB() {
    connectionms.query('SELECT * FROM users WHERE id=1', function (error, results, fields) {
        if (error) {
            console.log(error);
        } else {
            console.log("Connection estabilished!");
        }
    });

}
app.get('/reglog', async function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    let resp = await registerUser(encode_utf8(request.query.accessToken), "google");
    // console.log(resp);
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
app.post('/reqplr', jsonParser, async function(request, response) {
    // console.log(request.body.id);
    let reesp = await reqloadAccountData(encode_utf8(request.body.id), encode_utf8(request.body.token));
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(JSON.stringify(reesp));
})

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
async function registerUser(token, social) {
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
    } else if(social == 'discord') {
        let response = await fetch('https://discord.com/api/users/@me', {
            method: 'GET',
            body: null,
            headers: {
                'Content-Type': 'application/json',
                authorization: `${token}`,
            }
        });
        let data = await response.json();
        if(!data) {
            return out;
        }
        dsID = data.id;
        dsTag = data.username + '#' + data.discriminator;
        mfaEnabled = data.mfa_enabled == 1 ? "true" : "false";
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
                massiv.push(mfaEnabled);
            }
            await connectionms.promise().query('INSERT INTO `users`(`email`, `social`, `email_isVerified`, `name`, `profilePicUrl`, `locale`, `password_token`, `expirydate`'+ (social == "discord" ? ", `discordID`, `discordTag`, `mfaEnabled`" : "") +') VALUES (?,?,?,?,?,?,?,?'+ (social == "discord" ? ",?,?,?" : "") +')', massiv)
            .then(async ([rows,fields]) => {
                await connectionms.promise().query(query1, query1add)
                .then( ([rows,fields]) => {
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
client.on('ready', () => {
    console.log("Bot conencted!")
})
client.login("REMOVED_DUE_TO_SENSITIVE_DATA")

testDB()