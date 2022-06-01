/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Credentials from env.
 * @since 0.2
 */
public final class EnvCredentials implements Credentials {

    /**
     * Environment name for user.
     */
    public static final String ENV_NAME = "ARTIPIE_USER_NAME";

    /**
     * Environment name for password.
     */
    private static final String ENV_PASS = "ARTIPIE_USER_PASS";

    /**
     * Environment variables.
     */
    private final Map<String, String> env;

    /**
     * Ctor.
     * @param env Environment
     */
    public EnvCredentials(final Map<String, String> env) {
        this.env = env;
    }

    /**
     * Default ctor with system environment.
     */
    public EnvCredentials() {
        this(System.getenv());
    }

    @Override
    public Optional<User> user(final String name) {
        Optional<User> result = Optional.empty();
        if (this.env.get(EnvCredentials.ENV_NAME) != null
            && this.env.get(EnvCredentials.ENV_PASS) == null) {
            throw new IllegalStateException(
                // @checkstyle LineLengthCheck (1 line)
                "Password is not set: env variable `ARTIPIE_USER_PASS` is required for env credentials"
            );
        } else if (
            Objects.equals(Objects.requireNonNull(name), this.env.get(EnvCredentials.ENV_NAME))
        ) {
            result = Optional.of(
                // @checkstyle AnonInnerLengthCheck (30 lines)
                new User() {
                    @Override
                    public boolean validatePassword(final String pass) {
                        return Objects.equals(
                            pass, EnvCredentials.this.env.get(EnvCredentials.ENV_PASS)
                        );
                    }

                    @Override
                    public String uid() {
                        return name;
                    }

                    @Override
                    public Set<? extends String> groups() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Optional<String> email() {
                        return Optional.empty();
                    }
                }
            );
        }
        return result;
    }
}
