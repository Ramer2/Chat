package server;

import requests.Update_ML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    final String configFilePath = "./src/server/config.txt";
    private String port = null;
    private static String serverName = null;
    private ServerSocket serverSocket = null;
    public static ArrayList<String> bannedPhrases = new ArrayList<>();
    private static final CopyOnWriteArrayList<ServerThread> authorizedClients = new CopyOnWriteArrayList<>();

    private static final String instructions = "<html><head><style>body {display: flex;justify-content: center;align-items: center;" +
            "            height: 100vh;margin: 0;font-family: Arial, sans-serif;}.instructions {text-align: center;}</style></head>" +
            "<body>" +
            "    <div class=\"instructions\">" +
            "        <h2>To write a message:</h2>" +
            "        <ol>" +
            "            <li>Write a message in YOUR MESSAGE section</li>" +
            "            <li>Choose recipients in the NETWORK MEMBERS section</li>" +
            "            <li>Press \"Send\"</li>" +
            "        </ol>" +
            "        <h2>All received messages will appear in the CHAT section</h2>" +
            "    </div>" +
            "</body>" +
            "</html>";

    // reading the config
    private void initialize() {
        try (BufferedReader br = new BufferedReader(new FileReader(configFilePath))) {
            String line;

            // the first two lines are always port and server name
            port = br.readLine();
            serverName = br.readLine();

            // everything after are banned phrases
            while((line = br.readLine()) != null) {
                bannedPhrases.add(line);
            }
        } catch (IOException e) {
            System.out.println("Failed to initialize the server");
            throw new RuntimeException(e);
        }
    }

    public void listenSocket() {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
            System.out.println("Server's address: " + serverName + ":" + port);
        } catch (IOException e) {
            System.out.println("Listening failed: " + e);
        }

        // handling connections
        while (true) {
            try {
                Socket client = serverSocket.accept();
                Thread.startVirtualThread(() -> new ServerThread(client).run());
            } catch (IOException e) {
                System.out.println("Failed to connect a client: " + e);
            }
        }
    }

    // sends an update message to all the clients
    public static void update() {
        // creating an array of nicknames
        int numberOfClients = authorizedClients.size();
        String[] members = new String[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            members[i] = authorizedClients.get(i).getNickname();
        }

        // sending updates
        for (ServerThread client : authorizedClients) {
            client.sendUpdateMessage(new Update_ML(members));
        }
    }

    public static void addClient(ServerThread thread) {
        authorizedClients.add(thread);
        update();
    }

    public static void removeClient(ServerThread thread) {
        authorizedClients.remove(thread);
        update();
    }

    public static CopyOnWriteArrayList<ServerThread> getClients() {
        return authorizedClients;
    }

    public static ArrayList<String> getBannedPhrases() {
        return bannedPhrases;
    }

    public static String getInstructions() {
        return instructions;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.initialize();
        server.listenSocket();
    }
}
