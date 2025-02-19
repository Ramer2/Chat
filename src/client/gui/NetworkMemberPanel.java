package client.gui;

import client.Client;
import client.ClientThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

// panel for displaying connected members
public class NetworkMemberPanel extends JPanel {
    private static NetworkMemberPanel instance = null;
    private static JPanel checkBoxPanel = null;
    private static String[] currentMembers = null;
    private static JRadioButton publicButton;

    private NetworkMemberPanel() {
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.05;
        gbc.fill = GridBagConstraints.BOTH;

        JLabel membersLabel = new JLabel("YOUR NICKNAME IS:  " + Client.getNickname() + "  NETWORK MEMBERS (CHOOSE RECIPIENTS HERE)");
        add(membersLabel, gbc);
        gbc.gridy = 1;
        gbc.weighty = 0.79;

        // panel to hold all the checkboxes
        checkBoxPanel = new JPanel();
        checkBoxPanel.setBackground(Color.WHITE);
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        add(scrollPane, gbc);
        gbc.gridy = 2;
        gbc.weighty = 0.02;

        // panel to hold radio buttons
        JPanel radioPanel = getRadioButtonPanel();
        add(radioPanel, gbc);

        gbc.gridy = 3;
        gbc.weighty = 0.06;
        // panel to hold buttons
        JPanel buttonPanel = getButtonPanel();
        add(buttonPanel, gbc);
    }

    // radio buttons below the members panel
    private static JPanel getRadioButtonPanel() {
        JPanel radioPanel = new JPanel();
        radioPanel.setBackground(Color.LIGHT_GRAY);
        radioPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        publicButton = new JRadioButton("Public");
        publicButton.setBackground(Color.LIGHT_GRAY);
        publicButton.setFocusPainted(false);
        publicButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                changeEnabledCheckboxes(false);
            }
        });

        JRadioButton selectiveButton = new JRadioButton("Selective");
        selectiveButton.setBackground(Color.LIGHT_GRAY);
        selectiveButton.setFocusPainted(false);
        selectiveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                changeEnabledCheckboxes(true);
            }
        });

        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(publicButton);
        radioGroup.add(selectiveButton);

        publicButton.setSelected(true);
        changeEnabledCheckboxes(false);

        radioPanel.add(publicButton);
        radioPanel.add(selectiveButton);

        return radioPanel;
    }

    // buttons below the radio buttons
    private static JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        // button to send a request to refresh the member list
        JButton refresh = new JButton("Refresh");
        refresh.setFocusPainted(false);
        refresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ClientThread.request_ml();
            }
        });

        buttonPanel.add(refresh);

        JButton selectEveryone = getSelectEveryone();
        buttonPanel.add(selectEveryone);

        JButton deselectEveryone = getDeselectEveryone();
        buttonPanel.add(deselectEveryone);
        return buttonPanel;
    }

    // select everyone button
    private static JButton getSelectEveryone() {
        JButton selectEveryone = new JButton("Select everyone");
        selectEveryone.setFocusPainted(false);
        selectEveryone.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (Component component : checkBoxPanel.getComponents()) {
                    if (component instanceof JCheckBox checkBox) {
                        checkBox.setSelected(true);
                    }
                }
            }
        });
        return selectEveryone;
    }

    // deselect everyone button
    private static JButton getDeselectEveryone() {
        JButton deselectEveryone = new JButton("De-select everyone");
        deselectEveryone.setFocusPainted(false);
        deselectEveryone.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (Component component : checkBoxPanel.getComponents()) {
                    if (component instanceof JCheckBox checkBox) {
                        checkBox.setSelected(false);
                    }
                }
            }
        });
        return deselectEveryone;
    }

    // update checkboxes
    public void updateMemberList(String[] members, String thisClientNickname) {
        String left = "";
        if (currentMembers != null) {
            for (String current : currentMembers) {
                if (!Arrays.asList(members).contains(current)) {
                    left = current;
                    break;
                }
            }

            if (!left.isEmpty()) ChatPanel.getInstance().addMessage("Server", left + " has left the chat.");
        }

        currentMembers = members;
        checkBoxPanel.removeAll();
        checkBoxPanel.revalidate();
        checkBoxPanel.repaint();
        for (String member : members) {
            if (!member.isEmpty() && !member.equals(thisClientNickname)) {
                JCheckBox newCheckBox = new JCheckBox(member);
                newCheckBox.setBackground(Color.WHITE);
                newCheckBox.setFocusPainted(false);
                newCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                newCheckBox.setSelected(true);
                checkBoxPanel.add(newCheckBox);
                checkBoxPanel.revalidate();
                checkBoxPanel.repaint();
            }
        }

        if (isPublic()) {
            changeEnabledCheckboxes(false);
        }
    }

    private static void changeEnabledCheckboxes(boolean enabled) {
        Component[] components = checkBoxPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JCheckBox checkBox) checkBox.setEnabled(enabled);
        }
    }

    // get selected names
    public String[] getSelectedNames() {
        int selectedCounter = 0;
        for (Component component : checkBoxPanel.getComponents()) {
            if (component instanceof JCheckBox checkBox) {
                if (checkBox.isSelected()) selectedCounter++;
            }
        }

        String[] selectedNames = new String[selectedCounter];
        int index = 0;
        for (Component component : checkBoxPanel.getComponents()) {
            if (component instanceof JCheckBox checkBox) {
                if (checkBox.isSelected()) {
                    selectedNames[index] = checkBox.getText();
                    index++;
                }
            }
        }

        return selectedNames;
    }

    public static boolean isPublic() {
        return publicButton.isSelected();
    }

    public static NetworkMemberPanel getInstance() {
        if (instance == null)
            instance = new NetworkMemberPanel();

        return instance;
    }
}
