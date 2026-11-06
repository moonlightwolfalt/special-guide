package io.mopesbox.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import io.mopesbox.Utils.MessageType;
import io.mopesbox.Utils.Utils;

public class MsgWriter {
    private ByteArrayOutputStream outputStream;
    private DeflaterOutputStream deflaterOutputStream;
    private int offset;

    public MsgWriter() {
        this.outputStream = new ByteArrayOutputStream();
        this.deflaterOutputStream = new DeflaterOutputStream(this.outputStream);
        this.setOffset(0);
    }

    public void writeUInt8(final int num) {
        this.writeByte((byte) num);
    }

    public void writeType(MessageType type) {
        this.writeUInt8(type.value());
    }

    public void writeBoolean(final boolean bool) {
        this.writeUInt8(bool ? 1 : 0);
    }

    public void writeUInt16(final short num) {
        this.writeBytes(ByteBuffer.allocate(2).putShort(num).array());
    }

    public void writeInt16(final short num) {
        this.writeBytes(ByteBuffer.allocate(2).putShort(num).array());
    }

    public void writeUInt16(final int snum) {
        short num = (short) snum;
        this.writeBytes(ByteBuffer.allocate(2).putShort(num).array());
    }

    public void writeUInt32(final int num) {
        this.writeBytes(ByteBuffer.allocate(4).putInt(num).array());
    }

    private void writeByte(final byte byteData) {
        try {
            this.deflaterOutputStream.write(byteData);
            this.offset++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBytes(final byte[] bytes) {
        try {
            this.deflaterOutputStream.write(bytes);
            this.offset += bytes.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFlags(final FlagWriter writer) {
        List<Byte> bytes = writer.getBytes();
        int size = bytes.size();

        for (int i = 0; i < size; i++) {
            this.writeUInt8((int) bytes.get(i));
        }
    }

    public void writeString(String string) {
        string = Utils.encode_utf8(string);
        this.writeUInt16((short) (string.length() + 1));
        for (int i = 0; i < string.length(); ++i) {
            this.writeByte((byte) string.charAt(i));
        }
        this.writeByte((byte) 109);
    }

    public byte[] getData() {
        try {
            this.deflaterOutputStream.finish();
            this.deflaterOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.outputStream.toByteArray();
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }
}
