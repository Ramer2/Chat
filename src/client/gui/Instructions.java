package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// window for the instructions
public class Instructions {
    private static Instructions instance = null;
    private final JFrame frame;
    private static String instructionsText = "";
    final JLabel instructions;

    private Instructions() {
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(200, 300));
        frame.setTitle("Instructions");

        instructions = new JLabel(instructionsText);

        frame.add(instructions);

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

    // updates the instructions
    public static void updateInstructionsText(String instructionsText) {
        Instructions.instructionsText = instructionsText;
    }

    // opens the instruction window
    public static void open() {
        if (instance == null) {
            instance = new Instructions();
        } else {
            instance.frame.toFront();
            instance.frame.requestFocus();
        }
    }
}
