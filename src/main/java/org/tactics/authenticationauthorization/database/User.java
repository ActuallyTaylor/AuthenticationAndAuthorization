package org.tactics.authenticationauthorization.database;

public record User(String name, String password, AuthorizationLevel authorizationLevel) {

}
