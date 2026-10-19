package org.example.database;

import java.io.File;
import java.util.ArrayList;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

/// A very bad user database that just saves user information into a json file with plaintext passwords.
public class UnsafeUserDatabase {
    ArrayList<User> users;

    public UnsafeUserDatabase() { }

    static public UnsafeUserDatabase readFrom(File file) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, UnsafeUserDatabase.class);
    }

    public void writeTo(File file) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer();
        writer.writeValue(file, this);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void login(User user) {
        for (User u : users) {
            if (u.name().equals(user.name()) && u.password().equals(user.password())) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid username or password");
    }
}
