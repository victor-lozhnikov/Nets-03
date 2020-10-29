package com.lozhnikov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Pinger implements Runnable {

    Node node;
    long SENT_TIMEOUT = 1000;
    long CHECK_TIMEOUT = 5000;

    public Pinger(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            DatagramPacket packet;
            for (Neighbour neighbour : node.getNeighbours()) {
                if (System.currentTimeMillis() - neighbour.getLastSentPing() < SENT_TIMEOUT) {
                    continue;
                }
                try {
                    byte[] codeMessage = {4};
                    packet = new DatagramPacket(codeMessage, codeMessage.length,
                            neighbour.getInetAddress(), neighbour.getPort());
                    node.getSocket().send(packet);
                    neighbour.setLastSentPing(System.currentTimeMillis());
                    //System.out.println("Sent ping packet to " + neighbour.getName());
                }
                catch (IOException ex) {
                    System.out.println("Can't send ping packet");
                }
            }

            for (Neighbour neighbour : node.getNeighbours()) {
                if (System.currentTimeMillis() - neighbour.getLastReceivedPing() < CHECK_TIMEOUT) {
                    continue;
                }
                node.removeNeighbour(neighbour);
                System.out.println("Neighbour " + neighbour.getName() + " removed");

                if (node.getAlternate().equals(neighbour)) {
                    if (node.getNeighbours().isEmpty()) {
                        node.setAlternate(null);
                    }
                    else {
                        Neighbour alternate = node.getNeighbours().iterator().next();
                        node.setAlternate(alternate);
                        node.notifyNeighboursAboutNewAlternate();
                    }
                }

                if (neighbour.getAlternate().getName().equals(node.getName()) &&
                        neighbour.getAlternate().getInetAddress().equals(InetAddress.getLoopbackAddress()) &&
                        neighbour.getAlternate().getPort() == node.getPort()) continue;

                try {
                    node.sendGreeting(neighbour.getAlternate().getInetAddress(),
                            neighbour.getAlternate().getPort(), false);
                }
                catch (IOException ex) {
                    System.out.println("Can't connect to alternate of " + neighbour.getName() + "(" +
                            neighbour.getAlternate().getName() + ")");
                }
            }
        }
    }
}
