package io.mopesbox.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.mopesbox.Constants;

public class AccountConnector {
    private String privatekey = "REMOVED_DUE_TO_SENSITIVE_DATA";
    private String privatemasterkey = Constants.MASTERKEY;

    public AccountConnector() {
    }

    public String updatePlayers(int count) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pvk", privatekey+privatemasterkey);
        jsonObject.addProperty("players", count);
        jsonObject.addProperty("gm", Constants.GAMEMODE);
        Gson gson = new Gson();
        String request = executePost(Constants.MASTERSERVERURL + "/", gson.toJson(jsonObject));

        return request;
    }

    public String getAccount(String id, String token) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("token", token);
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/reqplr", gson.toJson(jsonObject));
        return request;
    }
   public String getAccountById(String id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/AccountByTheIdsBcYess", gson.toJson(jsonObject));
        return request;
    }
      public String getAccountByIdDiscord(String id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("discordID", id);
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/AccountByTheIdsBcYessDiscord", gson.toJson(jsonObject));
        return request;
    }
    public String checkToken(String token) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/verifykey", gson.toJson(jsonObject));
        return request;
    }
    public void sendLog(String reqType, int executorId, int targetId, String reason){
               JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("requestType", reqType);
        jsonObject.addProperty("executor", String.valueOf(executorId));
        jsonObject.addProperty("targetId", String.valueOf(targetId));
        jsonObject.addProperty("reason", reason);

        Gson gson = new Gson();
        executePost(Constants.ACCOUNTSERVERURL+"/sendLog", gson.toJson(jsonObject));
    }

    public String addCoins(String id, String token, int coins) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pvk", privatekey);
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("coins", String.valueOf(coins));
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/a2pcoins", gson.toJson(jsonObject));
        return request;
    }

    public String addExp(String id, String token, int xp) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pvk", privatekey);
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("xp", String.valueOf(xp));
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/addbpexp", gson.toJson(jsonObject));
        return request;
    }

    public String banPlayer(String id, String reason, String executor, String bantime) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pvk", privatekey);
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("reason", reason);
        jsonObject.addProperty("executor", executor);
        jsonObject.addProperty("time", bantime);

        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/banuserpzl", gson.toJson(jsonObject));
        return request;
    }

    public String warnPlayer(String id, String reason, String executor) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pvk", privatekey);
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("reason", reason);
        jsonObject.addProperty("executor", executor);
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/warnuserpzl", gson.toJson(jsonObject));
        return request;
    }
    public String unbanPlayer(String id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pvk", privatekey);
        jsonObject.addProperty("id", id);
  
        Gson gson = new Gson();
        String request = executePost(Constants.ACCOUNTSERVERURL+"/unbanbanuserpzl", gson.toJson(jsonObject));
        return request;
    }
    

    public static String executePost(String targetURL, String urlParameters) {
        // if(Constants.MOREDEBUGINFO) Main.log.info("connto: "+targetURL+" | "+urlParameters);
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}