/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.util.Collections;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link EnvCredentials}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class EnvCredentialsTest {

    @Test
    void findsUser() {
        final String alice = "Alice";
        MatcherAssert.assertThat(
            new EnvCredentials(
                Map.of("ARTIPIE_USER_NAME", alice, "ARTIPIE_USER_PASS", "any")
            ).user(alice).get().uid(),
            new IsEqual<>(alice)
        );
    }

    @Test
    void throwsErrWhenPasswordIsAbsent() {
        final String bob = "Bob";
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new EnvCredentials(Map.of("ARTIPIE_USER_NAME", bob)).user(bob)
        );
    }

    @Test
    void doesNotFindUserWhenMapIsEmpty() {
        MatcherAssert.assertThat(
            new EnvCredentials(Collections.emptyMap()).user("Artipie").isEmpty(),
            new IsEqual<>(true)
        );
    }
}
