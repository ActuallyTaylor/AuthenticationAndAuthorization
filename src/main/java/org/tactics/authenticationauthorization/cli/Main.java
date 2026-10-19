package org.tactics.authenticationauthorization.cli;

import org.tactics.authenticationauthorization.auth.RolePrincipal;
import org.tactics.authenticationauthorization.auth.UserLoginModule;
import org.tactics.authenticationauthorization.auth.UserPrincipal;
import org.tactics.authenticationauthorization.database.AuthorizationLevel;
import org.tactics.authenticationauthorization.database.UnsafeUserDatabase;
import org.tactics.authenticationauthorization.database.User;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class Main {
    static String DATABASE_NAME = "user.json";
    private static final String[] SERVICES = {
            "/users (DEFAULT)",
            "/register (ADMIN)",
            "/edit-user (ADMIN)",
            "/exit"
    };

    static void main() {
        Console console = System.console();
        if (console == null) {
            System.err.println("This application requires an interactive console.");
            return;
        }

        File databaseFile = new File(DATABASE_NAME);
        UnsafeUserDatabase userDatabase;
        if (databaseFile.exists()) {
            console.printf("Loading existing database!%n");
            userDatabase = UnsafeUserDatabase.readFrom(databaseFile);
        } else {
            console.printf("Creating new database!%n");
            userDatabase = new UnsafeUserDatabase();
            console.printf("Please register an admin account:%n");
            try {
                userDatabase.addUser(registerNewUser(true));
                userDatabase.writeTo(databaseFile);
            } catch (IOException | IllegalArgumentException
                     | javax.security.auth.callback.UnsupportedCallbackException e) {
                console.printf("Failed to register an initial user: %s%n", e.getMessage());
                return;
            }
        }

        CallbackHandler callbackHandler = new ConsoleCallbackHandler();
        Subject subject;
        try {
            console.printf("Please log in:%n");
            LoginContext loginContext = createLoginContext(userDatabase, callbackHandler);
            loginContext.login();
            subject = loginContext.getSubject();
            console.printf("Logged in as %s.%n", subject.getPrincipals(UserPrincipal.class)
                    .stream().findFirst().map(UserPrincipal::getName).orElse("unknown"));
        } catch (LoginException e) {
            console.printf("Failed to login: %s%n", e.getMessage());
            return;
        }

        while (true) {
            ChoiceCallback serviceCallback = new ChoiceCallback("Choose a service:", SERVICES, 0, false);
            try {
                callbackHandler.handle(new Callback[]{serviceCallback});
            } catch (IOException | javax.security.auth.callback.UnsupportedCallbackException e) {
                throw new IllegalStateException("Console callback handler rejected a service choice", e);
            }

            int choice = serviceCallback.getSelectedIndexes()[0];
            if (choice == SERVICES.length - 1) {
                return;
            }

            switch (choice) {
                case 0 -> invokeService(subject, AuthorizationLevel.DEFAULT, "/users",
                        () -> listUsers(userDatabase, console));
                case 1 -> invokeService(subject, AuthorizationLevel.ADMIN, "/register", () -> {
                    userDatabase.addUser(registerNewUser(false));
                    userDatabase.writeTo(databaseFile);
                    console.printf("User registered.%n");
                });
                case 2 -> invokeService(subject, AuthorizationLevel.ADMIN, "/edit-user", () -> {
                    editUser(userDatabase, callbackHandler, console);
                    userDatabase.writeTo(databaseFile);
                });
                default -> throw new IllegalStateException("Unknown service selection: " + choice);
            }
        }
    }

    private static LoginContext createLoginContext(UnsafeUserDatabase userDatabase,
                                                   CallbackHandler callbackHandler) throws LoginException {
        Configuration configuration = new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                if (!"console-login".equals(name)) {
                    return null;
                }
                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                                UserLoginModule.class.getName(),
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                Map.of("userDatabase", userDatabase))
                };
            }
        };
        return new LoginContext("console-login", null, callbackHandler, configuration);
    }

    private static void invokeService(Subject subject, AuthorizationLevel requiredLevel,
                                      String serviceUrl, ServiceAction action) {
        try {
            Subject.callAs(subject, () -> {
                authorizeCurrentSubject(requiredLevel);
                action.run();
                return null;
            });
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SecurityException) {
                System.out.printf("Access denied to %s: %s%n", serviceUrl, cause.getMessage());
            } else if (cause instanceof IOException
                    || cause instanceof javax.security.auth.callback.UnsupportedCallbackException
                    || cause instanceof IllegalArgumentException) {
                System.out.printf("Service %s failed: %s%n", serviceUrl, cause.getMessage());
            } else {
                throw e;
            }
        } catch (SecurityException e) {
            System.out.printf("Access denied to %s: %s%n", serviceUrl, e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.printf("Service %s failed: %s%n", serviceUrl, e.getMessage());
        }
    }

    private static void authorizeCurrentSubject(AuthorizationLevel requiredLevel) {
        Subject subject = Subject.current();
        boolean isAdmin = subject.getPrincipals(RolePrincipal.class)
                .contains(new RolePrincipal(AuthorizationLevel.ADMIN.name()));
        boolean hasRequiredRole = subject.getPrincipals(RolePrincipal.class)
                .contains(new RolePrincipal(requiredLevel.name()));
        if (!hasRequiredRole && !(requiredLevel == AuthorizationLevel.DEFAULT && isAdmin)) {
            throw new SecurityException("requires the " + requiredLevel.name() + " role");
        }
    }

    private static void listUsers(UnsafeUserDatabase userDatabase, Console console) {
        console.printf("Users:%n");
        userDatabase.users.forEach(user -> console.printf(" - %s%n", user.name()));
    }

    private static void editUser(UnsafeUserDatabase userDatabase, CallbackHandler callbackHandler,
                                 Console console) throws IOException,
            javax.security.auth.callback.UnsupportedCallbackException {
        NameCallback targetCallback = new NameCallback("Username to edit: ");
        NameCallback newNameCallback = new NameCallback("New username (blank to keep): ");
        PasswordCallback newPasswordCallback = new PasswordCallback("New password (blank to keep): ", false);
        callbackHandler.handle(new Callback[]{targetCallback, newNameCallback, newPasswordCallback});

        String targetName = targetCallback.getName();
        String newName = newNameCallback.getName();
        char[] password = newPasswordCallback.getPassword();
        try {
            String newPassword = password == null || password.length == 0 ? null : new String(password);
            userDatabase.updateUser(targetName, newName == null || newName.isBlank() ? null : newName,
                    newPassword);
        } finally {
            newPasswordCallback.clearPassword();
        }
        console.printf("User updated.%n");
    }

    /// Prompts for a new user. The initial account is always assigned ADMIN.
    static User registerNewUser(boolean autoAdmin)
            throws IOException, javax.security.auth.callback.UnsupportedCallbackException {
        CallbackHandler callbackHandler = new ConsoleCallbackHandler();
        NameCallback nameCallback = new NameCallback("username: ");
        PasswordCallback passwordCallback = new PasswordCallback("password: ", false);

        callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        String username = nameCallback.getName();
        char[] password = passwordCallback.getPassword();
        String passwordText;
        try {
            passwordText = password == null ? "" : new String(password);
        } finally {
            passwordCallback.clearPassword();
        }

        if (autoAdmin) {
            return new User(username, passwordText, AuthorizationLevel.ADMIN);
        }

        String[] availableRoles = {AuthorizationLevel.ADMIN.name(), AuthorizationLevel.DEFAULT.name()};
        ChoiceCallback authorizationCallback = new ChoiceCallback("Authorization level:", availableRoles,
                0, false);
        callbackHandler.handle(new Callback[]{authorizationCallback});
        AuthorizationLevel userLevel = AuthorizationLevel.valueOf(availableRoles[authorizationCallback.getSelectedIndexes()[0]]);
        return new User(username, passwordText, userLevel);
    }

    @FunctionalInterface
    private interface ServiceAction {
        void run() throws IOException, javax.security.auth.callback.UnsupportedCallbackException;
    }
}
