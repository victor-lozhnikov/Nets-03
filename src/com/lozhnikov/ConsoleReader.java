package com.lozhnikov;

import java.util.Scanner;
import java.util.UUID;

public class ConsoleReader implements Runnable {
    Node node;
    Scanner in;

    public ConsoleReader(Node node) {
        this.node = node;
        in = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String message = in.nextLine();
            String sender = node.getName();
            UUID uid = UUID.randomUUID();

            node.addMessageUid(uid);
            node.addMessageToQueue(new Message(uid, sender, message), null);
        }
    }
}
