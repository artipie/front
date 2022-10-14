/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.settings.ArtipieEndpoint;
import java.net.http.HttpResponse;

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
        final HttpResponse<String> response  = this.httpPost(AuthService.TOKEN_PATH, user);
        checkStatus(BaseService.SUCCESS, response);
        return jsonToObject(response, Token.class).getToken();
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
