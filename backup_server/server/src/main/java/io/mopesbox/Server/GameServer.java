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
     


    // The player's IP. Behind a proxy (Cloudflare, Sevalla, server.js) the socket address is
    // the proxy's, so use CF-Connecting-IP, else the first X-Forwarded-For entry. Without
    // either, fall back to the socket address so players never share an empty IP (the
    // game allows only a few connections per IP).
    private static String clientIP(WebSocket webSocket, ClientHandshake handshake) {
        String ip = handshake.getFieldValue("CF-Connecting-IP");
        if (ip != null && !ip.trim().isEmpty()) return ip.trim();
        String forwarded = handshake.getFieldValue("X-Forwarded-For");
        if (forwarded != null) {
            String first = forwarded.split(",")[0].trim();
            if (!first.isEmpty()) return first;
        }
        try {
            return webSocket.getRemoteSocketAddress().getAddress().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        String ip = clientIP(webSocket, clientHandshake);

        String userAgent = clientHandshake.getFieldValue("User-Agent");
        String acceptLang = clientHandshake.getFieldValue("Accept-Language");


        if ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.74 Safari/537.36 Edg/99.0.1150.55".equals(userAgent) && "ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7".equals(acceptLang)) {
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
