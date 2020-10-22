package com.lozhnikov;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Sender implements Runnable {

    Node node;

    public Sender(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            DatagramPacket packet;
            for (UUID messageUid : node.getMessageQueue().keySet()) {
                Message message = node.getMessageFromUid(messageUid);
                for (Neighbour neighbour : node.getMessageQueue().get(messageUid)) {
                    try {
                        byte[] messageBuffer = message.toByteArray(false);
                        packet = new DatagramPacket(messageBuffer, messageBuffer.length,
                                neighbour.getInetAddress(), neighbour.getPort());
                        node.getSocket().send(packet);
                        System.out.println("Message '" + message.getMessage() + "' sent to " + neighbour.getName());
                        Thread.sleep(1000);
                    } catch (IOException | InterruptedException ex) {
                        System.out.println("Can't send message");
                    }

                }
            }
        }
    }
}
