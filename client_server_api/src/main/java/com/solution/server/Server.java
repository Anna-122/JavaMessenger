package com.solution.server;

import com.solution.connection.Connection;
import com.solution.connection.Message;
import com.solution.connection.MessageType;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class {@code Server}
 * Was created to describe methods
 */
public class Server {
    private ServerSocket serverSocket;
    private static ViewGuiServer gui; //объект класса представления
    private static ModelGuiServer model; //объект класса модели
    private static volatile boolean isServerStart = false; //флаг отражающий состояние сервера запущен/остановлен

    /**
     * Method was created to start server
     *
     * @ param port - value of port
     * @throws Exception - if server was failed
     */
    //метод, запускающий сервер
    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            gui.refreshDialogWindowServer("Server started.\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Failed to start server.\n");
        }
    }

    /**
     * Method was created to stop server
     *
     * @throws Exception - if server could not be stopped
     */
    //метод останавливающий сервер
    protected void stopServer() {
        try {
            //если серверныйСокет не имеет ссылки или не запущен
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersMultiChat().clear();
                gui.refreshDialogWindowServer("Server stopped.\n");
            } else gui.refreshDialogWindowServer("The server is not running - there is nothing to stop!\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Server could not be stopped.\n");
        }
    }

    /**
     * Method was created to accept new socket connection from client
     *
     * @throws Exception - if server connection lost
     */
    //метод, в котором в бесконечном цикле сервер принимает новое сокетное подключение от клиента
    protected void acceptServer() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Server connection lost.\n");
                break;
            }
        }
    }

    /**
     * Method was created to send message to all clients from Map
     *
     * @throws Exception - if message was not sent
     */
    //метод, рассылающий заданное сообщение всем клиентам из мапы
    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Error sending message to all users!\n");
            }
        }
    }

    //точка входа для приложения сервера
    public static void main(String[] args) {
        Server server = new Server();
        gui = new ViewGuiServer(server);
        model = new ModelGuiServer();
        gui.initFrameServer();
        //цикл снизу ждет true от флага isServerStart (при старте сервера в методе startServer устанавливается в true)
        //после чего запускается бесконечный цикл принятия подключения от клиента в  методе acceptServer
        //до тех пор пока сервер не остановится, либо не возникнет исключение
        while (true) {
            if (isServerStart) {
                server.acceptServer();
                isServerStart = false;
            }
        }
    }

    /**
     * Class {@code ServerThread}
     */
    //класс-поток, который запускается при принятии сервером нового сокетного соединения с клиентом, в конструктор
    //передается объект класса Socket
    private class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        /**
         * Method was created to realize the server request from the client for the name and adding the name to the Map
         *
         * @throws Exception - error occurred while requesting and adding a new user
         */
        //метод который реализует запрос сервера у клиента имени и добавлении имени в мапу
        private String requestAndAddingUser(Connection connection) {
            while (true) {
                try {
                    //посылаем клиенту сообщение-запрос имени
                    connection.send(new Message(MessageType.REQUEST_NAME_USER));
                    Message responseMessage = connection.receive();
                    String userName = responseMessage.getTextMessage();
                    //получили ответ с именем и проверяем не занято ли это имя другим клиентом
                    if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null && !userName.isEmpty() && !model.getAllUsersMultiChat().containsKey(userName)) {
                        //добавляем имя в мапу
                        model.addUser(userName, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Connection> users : model.getAllUsersMultiChat().entrySet()) {
                            listUsers.add(users.getKey());
                        }
                        //отправляем клиенту множетство имен всех уже подключившихся пользователей
                        connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));
                        //отправляем всем клиентам сообщение о новом пользователе
                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
                        return userName;
                    }
                    //если такое имя уже занято отправляем сообщение клиенту, что имя используется
                    else connection.send(new Message(MessageType.NAME_USED));
                } catch (Exception e) {
                    gui.refreshDialogWindowServer("An error occurred while requesting and adding a new user\n");
                }
            }
        }

        /**
         * Method was created to realize connection between users
         *
         * @throws Exception - error occurred while sending a message from the user  either disconnected
         */
        //метод, реализующий обмен сообщениями между пользователями
        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Message message = connection.receive();
                    //приняли сообщение от клиента, если тип сообщения TEXT_MESSAGE то пересылаем его всем пользователям
                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
                    }
                    //если тип сообщения DISABLE_USER, то рассылаем всем пользователям, что данный пользователь покинул чат,
                    //удаляем его из мапы, закрываем его connection
                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {
                        sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
                        model.removeUser(userName);
                        connection.close();
                        gui.refreshDialogWindowServer(String.format("Remote access user %s disconnected.\n", socket.getRemoteSocketAddress()));
                        break;
                    }
                } catch (Exception e) {
                    gui.refreshDialogWindowServer(String.format("An error occurred while sending a message from the user %s, either disconnected!\n", userName));
                    break;
                }
            }
        }

        /**
         * Method was overrided to run connection  in thread
         *
         * @throws Exception - error occurred while sending a message from the user
         */
        @Override
        public void run() {
            gui.refreshDialogWindowServer(String.format("A new user connected with a remote socket- %s.\n", socket.getRemoteSocketAddress()));
            try {
                //получаем connection при помощи принятого сокета от клиента и запрашиваем имя, регистрируем, запускаем
                //цикл обмена сообщениями между пользователями
                Connection connection = new Connection(socket);
                String nameUser = requestAndAddingUser(connection);
                messagingBetweenUsers(connection, nameUser);
            } catch (Exception e) {
                gui.refreshDialogWindowServer(String.format("An error occurred while sending a message from the user!\n "));
            }
        }
    }
}
