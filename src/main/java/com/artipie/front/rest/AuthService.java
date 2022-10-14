/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.ArtipieException;
import com.artipie.front.settings.ArtipieEndpoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

/**
 * Auth-service.
 *
 * @since 1.0
 */
public class AuthService extends BaseService {
    /**
     * Path to rest-api.
     */
    private static final String TOKEN_PATH = "/api/v1/oauth/token";

    /**
     * Ctor.
     * @param endpoint Artipie endpoint configuration.
     */
    public AuthService(final ArtipieEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * Obtains JWT-token from auth rest-service.
     *
     * @param user User.
     * @return JWT-token.
     */
    public String getJwtToken(final AuthUser user) {
        final java.net.http.HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                .build()
                .send(
                    HttpRequest.newBuilder()
                        .uri(uri(AuthService.TOKEN_PATH))
                        .POST(
                            HttpRequest.BodyPublishers.ofString(
                                this.mapper().writeValueAsString(user)
                            )
                        )
                        .build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException | URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
        // @checkstyle MagicNumberCheck (1 line)
        if (response.statusCode() != 200) {
            throw new ArtipieException(
                String.format("Expected 200 result code, but received %s", response.statusCode())
            );
        }
        try {
            return this.mapper().readValue(response.body(), Token.class).getToken();
        } catch (final JsonProcessingException exp) {
            throw new ArtipieException(exp);
        }
    }

    /**
     * Auth-user.
     *
     * @since 1.0
     */
    public static class AuthUser {
        /**
         * Name.
         */
        private String name;

        /**
         * Password.
         */
        private String pass;

        /**
         * Gets name.
         *
         * @return Name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Gets password.
         *
         * @return Password.
         */
        public String getPass() {
            return this.pass;
        }

        /**
         * Sets name.
         *
         * @param name Name.
         * @return This.
         * @checkstyle HiddenFieldCheck (3 lines)
         */
        public AuthUser setName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets password.
         *
         * @param pass Password.
         * @return This.
         * @checkstyle HiddenFieldCheck (3 lines)
         */
        public AuthUser setPass(final String pass) {
            this.pass = pass;
            return this;
        }
    }

    /**
     * Auth-token rest-service api.
     *
     * @since 1.0
     */
    public static class Token {
        /**
         * JWT-token.
         */
        @SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
        private String token;

        /**
         * Gets token.
         *
         * @return Token.
         */
        public String getToken() {
            return this.token;
        }

        /**
         * Sets token.
         *
         * @param token Token.
         * @checkstyle HiddenFieldCheck (3 lines)
         */
        public void setToken(final String token) {
            this.token = token;
        }
    }
}
