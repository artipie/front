/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Collection;
import javax.json.JsonObject;

/**
 * Artipie users.
 * @since 0.1
 */
public interface Users {

    /**
     * List existing users.
     * @return Artipie users
     */
    Collection<? extends User> list();

    /**
     * Add user.
     * @param info User info (password, email, groups, etc)
     * @param uid User name
     */
    void add(JsonObject info, String uid);

    /**
     * Remove user by name.
     * @param uid User name
     * @throws com.artipie.front.api.NotFoundException If user does not exist
     */
    void remove(String uid);

}
