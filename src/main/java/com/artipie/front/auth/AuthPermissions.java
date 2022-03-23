/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

/**
 * Authorization permissions.
 * @since 1.0
 */
public interface AuthPermissions {

    /**
     * Stub permissions for development and debugging.
     * Remove after actual implementation.
     */
    AuthPermissions STUB = (uid, perm) -> true;

    /**
     * Check if permissions is allowed for user.
     * @param uid User ID
     * @param perm Permission name
     * @return True if allowed
     */
    boolean allowed(String uid, String perm);
}
