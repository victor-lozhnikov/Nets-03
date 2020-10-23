package com.lozhnikov;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Neighbour {
    private final String name;
    private final int port;
    private final InetAddress inetAddress;
    private final Map<UUID, Long> lastMessageSent;
    private Long lastReceivedPing = 0L;
    private Long lastSentPing = 0L;

    public Neighbour(String name, InetAddress inetAddress, int port) {
        this.name = name;
        this.port = port;
        this.inetAddress = inetAddress;
        lastMessageSent = Collections.synchronizedMap(new ConcurrentHashMap<>());
    }

    public String getName() {
        return name;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbour neighbour = (Neighbour) o;
        return port == neighbour.port &&
                Objects.equals(name, neighbour.name) &&
                Objects.equals(inetAddress, neighbour.inetAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, port, inetAddress);
    }

    public void addSentMessageTime(UUID uid, Long time) {
        lastMessageSent.put(uid, time);
    }

    public long getLastSentMessageTime(UUID uid) {
        if (lastMessageSent.containsKey(uid)) {
            return lastMessageSent.get(uid);
        }
        return 0;
    }

    public Long getLastReceivedPing() {
        return lastReceivedPing;
    }

    public void setLastReceivedPing(Long lastReceivedPing) {
        this.lastReceivedPing = lastReceivedPing;
    }

    public Long getLastSentPing() {
        return lastSentPing;
    }

    public void setLastSentPing(Long lastSentPing) {
        this.lastSentPing = lastSentPing;
    }
}
