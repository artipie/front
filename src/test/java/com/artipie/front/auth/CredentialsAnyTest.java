/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Map;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Credentials.Any}.
 * @since 0.2
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CredentialsAnyTest {

    @Test
    void findsUser() {
        final String alice = "Alice";
        MatcherAssert.assertThat(
            new Credentials.Any(
                new FakeCreds(),
                new FakeCreds(),
                new EnvCredentials(Map.of("ARTIPIE_USER_NAME", alice, "ARTIPIE_USER_PASS", "any")),
                new FakeCreds(),
                new EnvCredentials(Map.of("ARTIPIE_USER_NAME", "Bob", "ARTIPIE_USER_PASS", "any"))
            ).user(alice).get().uid(),
            new IsEqual<>(alice)
        );
    }

    @Test
    void returnsEmptyIfNotFound() {
        MatcherAssert.assertThat(
            new Credentials.Any(
                new EnvCredentials(Map.of("ARTIPIE_USER_NAME", "Jane", "ARTIPIE_USER_PASS", "any")),
                new FakeCreds(),
                new EnvCredentials(Map.of("ARTIPIE_USER_NAME", "Alex", "ARTIPIE_USER_PASS", "any"))
            ).user("Artipie").isEmpty(),
            new IsEqual<>(true)
        );
    }

    /**
     * Fake credentials for test.
     * @since 0.1
     */
    private static class FakeCreds implements Credentials {

        @Override
        public Optional<User> user(final  String name) {
            return Optional.empty();
        }

        @Override
        public void reload() {
            //does nothing
        }
    }

}
