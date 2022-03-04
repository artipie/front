/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ApiTokens}.
 * @since 1.0
 * @checkstyle LineLengthCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
public final class ApiTokensTest {

    /**
     * Tokens instance.
     */
    private ApiTokens tokens;

    @BeforeEach
    void setUp() {
        final byte[] key = new byte[20];
        Arrays.fill(key, (byte) 0);
        this.tokens = new ApiTokens(key, new Random(0));
    }

    @Test
    void generateToken() {
        MatcherAssert.assertThat(
            this.tokens.token("bob", Instant.ofEpochMilli(0)),
            Matchers.equalTo("03626f620000000060b420bbbb0c134e1f5ee861e418d4f7a9eba9176386c466")
        );
    }

    @Test
    void validateToken() {
        MatcherAssert.assertThat(
            this.tokens.validate("05616c6963650000000060b420bbca0534fa24ccfecf8f540debda4ed932025e518e"),
            Matchers.equalTo(true)
        );
    }

    @Test
    void rejectToken() {
        MatcherAssert.assertThat(
            this.tokens.validate("05616c6963650000000060b420bbca0534fa24ccfecf8f540debda4ed932025e5180"),
            Matchers.equalTo(false)
        );
    }

    @Test
    void parseName() {
        final var token = ApiTokens.Token.parse("05616c6963650000000060b420bbca0534fa24ccfecf8f540debda4ed932025e519e");
        MatcherAssert.assertThat(token.user(), Matchers.equalTo("alice"));
    }

    @Test
    void parseExpired() {
        final var token = ApiTokens.Token.parse("05616c6963650000000560b420bb5aa989410d88e43e397b3d6d7aca2f6d43f32c8a");
        MatcherAssert.assertThat(token.expired(Instant.ofEpochMilli(1)), Matchers.equalTo(false));
    }
}
