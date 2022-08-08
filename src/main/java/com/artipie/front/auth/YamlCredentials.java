/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.misc.UncheckedScalar;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Yaml credentials parser.
 *
 * @since 1.0
 */
public final class YamlCredentials implements Credentials {

    /**
     * Cache for credentials settings.
     */
    private final LoadingCache<String, YamlMapping> creds;

    /**
     * Ctor.
     * @param creds Credentials
     */
    public YamlCredentials(final LoadingCache<String, YamlMapping> creds) {
        this.creds = creds;
    }

    /**
     * New yaml credentials.
     *
     * @param asto Blocking storage
     * @param key Credentials key
     */
    public YamlCredentials(final BlockingStorage asto, final Key key) {
        this(
            CacheBuilder.newBuilder()
                //@checkstyle MagicNumberCheck (1 line)
                .expireAfterWrite(60, TimeUnit.SECONDS).softValues()
                .build(
                    new CacheLoader<>() {
                        @Override
                        public YamlMapping load(final String name) {
                            final YamlMapping res;
                            if (asto.exists(key)) {
                                try {
                                    res = Yaml.createYamlInput(
                                        new String(asto.value(key), StandardCharsets.UTF_8)
                                    ).readYamlMapping();
                                } catch (final IOException err) {
                                    throw new UncheckedIOException(err);
                                }
                            } else {
                                res = Yaml.createYamlMappingBuilder().build();
                            }
                            return res;
                        }
                    }
                )
        );
    }

    @Override
    public Optional<User> user(final String name) {
        return Optional.ofNullable(
            new UncheckedScalar<>(() -> this.creds.get("any")).value().yamlMapping("credentials")
        )
            .flatMap(cred -> Optional.ofNullable(cred.yamlMapping(name)))
            .map(yaml -> new YamlUser(yaml, name));
    }

    /**
     * Yaml user item.
     * @since 1.0
     */
    public static final class YamlUser implements User {

        /**
         * Yaml source.
         */
        private final YamlMapping mapping;

        /**
         * User name (id).
         */
        private final String name;

        /**
         * New user.
         * @param mapping Yaml
         * @param name User name
         */
        public YamlUser(final YamlMapping mapping, final String name) {
            this.mapping = mapping;
            this.name = name;
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
        public String uid() {
            return this.name;
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
