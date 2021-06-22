package com.solution.server;

import com.solution.connection.Connection;

import java.util.HashMap;
import java.util.Map;

/**
 * Class {@code ModelGuiServer}
 *
 * Was created for saving connected clients
 */
public class ModelGuiServer {
    //модель хранит карту со всеми подключившимися клиентами ключ - имя клиента, значение - объект connection
    private Map<String, Connection> allUsersMultiChat = new HashMap<>();

    protected Map<String, Connection> getAllUsersMultiChat() {
        return allUsersMultiChat;
    }

    protected void addUser(String nameUser, Connection connection) {
        allUsersMultiChat.put(nameUser, connection);
    }

    protected void removeUser(String nameUser) {
        allUsersMultiChat.remove(nameUser);
    }
}

