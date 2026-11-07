package io.mopesbox.Objects.Biome;

import io.mopesbox.Constants;
import io.mopesbox.Client.GameClient;
import io.mopesbox.Networking.Networking;
import io.mopesbox.Objects.Eatable.SpaceFood;
import io.mopesbox.Objects.Eatable.SpaceFoodKind;
import io.mopesbox.Objects.ETC.SpaceGate;
import io.mopesbox.Utils.BiomeType;
import io.mopesbox.Utils.Utils;
import io.mopesbox.World.Room;

import java.util.ArrayList;
import java.util.List;

// Outer space: the strip along the right edge of the map (Constants.SPACEW wide), behind
// a gate that opens every SPACE_OPEN_EVERY seconds for SPACE_OPEN_FOR seconds. It's full
// of space foods worth more XP than any pumpkin. For gameplay it counts as land.
public class Space extends Biome {
    public static final int OBJECT_TYPE = 160;

    private final Room room;
    public final SpaceGate gate;
    private final int[] alive = new int[SpaceFoodKind.KINDS.length];
    // ticks until each eaten food grows back, per kind
    private final List<int[]> regrow = new ArrayList<>();
    private int tick = 0;
    private boolean wasOpen = false;

    public Space(int id, double x, double y, double width, double height, Room room) {
        super(id, OBJECT_TYPE, x, y, width, height, BiomeType.LAND.ordinal(), room);
        this.room = room;
        this.gate = new SpaceGate(room.getID(), Constants.CLASSICWIDTH, y, 60, height, room);
    }

    // Called once the biome is in the room: adds the gate and fills space with food.
    public void populate() {
        this.room.addObj(this.gate);
        for (int kind = 0; kind < SpaceFoodKind.KINDS.length; kind++)
            for (int i = 0; i < SpaceFoodKind.KINDS[kind].count; i++)
                spawnFood(kind);
    }

    private void spawnFood(int kind) {
        SpaceFoodKind k = SpaceFoodKind.KINDS[kind];
        double pad = k.radius + 40;
        double minX = this.getX() - this.getWidth() / 2 + pad + 60; // keep clear of the gate
        double maxX = this.getX() + this.getWidth() / 2 - pad;
        double minY = this.getY() - this.getHeight() / 2 + pad;
        double maxY = this.getY() + this.getHeight() / 2 - pad;
        if (maxX <= minX || maxY <= minY) return;
        this.room.addObj(new SpaceFood(this.room.getID(), Utils.randomDouble(minX, maxX), Utils.randomDouble(minY, maxY),
                kind, this, this.room));
        alive[kind]++;
    }

    public void foodEaten(int kind) {
        alive[kind]--;
        // bigger things take longer to come back: 15 s for an element, up to 2.5 min for the universe
        int seconds = (int) Math.min(150, 15 + SpaceFoodKind.KINDS[kind].radius / 2.2);
        regrow.add(new int[] { kind, seconds * Constants.TICKS_PER_SECOND });
    }

    private void broadcast(String message) {
        for (GameClient c : new ArrayList<>(this.room.clients)) {
            if (c != null && !c.isBot()) c.send(Networking.personalGameEvent(255, message));
        }
    }

    @Override
    public void update() {
        super.update();
        tick++;
        int cycle = Constants.SPACE_OPEN_EVERY * Constants.TICKS_PER_SECOND;
        int openFor = Math.min(Constants.SPACE_OPEN_FOR, Constants.SPACE_OPEN_EVERY) * Constants.TICKS_PER_SECOND;
        int inCycle = tick % cycle;
        // closed for the start of each cycle, then open for the last SPACE_OPEN_FOR seconds
        boolean open = inCycle >= cycle - openFor;
        int ticksLeft = open ? cycle - inCycle : (cycle - openFor) - inCycle;
        this.gate.setState(open, (ticksLeft + Constants.TICKS_PER_SECOND - 1) / Constants.TICKS_PER_SECOND);
        if (open != wasOpen) {
            broadcast(open
                    ? "Outer space is open for " + Constants.SPACE_OPEN_FOR + " seconds! Cross the east ocean to explore it."
                    : "Outer space has closed. Explorers inside can stay until they head back.");
            wasOpen = open;
        }
        for (int i = regrow.size() - 1; i >= 0; i--) {
            int[] r = regrow.get(i);
            if (--r[1] <= 0) {
                regrow.remove(i);
                if (alive[r[0]] < SpaceFoodKind.KINDS[r[0]].count) spawnFood(r[0]);
            }
        }
    }
}
