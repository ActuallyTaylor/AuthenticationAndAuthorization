package org.tactics.authenticationauthorization.database;

public enum AuthorizationLevel {
    DEFAULT("default"),
    ADMIN("admin"),
    ;

    final String code;

    AuthorizationLevel(String s) {
        this.code = s;
    }

    public static String[] stringValues() {
        return new String[] { DEFAULT.code, ADMIN.code };
    }
}
