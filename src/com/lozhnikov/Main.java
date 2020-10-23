package com.lozhnikov;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length != 3 && args.length != 5) {
            System.out.println("Wrong count of arguments");
            return;
        }

        String nodeName = args[0];
        int port = Integer.parseInt(args[1]);
        int lossPercentage = Integer.parseInt(args[2]);
        if (lossPercentage > 100 || lossPercentage < 0) {
            System.out.println("Wrong loss percentage");
            return;
        }

        boolean alone = (args.length == 3);
        String neighbourAddress;
        int neighbourPort;

        Node node;
        try {
            if (!alone) {
                neighbourAddress = args[3];
                neighbourPort = Integer.parseInt(args[4]);
                node = new Node(nodeName, port, lossPercentage, neighbourAddress, neighbourPort);
            } else {
                node = new Node(nodeName, port, lossPercentage);
            }

            Thread console_thread = new Thread(new ConsoleReader(node));
            console_thread.start();
            Thread sender_thread = new Thread(new MessageSender(node));
            sender_thread.start();
            Thread receiver_thread = new Thread(new Transmitter(node));
            receiver_thread.start();
            Thread pinger_thread = new Thread(new Pinger(node));
            pinger_thread.start();
        }
        catch (IOException ex) {
            System.out.println("Can't create node");
        }
    }
}
