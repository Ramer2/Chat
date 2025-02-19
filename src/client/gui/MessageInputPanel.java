package client.gui;

import client.ClientThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Arrays;

// panel, which is used for creating and sending messages
public class MessageInputPanel extends JPanel {
    private static MessageInputPanel instance = null;
    private static JTextArea messageInputArea = null;

    private MessageInputPanel() {
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.VERTICAL;

        JLabel messageLabel = new JLabel("YOUR MESSAGE");
        add(messageLabel, gbc);
        gbc.gridy = 1;

        JLabel recipients = new JLabel("CHOOSE RECIPIENTS IN THE MEMBERS SECTION");
        add(recipients, gbc);
        gbc.gridy = 2;
        gbc.weighty = 0.88;

        messageInputArea = new JTextArea();
        messageInputArea.setColumns(70);
        messageInputArea.setLineWrap(true);
        messageInputArea.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JScrollPane messageAreaPane = new JScrollPane(messageInputArea);
        add(messageAreaPane, gbc);
        gbc.gridy = 3;
        gbc.weighty = 0.02;

        JPanel buttonPanel = getButtonPanel();
        add(buttonPanel, gbc);
    }

    // buttons below the text pane
    private static JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        JButton send = getSend();
        buttonPanel.add(send);

        // show the banned phrases
        JButton bannedPhrases = new JButton("Banned phrases");
        bannedPhrases.setFocusPainted(false);
        bannedPhrases.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                BannedPhrases.open();
            }
        });
        buttonPanel.add(bannedPhrases);

        // show the instructions
        JButton instructions = new JButton("Instructions");
        instructions.setFocusPainted(false);
        instructions.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ClientThread.request_instr();
                Instructions.open();
            }
        });
        buttonPanel.add(instructions);

        return buttonPanel;
    }

    // send the message
    private static JButton getSend() {
        JButton send = new JButton("Send");
        send.setFocusPainted(false);
        send.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String[] recipients = null;
                if (!NetworkMemberPanel.isPublic()) {
                    recipients = NetworkMemberPanel.getInstance().getSelectedNames();
                }
                String contents = messageInputArea.getText();
                if (contents.isEmpty()) return;
                messageInputArea.setText("");
                ClientThread.sendMessage(recipients, contents);
            }
        });
        return send;
    }

    public static MessageInputPanel getInstance() {
        if (instance == null)
            instance = new MessageInputPanel();

        return instance;
    }
}
