package server;

import requests.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread{
    private final Socket clientSocket;
    private String nickname = null;
    private String ip = null;
    private String port = null;

    ObjectInputStream ois;
    ObjectOutputStream oos;

    public ServerThread(Socket clientSocket) {
        super();
        this.clientSocket = clientSocket;
        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            while (true) {
                // initialization and connection
                Object input = ois.readObject();
                if (input instanceof Message message) {
                    handleMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection issue in run()");
            Server.removeClient(this);
            closeConnection();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case "REQUEST_CONN": {
                handleConnect(message);
                break;
            }
            case "REQUEST_BP": {
                handleRequestBP();
                break;
            }
            case "REQUEST_INSTR": {
                try {
                    oos.writeObject(new Update_Instr(Server.getInstructions()));
                    oos.flush();
                } catch (IOException e) {
                    System.out.println("Failed to update Instructions");
                }
                break;
            }
            case "REQUEST_ML": {
                try {
                    CopyOnWriteArrayList<ServerThread> clients = Server.getClients();
                    int numberOfClients = clients.size();
                    String[] members = new String[numberOfClients];
                    for (int i = 0; i < numberOfClients; i++) {
                        members[i] = clients.get(i).getNickname();
                    }

                    oos.writeObject(new Update_ML(members));
                    oos.flush();
                } catch (IOException e) {
                    System.out.println("Failed to update member list");
                }
                break;
            }
            case "SEND_MSG": {
                handleSendMessage(message);
                break;
            }
            case "SEND_ALL": {
                handleSendAll(message);
                break;
            }
            default: {
                System.out.println("Error! Unknown message type!");
                break;
            }
        }
    }

    private void handleSendAll(Message message) {
        try {
            String contents = ((Send_All) message).getContents();
            String check = validateMessage(contents);
            if (check.isEmpty()) {
                CopyOnWriteArrayList<ServerThread> clients = Server.getClients();
                for (ServerThread client : clients) {
                    if (!client.getNickname().equals(nickname)) {
                        client.forwardMessage(contents, nickname);
                    }
                }
                oos.writeObject(new Success_Msg(contents));
            } else {
                oos.writeObject(new Error_Msg("Your message contains a banned phrase: " + check));
            }
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnect(Message message) {
        try {
            Request_Conn connectMsg = (Request_Conn) message;
            String nickname = connectMsg.getNickname();
            // validate the nickname
            int result = validateNickname(nickname);
            if (result == 0) {
                // nickname is good, confirm connection
                oos.writeObject(new Success_Msg());
                oos.flush();

                // handle the registration of the new user
                this.nickname = nickname;
                this.ip = String.valueOf(clientSocket.getLocalAddress());
                this.port = String.valueOf(clientSocket.getPort());
                Server.addClient(this);
            } else if (result == 1) {
                // nickname is invalid
                oos.writeObject(new Error_Msg("The nickname contains banned phrases"));
                oos.flush();
                closeConnection();
            } else {
                // nickname is taken
                oos.writeObject(new Error_Msg("The nickname is already taken"));
                oos.flush();
                closeConnection();
            }
        } catch (IOException e) {
            closeConnection();
        }
    }

    private void handleRequestBP() {
        try {
            oos.writeObject(Server.getBannedPhrases());
            oos.flush();
            closeConnection();
        } catch (IOException e) {
            closeConnection();
        }
    }

    private void handleSendMessage(Message message) {
        Send_Msg sendMsg = (Send_Msg) message;
        String[] recipients = sendMsg.getRecipients();
        String contents = sendMsg.getContents();

        try {
            String check = validateMessage(contents);
            if (check.isEmpty()) {
                oos.writeObject(new Success_Msg(contents));
            } else {
                oos.writeObject(new Error_Msg("Your message contains a banned phrase: " + check));
                return;
            }

            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CopyOnWriteArrayList<ServerThread> clients = Server.getClients();
        for (ServerThread client : clients) {
            for (String recipient : recipients) {
                if (client.getNickname().equals(recipient)) {
                    client.forwardMessage(contents, nickname);
                    break;
                }
            }
        }
    }

    public void forwardMessage(String contents, String sender) {
        try {
            oos.writeObject(new Receive_Msg(contents, sender));
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUpdateMessage(Update_ML update) {
        try {
            oos.writeObject(update);
            oos.flush();
        } catch (IOException e) {
            System.out.println("Failed to send an update");
        }
    }

    // 0 - nickname is good
    // 1 - nickname contains banned phrases
    // 2 - nickname is taken
    private int validateNickname(String nickname) {
        ArrayList<String> bannedPhrases = Server.getBannedPhrases();
        CopyOnWriteArrayList<ServerThread> clients = Server.getClients();

        // banned phrases check
        for (String phrase : bannedPhrases)
            if (nickname.toLowerCase().contains(phrase.toLowerCase())) return 1;

        // duplicate check
        for (ServerThread client : clients) {
            if (client.getNickname().equals(nickname)) return 2;
        }

        return 0;
    }

    private String validateMessage(String message) {
        ArrayList<String> bannedPhrases = Server.getBannedPhrases();

        for (String bannedPhrase : bannedPhrases) {
            if (bannedPhrase.trim().contains(" ")) {
                if (message.toLowerCase().contains(bannedPhrase.toLowerCase())) {
                    return bannedPhrase;
                }
            } else {
                String[] words = message.split("\\s+");
                for (String word : words) {
                    if (word.equalsIgnoreCase(bannedPhrase)) {
                        return bannedPhrase;
                    }
                }
            }
        }

        return "";
    }


    public String getNickname() {
        return nickname;
    }

    private void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            // ignored
        }
    }
}
