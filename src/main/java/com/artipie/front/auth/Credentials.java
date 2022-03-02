/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Optional;

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

}
