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

    static void main()  {
        Console console = System.console();

        File databaseFile = new File(DATABASE_NAME);
        UnsafeUserDatabase userDatabase;

        // Check if the file exists
        if (databaseFile.exists()) {
            console.printf("Loading existing database!%n");
            userDatabase = UnsafeUserDatabase.readFrom(databaseFile);
        } else {
            console.printf("Creating new database!%n");
            userDatabase = new UnsafeUserDatabase();
            console.printf("Please register an admin account: %n");

            try {
                User adminUser = registerNewUser(AuthorizationLevel.ADMIN, true);
                userDatabase.addUser(adminUser);
                userDatabase.writeTo(databaseFile);
            } catch (IOException | UnsupportedCallbackException e) {
                console.printf("Failed to register an initial user: %s", e.getMessage());
                return;
            }
        }

        try {
            console.printf("Please log in: %n");
            loginUser(userDatabase);
        } catch (IOException | UnsupportedCallbackException e) {
            console.printf("Failed to login: %s", e.getMessage());
            return;
        }
    }

    /// Prompts a user to register a new user. User's with an AuthorizationLevel of ADMIN will be able to add
    /// new admin users. Non-admin users will be able to add regular users.
    ///
    /// @param level The AuthorizationLevel of the current user.
    /// @param autoAdmin Should this function automatically create an admin user (First Registration).
    /// @return A User object representing the newly registered user.
    static User registerNewUser(AuthorizationLevel level, Boolean autoAdmin) throws IOException, UnsupportedCallbackException {
        Console console = System.console();

        CallbackHandler callbackHandler = new ConsoleCallbackHandler();

        NameCallback nameCallback = new NameCallback("username: ");
        PasswordCallback passwordCallback = new PasswordCallback("password: ", false);

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
    }

    static User loginUser(UnsafeUserDatabase userDatabase) throws IOException, UnsupportedCallbackException {
        Console console = System.console();

        CallbackHandler callbackHandler = new ConsoleCallbackHandler();

        NameCallback nameCallback = new NameCallback("username: ");
        PasswordCallback passwordCallback = new PasswordCallback("password: ", false);

        callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        String username = nameCallback.getName();
        String password = new String(passwordCallback.getPassword());

        return userDatabase.login(username, password);
    }
}
