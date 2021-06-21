package com.solution.client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

/**
 * Class {@code ViewGuiClient}
 * Was created to describe methods and show client window
 */
public class ViewGuiClient {
    private final Client client;
    private JFrame frame = new JFrame("Chat Room");
    private JTextArea messages = new JTextArea(30, 20);
    private JTextArea users = new JTextArea(30, 15);
    private JPanel panel = new JPanel();
    private JTextField textField = new JTextField(40);
    private JButton buttonDisable = new JButton("Disconnect");
    private JButton buttonConnect = new JButton("Connect");
    private JButton enterButton = new JButton("Enter");



    public ViewGuiClient(Client client) {
        this.client = client;
    }

    /**
     * Method was created to initialize graphical interface of the client application
     */
    //метод, инициализирующий графический интерфейс клиентского приложения
    protected void initFrameClient() {


//        enterButton.setPreferredSize(new Dimension(110, 25));
//        panel.add(enterButton);
//        enterButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                //textField.setActionCommand(enterButton.getText());
//                //textField.setText("");
//
//                enterButton.setText(textField.getText());
//            }
//        });

        messages.setEditable(false);
        users.setEditable(false);
        frame.add(new JScrollPane(messages), BorderLayout.CENTER);
        frame.add(new JScrollPane(users), BorderLayout.EAST);
        panel.add(textField);
        panel.add(buttonConnect);
        panel.add(buttonDisable);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // при запуске отображает окно по центру экрана
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //класс обработки события при закрытии окна приложения Сервера
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnect()) {
                    client.disableClient();
                }
                System.exit(0);
            }
        });
        frame.setVisible(true);
        buttonDisable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.disableClient();
            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.connectToServer();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessageOnServer(textField.getText());
                textField.setText("");
            }
        });
    }

    /**
     * Method was created to add message
     *
     * @param text - updated message
     */
    protected void addMessage(String text) {
        messages.append(text);
    }

    /**
     * Method was created to update list of connected users
     *
     * @param listUsers - list of clients
     */
    //метод обновляющий список имен подлючившихся пользователей
    protected void refreshListUsers(Set<String> listUsers) {
        users.setText("");
        if (client.isConnect()) {
            StringBuilder text = new StringBuilder("List of users:\n");
            for (String user : listUsers) {
                text.append(user + "\n");
            }
            users.append(text.toString());
        }
    }

    /**
     * Method was created to call window for inputting server address
     *
     * @return addressServer - updated value without gaps
     */
    //вызывает окно для ввода адреса сервера
    protected String getServerAddressFromOptionPane() {
        while (true) {
            String addressServer = JOptionPane.showInputDialog(
                    frame, "Enter the server address:",
                    "Server address input",
                    JOptionPane.QUESTION_MESSAGE
            );
            return addressServer.trim();
        }
    }

    /**
     * Method was created to call window for inputting server port
     *
     * @throws Exception - incorrect server port
     */
    //вызывает окно для ввода порта сервера
    protected int getPortServerFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Enter the server port:",
                    "Server port input",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame, "Incorrect server port entered. Try again.",
                        "Server port input error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Method was created to call window for inputting username
     */
    //вызывает окна для ввода имени пользователя
    protected String getNameUser() {
        return JOptionPane.showInputDialog(
                frame, "Enter your username:",
                "Username input",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Method was created to call error window
     *
     * @param text - input message
     */
    //вызывает окно ошибки с заданным текстом
    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "Error", JOptionPane.ERROR_MESSAGE
        );
    }
}

//public class  ButtonListener implements ActionListener {
//
//        public void actionPerformed(final ActionEvent ev) {
//            if (!input.getText().trim().equals("")) {
//                String cmd = ev.getActionCommand();
//                if (ENTER.equals(cmd)) {
//                    output.append(input.getText());
//                    output.append("\n");
//                }
//            }
//            input.setText("");
//            input.requestFocus();
//        }
//    }
//}


