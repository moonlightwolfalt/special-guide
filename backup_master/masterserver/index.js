var express = require('express');
var fs = require('fs');
var app = express();
var https = require('https');
var http = require('http');
var bodyParser = require('body-parser');
var mainKey = process.env.MASTER_KEY || "REMOVED_DUE_TO_SENSITIVE_DATA";

//app.use()

var servers = [];

function addServer(ip, key) {
    servers.push(
        {
            "name": ip,
            "key": key,
            "pl": 0,
            "gm": 0,
            "ofl": false,
            "updateTime": 0
        }
    )
    console.log("Added server - " + ip);
}

addServer(process.env.GAME_SERVER_NAME || "localhost", process.env.GAME_SERVER_KEY || "REMOVED_DUE_TO_SENSITIVE_DATA");

function parseServer(get) {
    var key = get.pvk;
    var players = get.players;
    var gm = get.gm;
    var updateTime = +new Date();
    for(var i in servers) {
        if(key == mainKey+servers[i].key) {
            servers[i].pl = players;
            servers[i].updateTime = updateTime;
            servers[i].ofl = false;
            servers[i].gm = gm;
        }
    }
    serversCapitalize();
}

var parsedServers = [];

var htm = {
    "servers": parsedServers,
    "totalPlayersOnline": 0
};

var html = JSON.stringify(htm);

var totalPlayers = 0;

function serversCapitalize() {
    var res = [];
    totalPlayers = 0;
    for(var i in servers) {
        if(servers[i].updateTime+15000 > +new Date()) {
            servers[i].ofl = false;
        } else {
            servers[i].ofl = true;
        }
        var r = {
            "name": servers[i].name,
            "pl": servers[i].pl,
            "ofl": servers[i].ofl,
            "gm": servers[i].gm
        }
        totalPlayers += servers[i].pl;
        res.push(r);
    }
    parsedServers = res;
    htm = {
        "servers": parsedServers,
        "totalPlayersOnline": totalPlayers
    };
    html = JSON.stringify(htm);
}

var jsonParser = bodyParser.json();

app.get('/', function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    serversCapitalize();
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(html);
})

app.post('/', jsonParser, function(request, response) {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Allow-Headers", "X-Requested-With");
    parseServer(request.body);
    response.writeHead(200, {'Content-Type': 'text/html'});
    response.end(html);
})

// HTTPS is optional: set SSL_KEY and SSL_CERT to file paths to enable it.
if (process.env.SSL_KEY && process.env.SSL_CERT) {
    const port = 2096;
    const credentials = {key: fs.readFileSync(process.env.SSL_KEY), cert: fs.readFileSync(process.env.SSL_CERT)};
    https.createServer(credentials, app).listen(port);
    console.log(`Listening at ${port}`);
}

var localserv = http.createServer(app);
localserv.listen(14001);
console.log(`Local listening at 14001`);