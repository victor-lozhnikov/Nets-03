package com.lozhnikov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private String name;
    private int port;
    private int lossPercentage;
    private List<Neighbour> neighbours;
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
        this.port = port;
        this.lossPercentage = lossPercentage;
        neighbours = Collections.synchronizedList(new LinkedList<>());
        socket = new DatagramSocket(port);
        messageHistory = Collections.synchronizedSet(new HashSet<>());
        messageQueue = Collections.synchronizedMap(new ConcurrentHashMap<>());
        messageUid = Collections.synchronizedMap(new HashMap<>());
    }

    void addNeighbour(Neighbour neighbour) {
        neighbours.add(neighbour);
        System.out.println("New neighbour: " + neighbour.getName());
    }

    void sendGreeting(InetAddress inetAddress, int port, boolean accept) throws IOException {
        DatagramPacket packet;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] codeMessage = {accept ? (byte) 1 : (byte) 0};
        byteOut.write(codeMessage);
        byte[] nameLengthBuffer = ByteBuffer.allocate(2).putShort(
                (short) name.length()).array();
        byteOut.write(nameLengthBuffer);
        byte[] nameBuffer = name.getBytes();
        byteOut.write(nameBuffer);
        byte[] rubbish = new byte[4096 - 3 - 2 * name.length()];
        byteOut.write(rubbish);
        byte[] message = byteOut.toByteArray();
        packet = new DatagramPacket(message, 4096, inetAddress, port);
        socket.send(packet);
    }

    public String getName() {
        return name;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public List<Neighbour> getNeighbours() {
        return neighbours;
    }

    public Map<UUID, Set<Neighbour>> getMessageQueue() {
        return messageQueue;
    }

    public void addMessageUid (UUID uid) {
        messageHistory.add(uid);
    }

    public boolean hasMessageUid (UUID uid) {
        return messageHistory.contains(uid);
    }

    public void addMessageToQueue(Message message, Neighbour sender) {
        messageQueue.put(message.getUid(), Collections.synchronizedSet(new HashSet<>()));
        messageUid.put(message.getUid(), message);
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
}
