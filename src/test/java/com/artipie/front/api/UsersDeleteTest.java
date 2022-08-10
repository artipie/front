/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.auth.Credentials;
import com.artipie.front.auth.User;
import com.artipie.front.auth.Users;
import com.artipie.front.auth.YamlCredentialsTest;
import com.artipie.front.auth.YamlUsers;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link com.artipie.front.api.Users.Delete}.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (100 lines)
 */
class UsersDeleteTest {

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
        this.users = new YamlUsers(UsersDeleteTest.KEY, this.blsto);
    }

    @Test
    void removesUser() {
        final String alice = "Alice";
        this.blsto.save(
            UsersDeleteTest.KEY,
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.STRUCT,
                new YamlCredentialsTest.User(alice, Optional.of("alice@example.com"), "admin"),
                new YamlCredentialsTest.User("Bob", Optional.of("bob@example.com"))
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(com.artipie.front.api.Users.USER_PARAM.toString()))
            .thenReturn(alice);
        final Response resp = Mockito.mock(Response.class);
        final FakeCreds creds = new FakeCreds();
        new com.artipie.front.api.Users.Delete(this.users, creds).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.OK_200);
        MatcherAssert.assertThat(
            "One user was added",
            this.users.list().size(),
            new IsEqual<>(1)
        );
        MatcherAssert.assertThat(
            "Creds were reloaded",
            creds.wasReloaded(),
            new IsEqual<>(true)
        );
    }

    /**
     * Fake credentials for test.
     * @since 0.1
     */
    static class FakeCreds implements Credentials {

        /**
         * Reload operation counter.
         */
        private final AtomicInteger reloaded = new AtomicInteger(0);

        @Override
        public Optional<User> user(final String name) {
            return Optional.empty();
        }

        @Override
        public void reload() {
            this.reloaded.incrementAndGet();
        }

        /**
         * Were creds reloaded?
         * @return True if credentials were reloaded
         */
        public boolean wasReloaded() {
            return this.reloaded.get() == 1;
        }
    }

}
