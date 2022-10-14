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
import java.net.http.HttpResponse;
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
        final HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                .build()
                .send(
                    HttpRequest.newBuilder()
                        .uri(uri(RepositoryService.LIST_PATH))
                        .GET()
                        .header(BaseService.AUTHORIZATION, bearer(token))
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException | URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
        checkStatus(BaseService.SUCCESS, response);
        try {
            return List.of(this.mapper().readValue(response.body(), String[].class));
        } catch (final JsonProcessingException exc) {
            throw new ArtipieException(exc);
        }
    }

    /**
     * Obtains list of repository names by user's name.
     * @param token Token.
     * @param uname User name.
     * @return List of repository names.
     */
    public List<String> list(final String token, final String uname) {
        final HttpResponse<String> response;
        try {
            response = HttpClient.newBuilder()
                .build()
                .send(
                    HttpRequest.newBuilder()
                        .uri(uri(String.format("%s/%s", RepositoryService.LIST_PATH, uname)))
                        .GET()
                        .header(BaseService.AUTHORIZATION, bearer(token))
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                );
        } catch (final IOException | InterruptedException | URISyntaxException exc) {
            throw new ArtipieException(exc);
        }
        checkStatus(BaseService.SUCCESS, response);
        try {
            return List.of(this.mapper().readValue(response.body(), String[].class));
        } catch (final JsonProcessingException exc) {
            throw new ArtipieException(exc);
        }
    }
}
