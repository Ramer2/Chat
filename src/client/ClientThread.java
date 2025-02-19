package client;

import client.gui.*;
import requests.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread {
    private static Socket server;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;

    public ClientThread(Socket server, ObjectInputStream ois, ObjectOutputStream oos) {
        ClientThread.server = server;
        ClientThread.ois = ois;
        ClientThread.oos = oos;
    }

    public void run() {
        try {
            SwingUtilities.invokeLater(GUI::new);
            request_instr();
            while (true) {
                Object input = ois.readObject();
                if (input instanceof Message message) handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception in run method");
            closeConnection();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Lost connection to the server "));
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case "UPDATE_ML": {
                handleMemberUpdate(message);
                break;
            }
            case "RECEIVE_MSG": {
                handleReceivedMsg(message);
                break;
            }
            case "ERROR": {
                Error_Msg errorMsg = (Error_Msg) message;
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, errorMsg.getErrorText()));
                break;
            }
            case "SUCCESS": {
                Success_Msg successMsg = (Success_Msg) message;
                if (successMsg.getContents() != null)
                    SwingUtilities.invokeLater(() -> ChatPanel.getInstance().addMessage("Me", successMsg.getContents()));
                break;
            }
            case "UPDATE_INSTR": {
                Update_Instr updateInstr = (Update_Instr) message;
                SwingUtilities.invokeLater(() -> Instructions.updateInstructionsText(updateInstr.getInstructions()));
                break;
            }
            default: {
                // ignore the message
                System.out.println(message + "ignored");
            }
        }
    }

    private void handleReceivedMsg(Message message) {
        Receive_Msg msg = (Receive_Msg) message;
        String contents = msg.getContents();
        String sender = msg.getSender();

        SwingUtilities.invokeLater(() -> ChatPanel.getInstance().addMessage(sender, contents));
    }

    private void handleMemberUpdate(Message message) {
        Update_ML update = (Update_ML) message;
        String[] newMemberList = update.getMemberList();
        String thisClientNickname = Client.getNickname();
        SwingUtilities.invokeLater(() -> NetworkMemberPanel.getInstance().updateMemberList(newMemberList, thisClientNickname));
    }

    public static void request_instr() {
        try {
            oos.writeObject(new Request_Instr());
            oos.flush();
        } catch (IOException e) {
            System.out.println("Failed to send instructions request");
        }
    }

    public static void request_ml() {
        try {
            oos.writeObject(new Request_ML());
            oos.flush();
        } catch (IOException e) {
            System.out.println("Failed to send member list refresh request");
        }
    }

    public static void sendMessage(String[] recipients, String message) {
        try {
            if (recipients == null) {
                oos.writeObject(new Send_All(message));
            } else {
                oos.writeObject(new Send_Msg(recipients, message));
            }
            oos.flush();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Failed to send a message"));
            System.out.println("Failed to send a message");
        }
    }

    private void closeConnection() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (server != null) server.close();
        } catch (IOException e) {
            // ignored
        }
    }
}