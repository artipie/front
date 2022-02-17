/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.api.ApiAuthFilter.ValidationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.HaltException;
import spark.Request;
import spark.Response;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Test case for {@link ApiAuthFilter}.
 * @since 1.0
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class ApiAuthFilterTest {

    @Test
    void authenticate() throws Exception {
        final var token = "123";
        final var uid = "alice";
        final var req = Mockito.mock(Request.class);
        final var rsp = Mockito.mock(Response.class);
        mockHeader(req, token);
        new ApiAuthFilter(new FakeTokenValidator(token, uid, Instant.MAX)).handle(req, rsp);
        Mockito.verify(req).attribute("uid", uid);
    }

    @Test
    void expiredToken() throws Exception {
        final var req = Mockito.mock(Request.class);
        final var rsp = Mockito.mock(Response.class);
        final var token = "456";
        mockHeader(req, token);
        final var err = Assertions.assertThrows(
            HaltException.class,
            () -> new ApiAuthFilter(
                new FakeTokenValidator(token, "any", Instant.now().minus(1, ChronoUnit.DAYS))
            ).handle(req, rsp)
        );
        MatcherAssert.assertThat(
            "status",
            err.statusCode(), Matchers.equalTo(401)
        );
        MatcherAssert.assertThat(
            "body",
            err.body(),
            new StringIsJson.Object(new JsonHas("error", "token expired"))
        );
    }

    @Test
    void invalidToken() throws Exception {
        final var req = Mockito.mock(Request.class);
        final var rsp = Mockito.mock(Response.class);
        mockHeader(req, "fghj");
        final var err = Assertions.assertThrows(
            HaltException.class,
            () -> new ApiAuthFilter(
                new FakeTokenValidator("876", "bob", Instant.now())
            ).handle(req, rsp)
        );
        MatcherAssert.assertThat(
            "status",
            err.statusCode(), Matchers.equalTo(401)
        );
        MatcherAssert.assertThat(
            "body",
            err.body(),
            new StringIsJson.Object(new JsonHas("error", "invalid token"))
        );
    }

    /**
     * Mock request header.
     * @param req Request
     * @param token Token
     */
    private static void mockHeader(final Request req, final String token) {
        Mockito.when(req.headers("Authorization")).thenReturn(token);
    }

    /**
     * New fake validator for tests: it checks if token is equal to expected token,
     * that time provided is not after than expire time, then return user ID.
     * @since 1.0
     */
    private static final class FakeTokenValidator implements ApiAuthFilter.TokenValidator {

        /**
         * Expected token.
         */
        private final String token;

        /**
         * User ID to return.
         */
        private final String user;

        /**
         * Token expire time.
         */
        private final Instant expire;

        /**
         * New fake validator.
         * @param token Expected token
         * @param user User ID to return
         * @param expire Token expire time
         */
        FakeTokenValidator(final String token, final String user, final Instant expire) {
            this.token = token;
            this.user = user;
            this.expire = expire;
        }

        @Override
        public String validate(final String tkn, final Instant time) throws ValidationException {
            if (!tkn.equals(this.token)) {
                throw new ValidationException("invalid token");
            }
            if (time.isAfter(this.expire)) {
                throw new ValidationException("token expired");
            }
            return this.user;
        }
    }
}
