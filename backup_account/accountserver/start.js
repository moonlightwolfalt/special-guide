const {spawn} = require('child_process');
const { Console } = require('console');
const { ModalSubmitFieldsResolver } = require('discord.js');

let gameServerProcess;


function startServer()
{
    gameServerProcess = spawn('mvn', ['clean package'], {cwd:'/root/backup_server/server'});
   console.log('starting game server...');
   gameServerProcess = spawn('java', ['-jar ./target/MopeSBoxServer-1.0-SNAPSHOT.jar -Dfile.encoding=UTF-8'], {cwd:'/root/backup_server/server'});

}
function closeServer() 
{
    if(gameServerProcess)
    {
        console.log('stopping the game server');
        try
        {
            gameServerProcess.kill();
        }
        catch(error)
        {
            console.error('Error stopping master server', error);
        }
        gameServerProcess = null
    }
}
function restartServer(){
closeServer();
startServer();
}

module.exports = { startServer, closeServer, restartServer };


