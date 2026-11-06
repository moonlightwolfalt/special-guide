package io.mopesbox.Objects.ETC;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.Rectangle;
import io.mopesbox.Server.MsgWriter;
import io.mopesbox.World.Room;

public class Button extends Rectangle {
    private String label;
    private int value;
    private String color;
    private int opacity;
    private String hoverStroke;
    private String btnStroke;
    private boolean enabled;
    private String hotkey;
    public GameClient owner;
    private double ux;
    private double uy;
    private Room room;
    private int optionID;

    public Button(int id, double x, double y, double ux, double uy, int width, int height, int optionID, Room room, GameClient owner, String label, int value, String color, int opacity, String hoverStroke, String btnStroke, boolean enabled, String hotkey) {
        super(id, x, y, width, height, 129);
        this.label = label;
        this.value = value;
        this.color = color;
        this.opacity = opacity;
        this.hoverStroke = hoverStroke;
        this.btnStroke = btnStroke;
        this.enabled = enabled;
        this.owner = owner;
        this.hotkey = hotkey;
        this.ux = ux;
        this.room = room;
        this.optionID = optionID;
        this.uy = uy;
    }

    public void setLabel(String newlabel) {
        this.label = newlabel;
    }

    @Override
    public void update() {
        super.update();
        if(this.owner != null && this.owner.getPlayer() != null) {
            this.setX(this.owner.getPlayer().getX()+ux);
            this.setY(this.owner.getPlayer().getY()+uy);
        } else destroy();
    }

    public void destroy() {
        this.room.removeObj(this, this.owner != null && this.owner.getPlayer() != null ? this.owner.getPlayer() : null);
    }

    @Override
    public void writeCustomData_onAdd(MsgWriter writer) {
        super.writeCustomData_onAdd(writer);
        writer.writeUInt32(this.optionID);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("label", label);
        jsonObject.addProperty("value", value);
        jsonObject.addProperty("opacity", opacity);
        jsonObject.addProperty("hoverStroke", hoverStroke);
        jsonObject.addProperty("btnStroke", btnStroke);
        jsonObject.addProperty("enabled", enabled ? 1 : 0);
        jsonObject.addProperty("hotkey", hotkey);
        jsonObject.addProperty("optionID", optionID);
        jsonObject.addProperty("color", color);
        Gson gson = new Gson();
        writer.writeString(gson.toJson(jsonObject));
    }
}
