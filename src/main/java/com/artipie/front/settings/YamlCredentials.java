/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.front.auth.Credentials;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Yaml credentials parser.
 * @since 1.0
 */
public final class YamlCredentials implements Credentials {

    /**
     * Yaml source.
     */
    private final YamlMapping mapping;

    /**
     * New yaml credentials.
     * @param mapping Yaml
     */
    public YamlCredentials(final YamlMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public Optional<Credentials.User> user(final String name) {
        return Optional.ofNullable(this.mapping.yamlMapping("credentials"))
            .flatMap(cred -> Optional.ofNullable(cred.yamlMapping(name)))
            .map(User::new);
    }

    /**
     * Yaml user item.
     * @since 1.0
     */
    private static final class User implements Credentials.User {

        /**
         * Yaml source.
         */
        private final YamlMapping mapping;

        /**
         * New user.
         * @param mapping Yaml
         */
        private User(final YamlMapping mapping) {
            this.mapping = mapping;
        }

        @Override
        public boolean validatePassword(final String pass) {
            final var config = this.mapping.string("pass");
            if (config == null) {
                throw new IllegalStateException(
                    "invalid credentials configuration: `pass` field not found"
                );
            }
            final var type = this.mapping.string("type");
            final boolean res;
            if (type == null) {
                res = validateStringPass(config, pass);
            } else {
                res = validateStringPass(String.join(":", type, config), pass);
            }
            return res;
        }

        @Override
        public Set<? extends String> groups() {
            return Optional.ofNullable(this.mapping.yamlSequence("groups"))
                .map(seq -> StreamSupport.stream(seq.spliterator(), false))
                .orElse(Stream.empty())
                .map(node -> node.asScalar().value())
                .collect(Collectors.toSet());
        }

        @Override
        public Optional<String> email() {
            return Optional.ofNullable(this.mapping.string("email"));
        }

        /**
         * Validate password string.
         * @param config Passowrd string config
         * @param pass Actual password
         * @return True if password is valid
         * @checkstyle ReturnCountCheck (30 lines)
         */
        @SuppressWarnings("PMD.OnlyOneReturn")
        private static boolean validateStringPass(final String config, final String pass) {
            final var parts = config.split(":");
            switch (parts[0]) {
                case "plain":
                    return Objects.equals(parts[1], pass);
                case "sha256":
                    return Objects.equals(parts[1], DigestUtils.sha256Hex(pass));
                default:
                    throw new IllegalStateException(
                        String.format(
                            "invalid credentials configuration: `pass` type `%s` is not supported",
                                parts[0]
                        )
                    );
            }
        }
    }
}
