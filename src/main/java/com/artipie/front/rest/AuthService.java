/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import java.util.Optional;
import javax.json.Json;

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
     * @param rest Artipie rest endpoint.
     */
    public AuthService(final String rest) {
        super(rest);
    }

    /**
     * Obtain JWT-token from auth rest-service.
     * @param name User name.
     * @param password User password.
     * @return JWT-token.
     */
    public String getJwtToken(final String name, final String password) {
        return BaseService.handle(
            this.httpPost(
                Optional.empty(),
                AuthService.TOKEN_PATH,
                () ->
                    Json.createObjectBuilder()
                        .add("name", name)
                        .add("pass", password)
                        .build().toString()
            ),
            res -> BaseService.jsonObject(res).getString("token")
        );
    }
}
