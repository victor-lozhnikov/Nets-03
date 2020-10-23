package com.lozhnikov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private String name;
    private int lossPercentage;
    private Set<Neighbour> neighbours;
    private DatagramSocket socket;
    private Set<UUID> messageHistory;
    private Map<UUID, Message> messageUid;
    private Map<UUID, Set<Neighbour>> messageQueue;

    public Node (String name, int port, int lossPercentage) throws SocketException {
        init(name, port, lossPercentage);
    }

    public Node (String name, int port, int lossPercentage, String neighbourAddress, int neighbourPort)
            throws IOException {
        init(name, port, lossPercentage);
        sendGreeting(InetAddress.getByName(neighbourAddress), neighbourPort, false);
    }

    void init(String name, int port, int lossPercentage) throws SocketException {
        this.name = name;
        this.lossPercentage = lossPercentage;
        neighbours = Collections.newSetFromMap(new ConcurrentHashMap<>());
        socket = new DatagramSocket(port);
        messageHistory = Collections.newSetFromMap(new ConcurrentHashMap<>());
        messageQueue = new ConcurrentHashMap<>();
        messageUid = new ConcurrentHashMap<>();
    }

    void addNeighbour(Neighbour neighbour) {
        neighbour.setLastReceivedPing(System.currentTimeMillis());
        neighbours.add(neighbour);
        System.out.println("New neighbour: " + neighbour.getName());
    }

    void sendGreeting(InetAddress inetAddress, int port, boolean accept) throws IOException {
        DatagramPacket packet;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] codeMessage = {accept ? (byte) 1 : (byte) 0};
        byteOut.write(codeMessage);
        byte[] nameLengthBuffer = ByteBuffer.allocate(2).putShort(
                (short) name.getBytes().length).array();
        byteOut.write(nameLengthBuffer);
        byte[] nameBuffer = name.getBytes();
        byteOut.write(nameBuffer);
        byte[] message = byteOut.toByteArray();
        byteOut.close();
        packet = new DatagramPacket(message, message.length, inetAddress, port);
        socket.send(packet);
    }

    public String getName() {
        return name;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public int getLossPercentage() {
        return lossPercentage;
    }

    public Map<UUID, Set<Neighbour>> getMessageQueue() {
        return messageQueue;
    }

    public Set<Neighbour> getNeighbours() {
        return neighbours;
    }

    public void addMessageUid (UUID uid) {
        messageHistory.add(uid);
    }

    public boolean hasMessageUid (UUID uid) {
        return messageHistory.contains(uid);
    }

    public void addMessageToQueue(Message message, Neighbour sender) {
        messageUid.put(message.getUid(), message);
        messageQueue.put(message.getUid(), Collections.newSetFromMap(new ConcurrentHashMap<>()));
        for (Neighbour neighbour : neighbours) {
            if (neighbour.equals(sender)) continue;
            messageQueue.get(message.getUid()).add(neighbour);
        }
    }

    public Message getMessageFromUid(UUID uid) {
        return messageUid.get(uid);
    }

    public void removeMessageFromQueue(Message message, Neighbour neighbour) {
        messageQueue.get(message.getUid()).remove(neighbour);
    }

    public Neighbour findNeighbourByAddress(InetAddress address, int port) {
        for (Neighbour neighbour : neighbours) {
            if (neighbour.getInetAddress().equals(address) && neighbour.getPort() == port) {
                return neighbour;
            }
        }
        return null;
    }

    public void removeNeighbour(Neighbour neighbour) {
        neighbours.remove(neighbour);
    }
}
