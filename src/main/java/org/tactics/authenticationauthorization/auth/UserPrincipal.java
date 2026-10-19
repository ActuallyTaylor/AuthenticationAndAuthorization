package org.tactics.authenticationauthorization.auth;

import java.security.Principal;
import java.util.Objects;

public record UserPrincipal(String name) implements Principal {
    public UserPrincipal {
        Objects.requireNonNull(name, "name");
    }

    @Override
    public String getName() {
        return name;
    }
}
