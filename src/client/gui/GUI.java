package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// launches the main window (right after the connection)
public class GUI {
    public GUI() {
        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(1280, 720));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.setTitle("Chat");

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Client.shutdownServices();
                System.exit(0);
            }
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.25;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        JPanel upperPanel = new JPanel(new GridBagLayout());
        upperPanel.add(NetworkMemberPanel.getInstance(), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        upperPanel.add(ChatPanel.getInstance(), gbc);
        gbc.gridx = 0;

        gbc.weightx = 1;
        gbc.weighty = 0.6;
        frame.add(upperPanel, gbc);
        gbc.gridy = 1;

        gbc.weighty = 0.4;
        frame.add(MessageInputPanel.getInstance(), gbc);


        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
