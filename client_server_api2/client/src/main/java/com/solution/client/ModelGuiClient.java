package com.solution.client;
import java.util.HashSet;
import java.util.Set;

/**
 * Class {@code ModelGuiClient}
 * Was created to save all clients
 */
public class ModelGuiClient {
    //в модели клиентского приложения хранится множетство подключившихся пользователей
    private Set<String> users = new HashSet<>();

    protected Set<String> getUsers() {
        return users;
    }

    protected void addUser(String nameUser) {
        users.add(nameUser);
    }

    protected void removeUser(String nameUser) {
        users.remove(nameUser);
    }

    protected void setUsers(Set<String> users) {
        this.users = users;
    }
}