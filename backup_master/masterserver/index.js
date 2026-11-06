var express = require('express');
var fs = require('fs');
var app = express();
var https = require('https');
var http = require('http');
var bodyParser = require('body-parser');
var certificate = fs.readFileSync('REMOVED_DUE_TO_SENSITIVE_DATA');
var privatekey = fs.readFileSync('REMOVED_DUE_TO_SENSITIVE_DATA');


var credentials = {key: privatekey, cert: certificate};

var mainKey = "REMOVED_DUE_TO_SENSITIVE_DATA";

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

addServer("gameserver.mope2020.fun", "REMOVED_DUE_TO_SENSITIVE_DATA");

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

const port = 2096;
var httpsServer = https.createServer(credentials, app)
httpsServer.listen(port);
console.log(`Listening at ${port}`);

var localserv = http.createServer(app);
localserv.listen(14001);
console.log(`Local listening at 14001`);