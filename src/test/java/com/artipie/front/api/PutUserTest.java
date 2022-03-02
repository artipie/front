/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.auth.YamlCredentialsTest;
import com.artipie.front.auth.YamlUsers;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.json.Json;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link PutUser}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PutUserTest {

    /**
     * Test credentials file name.
     */
    private static final Key CREDS_YAML = new Key.From("_creds.yaml");

    /**
     * Test storage.
     */
    private BlockingStorage asto;

    /**
     * Test users.
     */
    private com.artipie.front.auth.Users users;

    @BeforeEach
    void init() {
        this.asto = new BlockingStorage(new InMemoryStorage());
        this.users = new YamlUsers(PutUserTest.CREDS_YAML, this.asto);
    }

    @Test
    void addsUser() {
        this.asto.save(
            new Key.From(PutUserTest.CREDS_YAML),
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                // @checkstyle LineLengthCheck (5 lines)
                new YamlCredentialsTest.User("Mark", Optional.of("Mark@example.com"), "writer", "admin")
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        final var name = "john";
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn(name);
        Mockito.when(rqs.body()).thenReturn(this.rqBody(name));
        new PutUser(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CREATED_201);
        MatcherAssert.assertThat(
            new String(
                this.asto.value(new Key.From(PutUserTest.CREDS_YAML)), StandardCharsets.UTF_8
            ),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "credentials:",
                    "  Mark:",
                    "    pass: \"plain:123\"",
                    "    email: Mark@example.com",
                    "    groups:",
                    "      - admin",
                    "      - writer",
                    "  john:",
                    "    type: plain",
                    "    pass: 123",
                    "    email: john@example.com",
                    "    groups:",
                    "      - reader",
                    "      - creator"
                )
            )
        );
    }

    @Test
    void addsUserWhenFileDoesNotExists() {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        final var name = "Alice";
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn(name);
        Mockito.when(rqs.body()).thenReturn(this.rqBody(name));
        new PutUser(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CREATED_201);
        MatcherAssert.assertThat(
            new String(
                this.asto.value(new Key.From(PutUserTest.CREDS_YAML)), StandardCharsets.UTF_8
            ),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "credentials:",
                    "  Alice:",
                    "    type: plain",
                    "    pass: 123",
                    "    email: Alice@example.com",
                    "    groups:",
                    "      - reader",
                    "      - creator"
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"Alice\": {}}", "{\"Alice\": {\"type\": \"plain\"}}"})
    void returnBadRequest(final String body) {
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn("Alice");
        Mockito.when(rqs.body()).thenReturn(body);
        new PutUser(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    void returnConflictWhenUserAlreadyExists() {
        final var name = "Mark";
        this.asto.save(
            new Key.From(PutUserTest.CREDS_YAML),
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                new YamlCredentialsTest.User("Mark", "plain", "123")
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetUser.USER_PARAM)).thenReturn(name);
        new PutUser(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CONFLICT_409);
    }

    /**
     * Json request body.
     * @param name User name
     * @return Request body
     */
    private String rqBody(final String name) {
        return Json.createObjectBuilder().add(
            name,
            Json.createObjectBuilder().add("type", "plain").add("pass", "123")
                .add("email", String.format("%s@example.com", name))
                .add("groups", Json.createArrayBuilder().add("reader").add("creator").build())
                .build()
        ).build().toString();
    }

}
