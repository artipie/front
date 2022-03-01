/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.YamlMapping;
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
     * Yaml credentials parser.
     * @since 1.0
     */
    final class FromYaml implements Credentials {

        /**
         * Yaml source.
         */
        private final YamlMapping mapping;

        /**
         * New yaml credentials.
         * @param mapping Yaml
         */
        public FromYaml(final YamlMapping mapping) {
            this.mapping = mapping;
        }

        @Override
        public Optional<User> user(final String name) {
            return Optional.ofNullable(this.mapping.yamlMapping("credentials"))
                .flatMap(cred -> Optional.ofNullable(cred.yamlMapping(name)))
                .map(yaml -> new User.FromYaml(yaml, name));
        }

    }
}
