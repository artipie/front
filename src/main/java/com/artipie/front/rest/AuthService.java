/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.settings.ArtipieEndpoint;
import java.net.http.HttpResponse;
import javax.json.Json;
import javax.servlet.http.HttpServletResponse;

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
        final HttpResponse<String> response  = this.httpPost(
            AuthService.TOKEN_PATH,
            () ->
                Json.createObjectBuilder()
                    .add("name", user.name())
                    .add("pass", user.pass())
                    .build().toString()
        );
        checkStatus(HttpServletResponse.SC_OK, response);
        return BaseService.jsonObject(response).getString("token");
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
        @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
        private final String name;

        /**
         * Password.
         */
        @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
        private final String pass;

        /**
         * Ctor.
         * @param name User name.
         * @param pass User password.
         */
        public AuthUser(final String name, final String pass) {
            this.name = name;
            this.pass = pass;
        }

        /**
         * Gets name.
         *
         * @return Name
         */
        public String name() {
            return this.name;
        }

        /**
         * Gets password.
         *
         * @return Password.
         */
        public String pass() {
            return this.pass;
        }
    }
}
