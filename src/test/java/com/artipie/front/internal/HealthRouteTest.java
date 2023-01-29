/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.internal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test case for {@link HealthRoute}.
 * @since 1.0
 */
public final class HealthRouteTest {

    @Test
    void returnsOK() throws Exception {
        final var req = Mockito.mock(Request.class);
        final var rsp = Mockito.mock(Response.class);
        MatcherAssert.assertThat(
            "returns OK", new HealthRoute().handle(req, rsp),
            Matchers.equalTo("OK")
        );
        // @checkstyle MagicNumberCheck (1 line)
        Mockito.verify(rsp).status(200);
    }
}
