package org.tactics.authenticationauthorization.auth;

import java.security.Principal;
import java.util.Objects;

public record RolePrincipal(String name) implements Principal {
    public RolePrincipal {
        Objects.requireNonNull(name, "name");
    }

    @Override
    public String getName() {
        return name;
    }
}
