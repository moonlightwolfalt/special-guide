package io.mopesbox.Server;

import io.mopesbox.Utils.PacketException;
import io.mopesbox.Utils.Utils;

public class MsgReader {
    private int length;
    private int offset;
    private byte[] data;
    public MsgReader(final byte[] data) {
        this.setLength(this.length);
        this.offset = 0;
        this.data = data;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public byte[] getData() {
        return this.data;
    }

    public short readInt16() {
        final short ret = (short)(((this.data[this.offset] & 0xFF) << 8) + (this.data[this.offset + 1] & 0xFF));
        this.offset += 2;
        return ret;
    }

    public short readUInt16() throws PacketException {
        if (this.data.length > this.offset + 1) {
            final int firstByte = 0xFF & this.data[this.offset];
            final int secondByte = 0xFF & this.data[this.offset + 1];
            this.offset += 2;
            char anUnsignedShort = (char)(firstByte << 8 | secondByte);
            if ((short)anUnsignedShort < 0) {
                anUnsignedShort = '\0';
            }
            return (short)anUnsignedShort;
        }
        throw new PacketException("UInt16 is failed to read");
    }

    public int readUInt32() throws PacketException {
        if (this.data.length > this.offset + 3) {
            final int firstByte = 0xFF & this.data[this.offset];
            final int secondByte = 0xFF & this.data[this.offset + 1];
            final int thirdByte = 0xFF & this.data[this.offset + 2];
            final int fourthByte = 0xFF & this.data[this.offset + 3];
            this.offset += 4;
            long anUnsignedInt = (long)(firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte) & 0xFFFFFFFFL;
            if ((int)anUnsignedInt < 0) {
                anUnsignedInt = 0L;
            }
            return (int)anUnsignedInt;
        }
        throw new PacketException("UInt32 is failed to read");
    }

    public int readUInt8() throws PacketException {
        int toReturn = 0;
        if (this.data.length > this.offset) {
            toReturn = (this.data[this.offset] & 0xFF);
            ++this.offset;
        }
        if (toReturn < 0) {
            toReturn = 0;
            throw new PacketException("UInt8 is failed to read");
        }
        return toReturn;
    }


    public String readString() throws PacketException {
        String c = "";
        for (int a = this.readUInt16(), g = 0; g < a; ++g) {
            final int e = (byte)this.readUInt8();
            c = String.valueOf(String.valueOf(c)) + (char)e;
        }
        return Utils.decode_utf8(c);
    }
}
