/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.auth.Users;
import com.artipie.front.auth.YamlCredentialsTest;
import com.artipie.front.auth.YamlUsers;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link DeleteUser}.
 * @since 0.1
 */
class DeleteUserTest {

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
        this.users = new YamlUsers(DeleteUserTest.KEY, this.blsto);
    }

    @Test
    void removesUser() {
        final String alice = "Alice";
        this.blsto.save(
            DeleteUserTest.KEY,
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.STRUCT,
                new YamlCredentialsTest.User(alice, Optional.of("alice@example.com"), "admin"),
                new YamlCredentialsTest.User("Bob", Optional.of("bob@example.com"))
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetUser.USER_PARAM.toString())).thenReturn(alice);
        final Response resp = Mockito.mock(Response.class);
        new DeleteUser(this.users).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.OK_200);
        MatcherAssert.assertThat(
            this.users.list().size(),
            new IsEqual<>(1)
        );
    }

}
