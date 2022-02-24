/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Optional;

/**
 * By password authentication.
 * @since 1.0
 */
@FunctionalInterface
public interface AuthByPassword {

    /**
     * Authenticate user by password.
     * @param user Login
     * @param password Password
     * @return User ID if found
     */
    Optional<String> authenticate(String user, String password);

    /**
     * Authenticator with credentials.
     * @param cred Credentials
     * @return Authentication function
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    static AuthByPassword withCredentials(final Credentials cred) {
        return (name, pass) -> cred.user(name)
            .filter(user -> user.validatePassword(pass))
            .map(ignore -> name);
    }
}
