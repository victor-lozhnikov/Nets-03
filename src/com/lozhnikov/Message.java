package com.lozhnikov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class Message {
    private final UUID uid;
    private final String sender;
    private final String message;

    public Message(UUID uid, String sender, String message) {
        this.uid = uid;
        this.sender = sender;
        this.message = message;
    }

    public UUID getUid() {
        return uid;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public byte[] toByteArray(boolean accept) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] codeMessage = {accept ? (byte) 3 : (byte) 2};
        byteOut.write(codeMessage);
        byte[] uidBuffer1 = ByteBuffer.allocate(8).putLong(uid.getMostSignificantBits()).array();
        byteOut.write(uidBuffer1);
        byte[] uidBuffer2 = ByteBuffer.allocate(8).putLong(uid.getLeastSignificantBits()).array();
        byteOut.write(uidBuffer2);
        byte[] senderLengthBuffer = ByteBuffer.allocate(2).putShort(
                (short) sender.length()).array();
        byteOut.write(senderLengthBuffer);
        byte[] senderBuffer = sender.getBytes();
        byteOut.write(senderBuffer);
        byte[] messageLengthBuffer = ByteBuffer.allocate(2).putShort(
                (short) message.length()).array();
        byteOut.write(messageLengthBuffer);
        byte[] messageBuffer = message.getBytes();
        byteOut.write(messageBuffer);
        byte[] rubbish = new byte[4096 - 1 - 16 -
                2 - 2 * sender.length() - 2 - 2 * message.length()];
        byteOut.write(rubbish);
        return byteOut.toByteArray();
    }
}
