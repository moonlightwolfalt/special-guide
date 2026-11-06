package io.mopesbox.World;

import io.mopesbox.Constants;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.Juggernaut.Meteor;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.Utils.JuggernautState;
import io.mopesbox.Utils.MessageType;
import io.mopesbox.Utils.Utils;

public class JuggernautHandler {
    public Room room;

    public JuggernautState state = JuggernautState.WAITING;
    public JuggernautHandler(Room room) {
        this.room = room;
    }

    public void update(){
        // switch (state){
            // case WAITING:
            //     handleWaiting();
            //     break;
        // }
    }

    public void spawnMeteors(int count){
        boolean hasSpawnedWithGold = false;
        for(int i = 0; i < count; i++){
            
                boolean willSpawnGold = !hasSpawnedWithGold && Math.random() > 0.75;
                if (willSpawnGold)
                    hasSpawnedWithGold = true;


                this.room.addObj(new Meteor(this.room.getID(), Utils.randomDouble(0, Constants.WIDTH),
                Utils.randomDouble(0, Constants.HEIGHT), Utils.randomInt(40,100), Utils.randomDouble(0,
                Constants.WIDTH), Utils.randomDouble(0, Constants.HEIGHT), this.room,willSpawnGold));
        }

        this.sendMeteorAlert(count, hasSpawnedWithGold);
    }
    public void sendMeteorAlert(int count, boolean hasGold){
         for (GameClient client : this.room.clients) {
  
                MsgWriter writer = new MsgWriter();
                writer.writeType(MessageType.GAMEMODEHANDLE);
                writer.writeUInt8(1);

                writer.writeString(String.format("Warning %s meteors will strike the Earth!",count) + (hasGold ? " There is a golden one!" : ""));
                writer.writeUInt8(50);
                client.send(writer);
         }
        
    }

    // public Timer waitingTimer = new Timer(60000);
    // public void handleWaiting(){

    // }
}