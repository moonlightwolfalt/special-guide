package io.mopesbox.Server;
import io.mopesbox.Utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class FlagWriter {
    public List<Byte> bytes = new ArrayList<Byte>();
    public int bitIndex = 1;
    public byte currentByte = 0;

    // public void setLengthFlags(){
    // for(int i = 0;i < bytes.length;i++){
    // if(i != bytes.length-1)
    // bytes[i] = Utils.set_bit(bytes[i],0,true);
    // }
    // }

    public void writeBool(boolean bit) {
        currentByte = Utils.set_bit(currentByte, this.bitIndex, bit);
        this.bitIndex++;
        if (bitIndex >= 8) {

            bytes.add(currentByte);
            currentByte = 0;
            this.bitIndex = 1;

        }

    }

    public void writeBoolean(boolean bit) {
        this.writeBool(bit);
    }

    public void writeByteWithBitCount(int integer, int bits) {
        for (int i = 0; i < bits; i++) {
            this.writeBool(Utils.get_bit((byte) integer, i));

        }
    }

    public List<Byte> getBytes() {
        int size = this.bytes.size();
        if (size <= 0)
            this.bytes.add(currentByte);
        for (int i = 0; i < size; i++) {
            if (i != size - 1)
                bytes.set(i, Utils.set_bit(bytes.get(i), 0, true));

            // Main.log.info("Byte at" + i + ":" + bytes.get(i));
        }

        return bytes;
    }

}