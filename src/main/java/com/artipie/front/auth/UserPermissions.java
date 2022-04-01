/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.YamlMapping;
import java.util.Optional;

/**
 * User authorization permissions.
 * @since 1.0
 */
public interface UserPermissions {

    /**
     * Stub permissions for development and debugging.
     * Remove after actual implementation.
     */
    UserPermissions STUB = (uid, perm) -> true;

    /**
     * Check if permissions is allowed for user.
     * @param uid User ID
     * @param perm Permission name
     * @return True if allowed
     */
    boolean allowed(String uid, String perm);

    /**
     * Permissions from yaml.
     * @since 0.1
     */
    final class FromYaml implements UserPermissions {

        /**
         * Users permissions yaml.
         */
        private final YamlMapping yaml;

        /**
         * Ctor.
         * @param yaml Users permissions yaml
         */
        public FromYaml(final YamlMapping yaml) {
            this.yaml = yaml;
        }

        @Override
        public boolean allowed(final String uid, final String perm) {
            return Optional.ofNullable(this.yaml.yamlSequence(uid))
                .map(
                    seq -> seq.values().stream().map(item -> item.asScalar().value())
                        .anyMatch(perm::equals)
                ).orElse(false);
        }
    }
}
