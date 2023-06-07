package me.none030.mortiskitpvp.kitpvp;

import java.util.HashMap;

public abstract class Manager {

    private final HashMap<String, String> messageById;

    public Manager() {
        this.messageById = new HashMap<>();
    }

    public void addMessage(String id, String message) {
        messageById.put(id, message);
    }

    public void addMessages(HashMap<String, String> messageById) {
        this.messageById.putAll(messageById);
    }

    public String getMessage(String id) {
        return messageById.get(id);
    }

    public HashMap<String, String> getMessageById() {
        return messageById;
    }
}
