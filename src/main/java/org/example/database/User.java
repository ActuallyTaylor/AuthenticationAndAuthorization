package org.example.database;

public record User(String name, String password, AuthorizationLevel authorizationLevel) {

}
