package io.mopesbox.Server;

import io.mopesbox.Networking.Networking;
import io.mopesbox.Utils.SSL;
import io.mopesbox.Main;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Constants;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
public class GameServer extends WebSocketServer {
    public ConcurrentHashMap<WebSocket, GameClient> clients = new ConcurrentHashMap<WebSocket, GameClient>();

    public GameServer(int port) {
        super(new InetSocketAddress("0.0.0.0", port));
        if (Constants.USINGSSL) {
            this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(SSL.getContext()));
        }
        this.setReuseAddr(true);
        
  
        // this.logger = Main.log;
    }
     


    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
   String ip = clientHandshake.getFieldValue("CF-Connecting-IP");
        if (ip == null || ip.isEmpty()) {
            ip = clientHandshake.getFieldValue("X-Forwarded-For");
        }
        
        // String ip = webSocket.getRemoteSocketAddress().getAddress().toString();
 

        String userAgent = clientHandshake.getFieldValue("User-Agent");
        String acceptLang = clientHandshake.getFieldValue("Accept-Language");


        if (userAgent.equals("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.74 Safari/537.36 Edg/99.0.1150.55") && acceptLang.equals("ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7")) {
            if (webSocket.isOpen())
            webSocket.send(Networking.popup("MOPERR_69420", "error", 5).getData());
            
            webSocket.close();
        }
        
        if (this.clients.get(webSocket) == null && !Main.instance.room.ipblock.checkIP(ip)) {
            final GameClient newClient = new GameClient(Main.instance.room, webSocket);
            newClient.setIP(ip);
            this.clients.put(webSocket, newClient);
            newClient.onOpen();
            

        } else {
            if (webSocket.isOpen())
                webSocket.send(Networking.popup("MOPERR_691", "error", 5).getData());
            webSocket.close();
        }
    }
        
    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        try {
            this.clients.get(webSocket).onClose();
            if(this.clients.get(webSocket).saveSession){
                Main.instance.sessionManager.saveSession(this.clients.get(webSocket));
            }

            this.clients.remove(webSocket);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        // empty this was required because of implement. oh gosh java i already hate you
        // :P
        
    }

    
    @Override
    public void onMessage(WebSocket webSocket, final ByteBuffer blob) {
        try {
            GameClient e = this.clients.get(webSocket);
            if(e == null) return;
            e.onMessage(blob.array());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        // this.logger.info("Started Websocket server.");
        Main.instance.guiThread.changeWebsocketStatus(true);
    }
    
}
