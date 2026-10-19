package org.example;

import org.example.database.AuthorizationLevel;
import org.example.database.User;
import org.example.database.UnsafeUserDatabase;

import javax.security.auth.callback.*;
import java.io.Console;
import java.io.File;
import java.io.IOException;

public class Main {
    static String DATABASE_NAME = "user.json";

    static void main() {
        Console console = System.console();

        File file = new File(DATABASE_NAME);
        UnsafeUserDatabase vulnerableUserDatabase;

        // Check if the file exists
        if (file.exists()) {
            console.printf("Loading existing database!%n");
            vulnerableUserDatabase = UnsafeUserDatabase.readFrom(file);
        } else {
            console.printf("Creating new database!%n");
            vulnerableUserDatabase = new UnsafeUserDatabase();
            console.printf("Please register an admin account: %n");
            User adminUser = registerNewUser(AuthorizationLevel.ADMIN, true);
        }

        
    }

    static User registerNewUser(AuthorizationLevel level, Boolean autoAdmin) {
        Console console = System.console();

        CallbackHandler callbackHandler = new ConsoleCallbackHandler();

        NameCallback nameCallback = new NameCallback("username: ");
        PasswordCallback passwordCallback = new PasswordCallback("password: ", false);

        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
            String username = nameCallback.getName();
            String password = new String(passwordCallback.getPassword());

            AuthorizationLevel userLevel;

            if (autoAdmin) {
                userLevel = AuthorizationLevel.ADMIN;
            } else {
                if (level == AuthorizationLevel.ADMIN) {
                    String[] availableRoles = new String[]{AuthorizationLevel.ADMIN.toString(), AuthorizationLevel.DEFAULT.toString()};
                    ChoiceCallback authorizationCallback = new ChoiceCallback("Authentication: ", availableRoles, 0, false);
                    callbackHandler.handle(new Callback[]{authorizationCallback});

                    int[] selectedChoices = authorizationCallback.getSelectedIndexes();
                    String role = availableRoles[selectedChoices[0]];
                    userLevel = AuthorizationLevel.valueOf(role);
                } else {
                    userLevel = AuthorizationLevel.DEFAULT;
                }
            }

            console.printf("username: %s, password: %s", username, password);
            return new User(username, password, userLevel);
        } catch (IOException | UnsupportedCallbackException e) {
            console.printf("Error: %s", e.getMessage());
            return null;
        }
    }
}
