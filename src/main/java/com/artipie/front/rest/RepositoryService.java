/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.settings.ArtipieEndpoint;
import io.vertx.core.http.HttpMethod;
import java.util.List;
import java.util.Optional;

/**
 * Repository-service.
 *
 * @since 1.0
 */
public class RepositoryService extends BaseService {
    /**
     * Path to 'list' rest-api.
     */
    private static final String LIST_PATH = "/api/v1/repository/list";

    /**
     * Ctor.
     *
     * @param endpoint Artipie endpoint configuration.
     */
    public RepositoryService(final ArtipieEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * Obtains list of repository names.
     * @param token Token.
     * @return List of repository names.
     */
    public List<String> list(final String token) {
        return this.sendSync(
            HttpMethod.GET, RepositoryService.LIST_PATH, Optional.empty(),
            Optional.ofNullable(token)
        )
        .bodyAsJsonArray()
        .stream()
        .map(Object::toString)
        .toList();
    }
}
