package org.tactics.authenticationauthorization.auth;

import org.tactics.authenticationauthorization.database.UnsafeUserDatabase;
import org.tactics.authenticationauthorization.database.User;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

public class UserLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private UnsafeUserDatabase userDatabase;
    private User authenticatedUser;
    private boolean succeeded;
    private boolean committed;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        Object configuredDatabase = options.get("userDatabase");
        if (!(configuredDatabase instanceof UnsafeUserDatabase database)) {
            throw new IllegalArgumentException("JAAS configuration must provide a userDatabase");
        }
        this.userDatabase = database;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("No callback handler was provided");
        }

        NameCallback nameCallback = new NameCallback("username: ");
        PasswordCallback passwordCallback = new PasswordCallback("password: ", false);
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (Exception e) {
            LoginException failure = new LoginException("Unable to read login credentials");
            failure.initCause(e);
            throw failure;
        }

        char[] suppliedPassword = passwordCallback.getPassword();
        try {
            authenticatedUser = userDatabase.login(nameCallback.getName(),
                    suppliedPassword == null ? "" : new String(suppliedPassword));
            succeeded = true;
            return true;
        } catch (IllegalArgumentException e) {
            LoginException failure = new LoginException(e.getMessage());
            failure.initCause(e);
            throw failure;
        } finally {
            passwordCallback.clearPassword();
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!succeeded) {
            return false;
        }
        if (subject.isReadOnly()) {
            throw new LoginException("Cannot add principals to a read-only subject");
        }

        subject.getPrincipals().add(new UserPrincipal(authenticatedUser.name()));
        subject.getPrincipals().add(new RolePrincipal(authenticatedUser.authorizationLevel().name()));
        committed = true;
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        if (!succeeded) {
            return false;
        }
        if (!committed) {
            succeeded = false;
            authenticatedUser = null;
        } else {
            logout();
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        if (subject.isReadOnly()) {
            throw new LoginException("Cannot remove principals from a read-only subject");
        }
        subject.getPrincipals().remove(new UserPrincipal(authenticatedUser.name()));
        subject.getPrincipals().remove(new RolePrincipal(authenticatedUser.authorizationLevel().name()));
        succeeded = false;
        committed = false;
        authenticatedUser = null;
        return true;
    }
}
