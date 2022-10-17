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
     * @param name User name.
     * @param password User password.
     * @return JWT-token.
     */
    public String getJwtToken(final String name, final String password) {
        final HttpResponse<String> response  = this.httpPost(
            AuthService.TOKEN_PATH,
            () ->
                Json.createObjectBuilder()
                    .add("name", name)
                    .add("pass", password)
                    .build().toString()
        );
        checkStatus(HttpServletResponse.SC_OK, response);
        return BaseService.jsonObject(response).getString("token");
    }
}
