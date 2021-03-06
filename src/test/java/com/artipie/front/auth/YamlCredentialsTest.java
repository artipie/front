/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link YamlCredentials}.
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class YamlCredentialsTest {

    @Test
    void findUser() {
        final var username = "John";
        final var user = new YamlCredentials(
            credYaml(PasswordFormat.SIMPLE, new User(username, "plain", "qwerty"))
        ).user(username);
        MatcherAssert.assertThat(
            user.isPresent(), Matchers.is(true)
        );
    }

    @Test
    void validateShaPass() {
        final var username = "Alice";
        final var user = new YamlCredentials(
            // @checkstyle LineLengthCheck (1 line)
            credYaml(PasswordFormat.SIMPLE, new User(username, "sha256", "65e84be33532fb784c48129675f9eff3a682b27168c0ea744b2cf58ee02337c5"))
        ).user(username);
        MatcherAssert.assertThat(
            user.orElseThrow().validatePassword("qwerty"),
            Matchers.is(true)
        );
    }

    @Test
    void validatePlainPass() {
        final var username = "Bob";
        final var user = new YamlCredentials(
            credYaml(PasswordFormat.SIMPLE, new User(username, "plain", "1234"))
        ).user(username);
        MatcherAssert.assertThat(
            user.orElseThrow().validatePassword("1234"),
            Matchers.is(true)
        );
    }

    @Test
    void validateStrucPass() {
        final var username = "John";
        final var type = "plain";
        final var pass = "zxcvb";
        final var user = new YamlCredentials(
            credYaml(PasswordFormat.STRUCT, new User(username, type, pass))
        ).user(username).get();
        MatcherAssert.assertThat(
            user.validatePassword(pass),
            Matchers.is(true)
        );
    }

    @Test
    void readUserGroups() {
        final var username = "Jane";
        final var admins = "admins";
        final var readers = "readers";
        final var groups = new YamlCredentials(
            credYaml(PasswordFormat.SIMPLE, new User(username, "plain", "qwerty", admins, readers))
        ).user(username).orElseThrow().groups();
        MatcherAssert.assertThat(
            groups,
            Matchers.containsInAnyOrder(admins, readers)
        );
    }

    @Test
    void readUserEmail() {
        final var username = "Olga";
        final var email = "olga@example.com";
        MatcherAssert.assertThat(
            new YamlCredentials(
                credYaml(
                    PasswordFormat.SIMPLE,
                    new User(username, "plain", "qwerty", Optional.of(email))
                )
            ).user(username).orElseThrow().email().get(),
            new IsEqual<>(email)
        );
    }

    /**
     * Credentials YAML.
     * @param fmt Password format
     * @param users User list
     * @return Yaml
     * @checkstyle MethodsOrderCheck (10 lines)
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static YamlMapping credYaml(final PasswordFormat fmt, final User... users) {
        var builder = Yaml.createYamlMappingBuilder();
        for (final var user : users) {
            builder = user.render(builder, fmt);
        }
        return Yaml.createYamlMappingBuilder().add("credentials", builder.build()).build();
    }

    /**
     * User in yaml.
     * @since 1.0
     */
    public static final class User {

        /**
         * User name.
         */
        private final String name;

        /**
         * User password type.
         */
        private final String ptype;

        /**
         * User password.
         */
        private final String pass;

        /**
         * User password.
         */
        private final Optional<String> email;

        /**
         * User groups.
         */
        private final Set<String> groups;

        /**
         * New user.
         * @param name User name
         * @param ptype User password type
         * @param pass User password
         * @param email User email
         * @param groups User groups
         * @checkstyle ParameterNumberCheck (10 lines)
         */
        public User(final String name, final String ptype, final String pass,
            final Optional<String> email, final String... groups) {
            this.name = name;
            this.ptype = ptype;
            this.pass = pass;
            this.email = email;
            this.groups = new HashSet<>(Arrays.asList(groups));
        }

        /**
         * New user.
         * @param name User name
         * @param ptype User password type
         * @param pass User password
         * @param groups User groups
         * @checkstyle ParameterNumberCheck (10 lines)
         */
        public User(final String name, final String ptype, final String pass,
            final String... groups) {
            this(name, ptype, pass, Optional.empty(), groups);
        }

        /**
         * New user.
         * @param name User name
         * @param email User email
         * @param groups User groups
         * @checkstyle ParameterNumberCheck (10 lines)
         */
        public User(final String name, final Optional<String> email, final String... groups) {
            this(name, "plain", "123", email, groups);
        }

        /**
         * Render user to a yaml.
         * @param builder Yaml builder
         * @param fmt Password format
         * @return Yaml builder
         */
        YamlMappingBuilder render(final YamlMappingBuilder builder, final PasswordFormat fmt) {
            var yaml = Yaml.createYamlMappingBuilder();
            yaml = fmt.apply(yaml, this.ptype, this.pass);
            if (this.email.isPresent()) {
                yaml = yaml.add("email", this.email.get());
            }
            if (!this.groups.isEmpty()) {
                var gbuild = Yaml.createYamlSequenceBuilder();
                for (final var group : this.groups) {
                    gbuild = gbuild.add(group);
                }
                yaml = yaml.add("groups", gbuild.build());
            }
            return builder.add(this.name, yaml.build());
        }
    }

    /**
     * Format of user password.
     * @since 1.0
     */
    @FunctionalInterface
    public interface PasswordFormat {

        /**
         * Simple format, like {@code plain:qwerty}.
         */
        PasswordFormat SIMPLE =
            (builder, type, pass) -> builder.add("pass", String.format("%s:%s", type, pass));

        /**
         * Structured format, like {@code {type: plain, pass: qwerty}}.
         */
        PasswordFormat STRUCT =
            (builder, type, pass) -> builder.add("type", type).add("pass", pass);

        /**
         * Apply format using builder.
         * @param builder Yaml builder
         * @param type Password type
         * @param pass Password value
         * @return Builder
         */
        YamlMappingBuilder apply(YamlMappingBuilder builder, String type, String pass);
    }
}
