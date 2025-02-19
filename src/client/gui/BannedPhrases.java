package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

// window for the banned phrases
public class BannedPhrases {
    private static String bannedPhrases = "";
    private static BannedPhrases instance;
    private final JFrame frame;

    private BannedPhrases() {
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(200, 300));
        frame.setTitle("Instructions");

        JTextArea bannedPhrasesArea = new JTextArea();
        JScrollPane bannedPhrasesPane = new JScrollPane(bannedPhrasesArea);
        bannedPhrasesPane.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
        bannedPhrasesArea.setEditable(false);
        bannedPhrasesArea.setFocusable(false);
        bannedPhrasesArea.setLineWrap(true);
        bannedPhrasesArea.setText(bannedPhrases);

        frame.add(bannedPhrasesPane);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // allows for the new windows to be opened
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instance = null;
            }
        });
    }

    // updates the String, which contains the banned words list
    public static void updateBannedPhrases(ArrayList<String> newBannedPhrases) {
        bannedPhrases = "";
        for (String word : newBannedPhrases) {
            bannedPhrases += (word + "\n");
        }
    }

    public static void open() {
        if (instance == null) {
            instance = new BannedPhrases();
        } else {
            instance.frame.toFront();
            instance.frame.requestFocus();
        }
    }
}
