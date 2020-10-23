package com.lozhnikov;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MessageSender implements Runnable {

    Node node;
    long TIMEOUT = 3000;

    public MessageSender(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            DatagramPacket packet;
            for (UUID messageUid : node.getMessageQueue().keySet()) {
                Message message = node.getMessageFromUid(messageUid);
                for (Neighbour neighbour : node.getMessageQueue().get(messageUid)) {
                    if (System.currentTimeMillis() - neighbour.getLastSentMessageTime(messageUid) < TIMEOUT) {
                        continue;
                    }
                    try {
                        byte[] messageBuffer = message.toByteArray(false);
                        packet = new DatagramPacket(messageBuffer, messageBuffer.length,
                                neighbour.getInetAddress(), neighbour.getPort());
                        node.getSocket().send(packet);
                        neighbour.addSentMessageTime(messageUid, System.currentTimeMillis());
                        System.out.println("Message '" + message.getMessage() + "' sent to " + neighbour.getName());
                        //Thread.sleep(1000);
                    } catch (IOException ex) {
                        System.out.println("Can't send message");
                    }

                }
            }
        }
    }
}
