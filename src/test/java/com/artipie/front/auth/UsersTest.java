/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.api.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.json.Json;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link YamlUsers}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UsersTest {

    /**
     * Test credentials key.
     */
    private static final Key KEY = new Key.From("creds.yaml");

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    /**
     * Test users.
     */
    private Users users;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
        this.users = new YamlUsers(UsersTest.KEY, this.blsto);
    }

    @Test
    void listUsers() {
        this.blsto.save(
            UsersTest.KEY,
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.STRUCT,
                new YamlCredentialsTest.User("Alice", Optional.of("alice@example.com"), "admin"),
                new YamlCredentialsTest.User("Bob", Optional.of("bob@example.com"))
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final Collection<? extends User> list = this.users.list();
        MatcherAssert.assertThat(
            "Failed to list users, collection size should be 2",
            list.size(),
            new IsEqual<>(2)
        );
        MatcherAssert.assertThat(
            "Failed to list users, collection should contain Alice and Bob",
            list.stream()
                .filter(item -> "Alice".equals(item.uid()) || "Bob".equals(item.uid())).count(),
            new IsEqual<>(2L)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void returnsEmptyList(final boolean add) {
        if (add) {
            this.blsto.save(UsersTest.KEY, new byte[]{});
        }
        MatcherAssert.assertThat(
            this.users.list(),
            new IsEmptyCollection<>()
        );
    }

    @ParameterizedTest
    @MethodSource("creds")
    void addsUser(final Pair<YamlMapping, Boolean> pair) {
        if (pair.getRight()) {
            this.blsto.save(
                UsersTest.KEY,
                pair.getLeft().toString().getBytes(StandardCharsets.UTF_8)
            );
        }
        final String alice = "Alice";
        final String email = "Alice@example.com";
        final String pass = "123";
        this.users.add(
            Json.createObjectBuilder().add("type", "plain").add("pass", pass)
                .add("email", email)
                .add("groups", Json.createArrayBuilder().add("reader").add("creator").build())
                .build(),
            alice
        );
        final User nuser = this.users.list().stream()
            .filter(usr -> alice.equals(usr.uid())).findFirst().get();
        MatcherAssert.assertThat(
            "Failed to add user email",
            nuser.email().get(),
            new IsEqual<>(email)
        );
        MatcherAssert.assertThat(
            "Failed to add password",
            nuser.validatePassword(pass)
        );
        MatcherAssert.assertThat(
            "Failed to add groups",
            nuser.groups(),
            Matchers.containsInAnyOrder("reader", "creator")
        );
    }

    @ParameterizedTest
    @MethodSource("creds")
    void throwsNotFoundErrorWhenUserNotExist(final Pair<YamlMapping, Boolean> pair) {
        if (pair.getRight()) {
            this.blsto.save(
                UsersTest.KEY,
                pair.getLeft().toString().getBytes(StandardCharsets.UTF_8)
            );
        }
        Assertions.assertThrows(
            NotFoundException.class,
            () -> this.users.remove("Alice")
        );
    }

    @Test
    void removesUser() {
        this.blsto.save(
            UsersTest.KEY,
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                new YamlCredentialsTest.User("John", "sha256", "xxx"),
                new YamlCredentialsTest.User("Mark", "plain", "098")
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        this.users.remove("John");
        MatcherAssert.assertThat(
            this.users.list().size(),
            new IsEqual<>(1)
        );
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Pair<YamlMapping, Boolean>> creds() {
        return Stream.of(
            new ImmutablePair<>(
                YamlCredentialsTest.credYaml(
                    YamlCredentialsTest.PasswordFormat.STRUCT,
                    new YamlCredentialsTest.User("Bob", Optional.of("bob@example.com"))
                ),
                true
            ),
            new ImmutablePair<>(Yaml.createYamlMappingBuilder().build(), true),
            new ImmutablePair<>(Yaml.createYamlMappingBuilder().build(), false)
        );
    }

}
