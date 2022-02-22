/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Optional;
import java.util.Set;

/**
 * Credentials.
 * @since 1.0
 */
public interface Credentials {
    /**
     * Find user by name.
     * @param name Username
     * @return User if found
     */
    Optional<User> user(String name);

    /**
     * User.
     * @since 1.0
     */
    interface User {

        /**
         * Validate user password.
         * @param pass Password to check
         * @return True if password is valid
         */
        boolean validatePassword(String pass);

        /**
         * User groups.
         * @return Readonly set of groups
         */
        Set<? extends String> groups();
    }
}
