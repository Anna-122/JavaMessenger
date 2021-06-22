package com.solution.client;

import com.solution.connection.Connection;
import com.solution.connection.Message;
import com.solution.connection.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Class {@code Client}
 * Was created to describe methods and to run client side
 */
public class Client {
    private Connection connection;
    private static ModelGuiClient model;
    private static ViewGuiClient gui;
    private volatile boolean isConnect = false; //флаг отобаржающий состояние подключения клиента  серверу

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    //точка входа в клиентское приложение
    public static void main(String[] args) {
        Client client = new Client();
        model = new ModelGuiClient();
        gui = new ViewGuiClient(client);
        gui.initFrameClient();
        while (true) {
            if (client.isConnect()) {
                client.nameUserRegistration();
                client.receiveMessageFromServer();
                client.setConnect(false);
            }
        }
    }

    /**
     * Method was created for connection to the server
     *
     * @throws Exception - if name of port or server wrong
     */
    //метод подключения клиента  серверу
    protected void connectToServer() {
        //если клиент не подключен  сервере то..
        if (!isConnect) {
            while (true) {
                try {
                    //вызываем окна ввода адреса, порта сервера
                    String addressServer = gui.getServerAddressFromOptionPane();
                    int port = gui.getPortServerFromOptionPane();
                    //создаем сокет и объект connection
                    Socket socket = new Socket(addressServer, port);
                    connection = new Connection(socket);
                    isConnect = true;
                    gui.addMessage("Service message: You have connected to the server.\n");
                    break;
                } catch (Exception e) {
                    gui.errorDialogWindow("An error has occurred! You may have entered the wrong server address or port. Try again!");
                    break;
                }
            }
        } else gui.errorDialogWindow("You are already connected!");
    }

    /**
     * Method was created to realize registration of username from the client application side
     *
     * @throws Exception - error occurred while registering the name
     * @throws IOException  - connection closing error
     */
    //метод, реализующий регистрацию имени пользователя со стороны клиентского приложения
    protected void nameUserRegistration() {
        while (true) {
            try {
                Message message = connection.receive();
                //приняли от сервера сообщение, если это запрос имени, то вызываем окна ввода имени, отправляем на сервер имя
                if (message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }
                //если сообщение - имя уже используется, выводим соответствующее окно с ошибой, повторяем ввод имени
                if (message.getTypeMessage() == MessageType.NAME_USED) {
                    gui.errorDialogWindow("This name is already in use, please enter another");
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }
                //если имя принято, получаем множество всех подключившихся пользователей, выходим из цикла
                if (message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
                    gui.addMessage("Service message: your name is accepted!\n");
                    model.setUsers(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                gui.errorDialogWindow("An error occurred while registering the name. Try to reconnect");
                try {
                    connection.close();
                    isConnect = false;
                    break;
                } catch (IOException ex) {
                    gui.errorDialogWindow("Connection closing error");
                }
            }

        }
    }

    /**
     * Method was created to send messages intended for other users to the server
     *
     * @param text - message from user
     * @throws Exception - error sending message
     */
    //метод отправки сообщения предназначенного для других пользователей на сервер
    protected void sendMessageOnServer(String text) {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, text));
        } catch (Exception e) {
            gui.errorDialogWindow("Error sending message");
        }
    }

    /**
     * Method was created to accept messages from server and other clients
     *
     * @throws Exception - error while receiving a message from the server
     */
    //метод принимающий с сервера собщение от других клиентов
    protected void receiveMessageFromServer() {
        while (isConnect) {
            try {
                Message message = connection.receive();
                //если тип TEXT_MESSAGE, то добавляем текст сообщения в окно переписки
                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    gui.addMessage(message.getTextMessage());
                }
                //если сообщение с типо USER_ADDED добавляем сообщение в окно переписки о новом пользователе
                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    model.addUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Service message: user %s has joined the chat.\n", message.getTextMessage()));
                }
                //аналогично для отключения других пользователей
                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    model.removeUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Service message: user %s has left the chat.\n", message.getTextMessage()));
                }
            } catch (Exception e) {
                gui.errorDialogWindow("Error while receiving a message from the server.");
                setConnect(false);
                gui.refreshListUsers(model.getUsers());
                break;
            }
        }
    }

    /**
     * Method was created to disconnect clients
     *
     * @throws Exception - error occurred while disconnecting
     */
    //метод реализующий отключение нашего клиента от чата
    protected void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getUsers().clear();
                isConnect = false;
                gui.refreshListUsers(model.getUsers());
            } else gui.errorDialogWindow("You are already disconnected.");
        } catch (Exception e) {
            gui.errorDialogWindow("Service message: An error occurred while disconnecting.");
        }
    }
}