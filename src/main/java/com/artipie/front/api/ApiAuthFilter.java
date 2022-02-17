/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import java.time.Instant;
import java.util.Optional;
import javax.json.Json;
import org.eclipse.jetty.http.HttpStatus;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Spark filter for API authentication.
 * @since 1.0
 */
public final class ApiAuthFilter implements Filter {

    /**
     * Auth token validator.
     */
    private final TokenValidator validator;

    /**
     * New API auth filter.
     * @param validator Token validator
     */
    public ApiAuthFilter(final TokenValidator validator) {
        this.validator = validator;
    }

    @Override
    public void handle(final Request request, final Response response) throws Exception {
        try {
            final var uid = this.validator.validate(
                Optional.of(request.headers("Authorization")).orElse(""),
                Instant.now()
            );
            RequestAttr.Standard.USER_ID.write(request, uid);
        } catch (final ValidationException vex) {
            Spark.halt(
                HttpStatus.UNAUTHORIZED_401,
                Json.createObjectBuilder()
                    .add("error", vex.getMessage())
                    .build().toString()
            );
        }
    }

    /**
     * Token validator API.
     * @since 1.0
     */
    @FunctionalInterface
    public interface TokenValidator {

        /**
         * Validate auth token.
         * @param token Token data
         * @param time Current time
         * @return User ID
         * @throws ValidationException If token is not valid
         */
        String validate(String token, Instant time) throws ValidationException;
    }

    /**
     * Token validation exception.
     * @since 1.0
     */
    public static final class ValidationException extends Exception {

        public static final long serialVersionUID = 0L;

        /**
         * New exception.
         * @param details Validation details
         */
        public ValidationException(final String details) {
            super(details);
        }
    }
}
