/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Optional;
import java.util.Set;

/**
 * User.
 *
 * @since 1.0
 */
public interface User {

    /**
     * Validate user password.
     *
     * @param pass Password to check
     * @return True if password is valid
     */
    boolean validatePassword(String pass);

    /**
     * User id (name).
     *
     * @return String id
     */
    String uid();

    /**
     * User groups.
     *
     * @return Readonly set of groups
     */
    Set<? extends String> groups();

    /**
     * User email.
     *
     * @return Email if present, empty otherwise
     */
    Optional<String> email();

}
