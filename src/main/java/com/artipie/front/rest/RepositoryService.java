/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.settings.ArtipieEndpoint;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletResponse;

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
        final HttpResponse<String> response = this.httpGet(token, RepositoryService.LIST_PATH);
        checkStatus(HttpServletResponse.SC_OK, response);
        return RepositoryService.listOfStrings(response);
    }

    /**
     * Obtains list of repository names by user's name.
     * @param token Token.
     * @param uname User name.
     * @return List of repository names.
     */
    public List<String> list(final String token, final String uname) {
        final HttpResponse<String> response  =
            this.httpGet(token, String.format("%s/%s", RepositoryService.LIST_PATH, uname));
        checkStatus(HttpServletResponse.SC_OK, response);
        return RepositoryService.listOfStrings(response);
    }

    /**
     * Reads response body and convert it to List of strings.
     * Expects json-body as array of strings.
     * @param res Response with json-body.
     * @return List of string.
     */
    private static List<String> listOfStrings(final HttpResponse<String> res) {
        final JsonArray array = BaseService.jsonArray(res);
        final List<String> result = new ArrayList<>(array.size());
        for (final JsonValue item : array) {
            result.add(item.toString());
        }
        return result;
    }
}
