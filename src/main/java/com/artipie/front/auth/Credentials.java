/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Arrays;
import java.util.Collection;
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

    /**
     * Decorator of {@link Credentials}, which tries to find user among several
     * {@link Credentials} implementations, returns any user if found, empty optional
     * if user not found.
     * @since 0.2
     */
    class Any implements Credentials {

        /**
         * Origins list.
         */
        private final Collection<Credentials> creds;

        /**
         * Primary ctor.
         * @param creds Origins
         */
        public Any(final Collection<Credentials> creds) {
            this.creds = creds;
        }

        /**
         * Ctor.
         * @param creds Origins
         */
        public Any(final Credentials... creds) {
            this(Arrays.asList(creds));
        }

        @Override
        public Optional<User> user(final String name) {
            return this.creds.stream().filter(item -> item.user(name).isPresent()).findFirst()
                .flatMap(item -> item.user(name));
        }
    }

}
