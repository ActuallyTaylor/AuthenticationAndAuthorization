package org.example.database;

import java.io.File;
import java.util.ArrayList;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

/// A very bad user database that just saves user information into a json file with plaintext passwords.
public class UnsafeUserDatabase {
    public ArrayList<User> users = new ArrayList<>();

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
        this.users.add(user);
    }

    public User login(String name, String password) {
        for (User u : users) {
            if (u.name().equals(name) && u.password().equals(password)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Invalid username or password");
    }
}
