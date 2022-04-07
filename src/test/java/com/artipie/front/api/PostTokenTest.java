/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.auth.ApiTokens;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link PostToken}.
 * @since 0.1
 * @checkstyle MagicNumberCheck (500 lines)
 */
class PostTokenTest {

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
    void createsToken() {
        final var rqs = Mockito.mock(Request.class);
        final var rsp = Mockito.mock(Response.class);
        Mockito.when(rqs.body()).thenReturn("{ \"name\":\"Alice\", \"pass\":\"123\" }");
        MatcherAssert.assertThat(
            new PostToken((usr, pswd) -> Optional.of("Alice"), this.tokens).handle(rqs, rsp),
            new StringContains("token")
        );
        Mockito.verify(rsp).status(HttpStatus.CREATED_201);
    }

    @Test
    void returnsError() {
        final var rqs = Mockito.mock(Request.class);
        final var rsp = Mockito.mock(Response.class);
        Mockito.when(rqs.body()).thenReturn("{ \"name\":\"Mark\", \"pass\":\"123\" }");
        MatcherAssert.assertThat(
            new PostToken((usr, pswd) -> Optional.empty(), this.tokens).handle(rqs, rsp),
            new StringContains("err")
        );
        Mockito.verify(rsp).status(HttpStatus.UNAUTHORIZED_401);
    }

}
