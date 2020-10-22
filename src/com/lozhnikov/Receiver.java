package com.lozhnikov;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class Receiver implements Runnable {

    Node node;

    public Receiver(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] messageBuffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length);
                node.getSocket().receive(packet);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(messageBuffer);
                byte[] codeMessage = new byte[1];
                byteIn.read(codeMessage);
                Neighbour neighbour;
                switch (codeMessage[0]) {
                    case 0:
                    case 1:
                        byte[] nameLengthBuffer = new byte[2];
                        byteIn.read(nameLengthBuffer);
                        byte[] nameBuffer = new byte[2 * ByteBuffer.wrap(nameLengthBuffer).getShort()];
                        byteIn.read(nameBuffer);
                        neighbour = new Neighbour(new String(nameBuffer),
                                packet.getAddress().getCanonicalHostName(), packet.getPort());
                        node.addNeighbour(neighbour);
                        if (codeMessage[0] == 0) {
                            node.sendGreeting(neighbour.getInetAddress(), neighbour.getPort(), true);
                        }
                        break;
                    case 2:
                    case 3:
                        byte[] uidBuffer1 = new byte[8];
                        byteIn.read(uidBuffer1);
                        byte[] uidBuffer2 = new byte[8];
                        byteIn.read(uidBuffer2);
                        long uidLong1 = ByteBuffer.wrap(uidBuffer1).getLong();
                        long uidLong2 = ByteBuffer.wrap(uidBuffer2).getLong();
                        UUID uid = new UUID(uidLong1, uidLong2);

                        byte[] senderLengthBuffer = new byte[2];
                        byteIn.read(senderLengthBuffer);
                        byte[] senderBuffer = new byte[2 * ByteBuffer.wrap(senderLengthBuffer).getShort()];
                        byteIn.read(senderBuffer);
                        String sender = new String(senderBuffer);

                        byte[] messageLengthBuffer = new byte[2];
                        byteIn.read(messageLengthBuffer);
                        byte[] messageTextBuffer = new byte[2 * ByteBuffer.wrap(messageLengthBuffer).getShort()];
                        byteIn.read(messageTextBuffer);
                        String messageText = new String(messageTextBuffer);

                        Message message = new Message(uid, sender, messageText);

                        neighbour = node.findNeighbourByAddress(packet.getAddress(), packet.getPort());

                        if (codeMessage[0] == 2 && !node.hasMessageUid(message.getUid())) {
                            node.addMessageToQueue(message, neighbour);
                            node.addMessageUid(message.getUid());
                            byte[] acceptBuffer = message.toByteArray(true);
                            packet = new DatagramPacket(acceptBuffer, acceptBuffer.length,
                                    neighbour.getInetAddress(), neighbour.getPort());
                            node.getSocket().send(packet);
                            System.out.println(
                                    "I received message '" + message.getMessage() + "' from " +
                                            neighbour.getName()
                            );
                        }
                        if (codeMessage[0] == 3) {
                            node.removeMessageFromQueue(message, neighbour);
                            System.out.println(
                                    "Message '" + message.getMessage() + "' delivered to " +
                                            neighbour.getName()
                            );
                        }
                }

            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}
