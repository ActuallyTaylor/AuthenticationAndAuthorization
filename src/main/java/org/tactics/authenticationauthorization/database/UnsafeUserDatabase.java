package org.tactics.authenticationauthorization.database;

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
        if (user.name() == null || user.name().isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (user.password() == null || user.password().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if (users.stream().anyMatch(existing -> existing.name().equals(user.name()))) {
            throw new IllegalArgumentException("A user with that username already exists");
        }
        this.users.add(user);
    }

    public void updateUser(String currentName, String newName, String newPassword) {
        int index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).name().equals(currentName)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("No user exists with that username");
        }

        User currentUser = users.get(index);
        String updatedName = newName == null ? currentUser.name() : newName;
        String updatedPassword = newPassword == null ? currentUser.password() : newPassword;
        if (updatedName.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (updatedPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        boolean duplicateName = users.stream().anyMatch(user ->
                !user.name().equals(currentName) && user.name().equals(updatedName));
        if (duplicateName) {
            throw new IllegalArgumentException("A user with that username already exists");
        }

        users.set(index, new User(updatedName, updatedPassword, currentUser.authorizationLevel()));
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
