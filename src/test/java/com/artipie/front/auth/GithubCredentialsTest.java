/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.artipie.ArtipieException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link GithubCredentials}.
 * @since 0.1
 */
class GithubCredentialsTest {

    @Test
    void returnsEmptyUserWhenFormatIsNotGithub() {
        MatcherAssert.assertThat(
            new GithubCredentials().user("any user").isEmpty(),
            new IsEqual<>(true)
        );
    }

    @Test
    void doesNotValidateWrongUser() {
        MatcherAssert.assertThat(
            new GithubCredentials(
                token -> "Alice"
            ).user("github.com/John").orElseThrow().validatePassword("pass"),
            new IsEqual<>(false)
        );
    }

    @Test
    void resolveUserByToken() {
        final String secret = "secret";
        MatcherAssert.assertThat(
            new GithubCredentials(
                // @checkstyle ReturnCountCheck (5 lines)
                token -> {
                    if (token.equals(secret)) {
                        return "User";
                    }
                    return "";
                }
            ).user("github.com/UsEr").orElseThrow().validatePassword(secret),
            new IsEqual<>(true)
        );
    }

    @Test
    void shouldReturnOptionalEmptyWhenRequestIsUnauthorized() {
        MatcherAssert.assertThat(
            new GithubCredentials(
                token -> {
                    throw new AssertionError(
                        String.join(
                            "\n", "HTTP response status is not equal to 200:",
                            "401 Unauthorized [https://api.github.com/user]"
                        )
                    );
                }
            ).user("github.com/bad_user").orElseThrow().validatePassword("incorrect"),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldThrownExceptionWhenAssertionErrorIsHappened() {
        Assertions.assertThrows(
            ArtipieException.class,
            () -> new GithubCredentials(
                token -> {
                    throw new AssertionError("Any error");
                }
            ).user("github.com/user").orElseThrow().validatePassword("any")
        );
    }
}
