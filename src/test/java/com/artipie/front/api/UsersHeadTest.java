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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Users.Head}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UsersHeadTest {

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
        this.users = new YamlUsers(UsersHeadTest.CREDS_YAML, this.asto);
    }

    @Test
    void returnsOkWhenUserFound() {
        final String uid = "Mark";
        this.asto.save(
            new Key.From(UsersHeadTest.CREDS_YAML),
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                // @checkstyle LineLengthCheck (5 lines)
                new YamlCredentialsTest.User(uid, Optional.of("Mark@example.com"), "writer", "admin")
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        final Response resp = Mockito.mock(Response.class);
        new Users.Head(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.OK_200);
    }

    @Test
    void returnsNotFoundWhenUserDoesNotExists() {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn("Someone");
        final Response resp = Mockito.mock(Response.class);
        new Users.Head(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.NOT_FOUND_404);
    }

}
