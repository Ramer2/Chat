package client.gui;

import javax.swing.*;
import java.awt.*;

// class for the chat (message history)
public class ChatPanel extends JPanel {
    private static ChatPanel instance = null;
    private static String contents = "";
    private static JTextArea chat;

    private ChatPanel() {
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.BOTH;

        JLabel chatLabel = new JLabel("CHAT");
        add(chatLabel, gbc);
        gbc.gridy = 1;
        gbc.weighty = 0.9;

        chat = new JTextArea(contents);
        chat.setEditable(false);
        chat.setLineWrap(true);
        chat.setWrapStyleWord(true);
        chat.setTabSize(3);
        chat.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        chat.setCaretColor(Color.WHITE);

        JScrollPane chatPane = new JScrollPane(chat);
        chatPane.setPreferredSize(new Dimension(400, 200));
        add(chatPane, gbc);
    }

    // print a new message
    public void addMessage(String sender, String messageContents) {
        contents += sender + ": " + messageContents + "\n";
        chat.setText(contents);
        revalidate();
        repaint();
    }

    public static ChatPanel getInstance() {
        if (instance == null)
            instance = new ChatPanel();

        return instance;
    }
}
