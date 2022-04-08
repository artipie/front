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
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Users.GetUser}.
 * @since 0.1
 */
class UsersGetUserTest {

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
        this.users = new YamlUsers(UsersGetUserTest.CREDS_YAML, this.asto);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "Alice;{\"Alice\":{\"email\":\"alice@example.com\",\"groups\":[]}}",
        "John;{\"John\":{\"groups\":[\"reader\",\"dev-lead\"]}}",
        "Mark;{\"Mark\":{\"email\":\"Mark@example.com\",\"groups\":[\"admin\",\"writer\"]}}"
        }, delimiterString = ";")
    void returnsUserInfo(final String name, final String res) throws JSONException {
        this.asto.save(
            new Key.From(UsersGetUserTest.CREDS_YAML),
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                // @checkstyle LineLengthCheck (5 lines)
                new YamlCredentialsTest.User("Alice", Optional.of("alice@example.com")),
                new YamlCredentialsTest.User("John", Optional.empty(), "reader", "dev-lead"),
                new YamlCredentialsTest.User("Mark", Optional.of("Mark@example.com"), "writer", "admin")
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(name);
        JSONAssert.assertEquals(
            new Users.GetUser(this.users).handle(rqs, Mockito.mock(Response.class)),
            res,
            true
        );
    }

    @Test
    void returnNotFound() {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn("any");
        final Response resp = Mockito.mock(Response.class);
        new Users.GetUser(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.NOT_FOUND_404);
    }

}
