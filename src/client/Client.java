package client;

import client.gui.BannedPhrases;
import requests.*;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static Client instance = null;
    private static String nickname = null;
    private static String ip = null;
    private static String port = null;

    // executor services
    private static final ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private static final ExecutorService requestExecutor = Executors.newCachedThreadPool();

    private Client() {
        initializeClient();
    }

    // registers a client
    private void initializeClient() {
        JFrame frame = new JFrame("Register");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(350, 500));

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownServices();
                System.exit(0);
            }
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(
                "<html><div style='text-align: center; font-family: Arial, sans-serif; font-size: 24px;'>Register</div></html>"
        );
        label.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(label, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 10, 20);
        JTextField nicknameField = new JTextField();
        nicknameField.setToolTipText("Enter your nickname");
        nicknameField.setPreferredSize(new Dimension(250, 30));
        frame.add(new JLabel("Nickname:"), gbc);
        gbc.gridy++;
        frame.add(nicknameField, gbc);

        gbc.gridy++;
        JTextField ipField = new JTextField();
        ipField.setToolTipText("Enter server IP address");
        ipField.setPreferredSize(new Dimension(250, 30));
        frame.add(new JLabel("Server IP:"), gbc);
        gbc.gridy++;
        frame.add(ipField, gbc);

        gbc.gridy++;
        JTextField portField = new JTextField();
        portField.setToolTipText("Enter server port");
        portField.setPreferredSize(new Dimension(250, 30));
        frame.add(new JLabel("Server Port:"), gbc);
        gbc.gridy++;
        frame.add(portField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 0, 20);
        JLabel nicknameRules = new JLabel(
                "<html>" +
                        "<div style='text-align: left; font-family: Arial, sans-serif;'>" +
                        "<h3>Nickname Rules:</h3>" +
                        "<ul>" +
                        "<li>Between 3 and 16 characters</li>" +
                        "<li>No spaces</li>" +
                        "<li>No banned words/phrases</li>" +
                        "</ul>" +
                        "</div>" +
                        "</html>"
        );
        frame.add(nicknameRules, gbc);

        // buttons for connecting and getting BannedPhrases
        gbc.gridy++;
        gbc.insets = new Insets(20, 50, 10, 50);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton connectButton = new JButton("Connect");
        connectButton.setFocusPainted(false);
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Client.ip = ipField.getText();
                Client.port = portField.getText();
                Client.nickname = nicknameField.getText();

                // validate nickname
                if (nickname.length() < 3 || nickname.length() > 16) {
                    JOptionPane.showMessageDialog(frame, "Nickname has to be between 3 and 16 characters");
                    return;
                } else if (nickname.contains(" ")) {
                    JOptionPane.showMessageDialog(frame, "Nickname cannot contain any spaces");
                    return;
                }

                // validate ip
                if (!isValidIP(ip)) {
                    JOptionPane.showMessageDialog(frame, "Your IP is not of correct format (IPv4)");
                    return;
                }

                //validate port
                try {
                    int portNum = Integer.parseInt(port);
                    if (!(portNum >= 1024 && portNum <= 65535)){
                        JOptionPane.showMessageDialog(frame, "Your port is not of correct format (1024 <= port <= 65535");
                        return;
                    }
                } catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(frame, "Your port is not of correct format (1024 <= port <= 65535");
                    return;
                }

                connect(ip, port, nickname, frame);
            }
        });
        buttonPanel.add(connectButton);

        JButton bannedPhrasesButton = getBannedPhrasesButton(ipField, portField);
        buttonPanel.add(bannedPhrasesButton);

        frame.add(buttonPanel, gbc);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // connection establishment
    private void connect(String ip, String port, String nickname, JFrame frame) {
        connectionExecutor.execute(() -> {
            Request_Conn requestConn = new Request_Conn(nickname);

            // establish connection and streams
            try {
                Socket socket = new Socket(ip, Integer.parseInt(port));
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                // send connection response
                oos.writeObject(requestConn);
                oos.flush();

                // waiting and getting a response
                Object input = ois.readObject();
                if (input instanceof Message message) {
                    switch (message.getType()) {
                        // the server allows to connect
                        case "SUCCESS": {
                            Thread.startVirtualThread(() -> new ClientThread(socket, ois, oos).run());

                            // update the banned phrases
                            requestBannedPhrases();
                            frame.setVisible(false);
                            break;
                        }
                        // server refuses the connection (the nickname is already taken or contains banned phrases)
                        case "ERROR": {
                            Error_Msg error = (Error_Msg) message;
                            JOptionPane.showMessageDialog(frame, error.getErrorText());
                            break;
                        }
                        // ignore unknown
                        default: {
                            System.out.println("Unknown message received. Ignored.");
                            break;
                        }
                    }
                }
            } catch (UnknownHostException e) {
                JOptionPane.showMessageDialog(frame, "Connection Failed: check your host (ip).");
            } catch (ConnectException e) {
                JOptionPane.showMessageDialog(frame, "Connection failed. Server may not be up.");
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static ArrayList<String> request_bp(String ip, String port) {
        Future<ArrayList<String>> future = requestExecutor.submit(() -> {
            try {
                Socket socket = new Socket(ip, Integer.parseInt(port));
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                oos.writeObject(new Request_BP());
                Object response = ois.readObject();
                if (response instanceof ArrayList<?>) {
                    oos.close();
                    ois.close();
                    socket.close();
                    return (ArrayList<String>) response;
                }
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Request failed");
            }
            return null;
        });

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private JButton getBannedPhrasesButton(JTextField ipField, JTextField portField) {
        JButton bannedPhrasesButton = new JButton("Banned Phrases");
        bannedPhrasesButton.setFocusPainted(false);
        bannedPhrasesButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String ip = ipField.getText();
                String port = portField.getText();
                // validate ip
                if (!isValidIP(ip)) {
                    JOptionPane.showMessageDialog(null, "Your IP is not of correct format (IPv4)");
                    return;
                }

                //validate port
                try {
                    int portNum = Integer.parseInt(port);
                    if (!(portNum >= 1024 && portNum <= 65535)){
                        JOptionPane.showMessageDialog(null, "Your port is not of correct format (1024 <= port <= 65535");
                        return;
                    }
                } catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(null, "Your port is not of correct format (1024 <= port <= 65535");
                    return;
                }

                Client.ip = ipField.getText();
                Client.port = portField.getText();
                requestBannedPhrases();
                BannedPhrases.open();
            }
        });
        return bannedPhrasesButton;
    }

    // request new banned phrases and update the BannedPhrases window
    private static void requestBannedPhrases() {
        ArrayList<String> bannedPhrases = request_bp(ip, port);
        if (bannedPhrases != null) BannedPhrases.updateBannedPhrases(bannedPhrases);
    }

    private boolean isValidIP(String ip) {
        if (ip.equals("localhost")) return true;
        String ipPattern = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        return ip.matches(ipPattern);
    }

    public static String getNickname() {
        return nickname;
    }

    public static void launchClient() {
        if (instance == null) instance = new Client();
    }

    public static void shutdownServices() {
        connectionExecutor.shutdown();
        requestExecutor.shutdown();

        try {
            if (!connectionExecutor.awaitTermination(5, TimeUnit.SECONDS))
                connectionExecutor.shutdownNow();

            if (!requestExecutor.awaitTermination(5, TimeUnit.SECONDS))
                requestExecutor.shutdownNow();
        } catch (InterruptedException e) {
            connectionExecutor.shutdownNow();
            requestExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
