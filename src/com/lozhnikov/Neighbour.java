package com.lozhnikov;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Neighbour {
    private final String name;
    private final int port;
    private final InetAddress inetAddress;

    public Neighbour(String name, String address, int port) throws UnknownHostException {
        this.name = name;
        this.port = port;
        inetAddress = InetAddress.getByName(address);
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
}
