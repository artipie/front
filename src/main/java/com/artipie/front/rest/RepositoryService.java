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
import java.util.List;

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
        final java.net.http.HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                .build()
                .send(
                    HttpRequest.newBuilder()
                        .uri(uri(RepositoryService.LIST_PATH))
                        .GET()
                        .header("Authorization", String.format("Bearer %s", token))
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
            return List.of(this.mapper().readValue(response.body(), String[].class));
        } catch (final JsonProcessingException exc) {
            throw new ArtipieException(exc);
        }
    }
}
