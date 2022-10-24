/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.settings.ArtipieEndpoint;
import io.vavr.Tuple3;
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
     * @param endpoint Artipie endpoint configuration.
     */
    public AuthService(final ArtipieEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * Obtain JWT-token from auth rest-service.
     * @param name User name.
     * @param password User password.
     * @return Tuple3 of 'Status code, JWT-token, Error message'.
     */
    public Tuple3<Integer, String, String> getJwtToken(final String name, final String password) {
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
