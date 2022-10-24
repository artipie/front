/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.rest;

import com.artipie.front.misc.Yaml2Json;
import com.artipie.front.settings.ArtipieEndpoint;
import io.vavr.Tuple3;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.json.JsonArray;
import javax.json.JsonValue;

/**
 * Repository-service.
 *
 * @since 1.0
 */
public class RepositoryService extends BaseService {
    /**
     * Path to 'repository'.
     */
    private static final String REPOSITORY_PATH = "/api/v1/repository";

    /**
     * Path to 'list' rest-api.
     */
    private static final String LIST_PATH = BaseService.path(
        RepositoryService.REPOSITORY_PATH, "list"
    );

    /**
     * Ctor.
     *
     * @param endpoint Artipie endpoint configuration.
     */
    public RepositoryService(final ArtipieEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * Obtain list of repository names.
     * @param token Token.
     * @return Tuple3 of 'Status code, List of repository names, Error message'
     */
    public Tuple3<Integer, List<String>, String> list(final String token) {
        return BaseService.handle(
            this.httpGet(Optional.of(token), RepositoryService.LIST_PATH),
            RepositoryService::listOfStrings
        );
    }

    /**
     * Obtain list of repository names by user's name.
     * @param token Token.
     * @param uname User name.
     * @return Tuple3 of 'Status code, List of repository names, Error message'
     */
    public Tuple3<Integer, List<String>, String> list(final String token, final String uname) {
        return BaseService.handle(
            this.httpGet(
                Optional.of(token),
                BaseService.path(RepositoryService.LIST_PATH, uname)
            ),
            RepositoryService::listOfStrings
        );
    }

    /**
     * Obtain repository content.
     * @param token Token.
     * @param rname Repository name.
     * @return Tuple3 of 'Status code, Repository content, Error message'
     */
    public Tuple3<Integer, String, String> repo(final String token, final RepositoryName rname) {
        return BaseService.handle(
            this.httpGet(
                Optional.of(token),
                BaseService.path(RepositoryService.REPOSITORY_PATH, rname)
            ),
            BaseService::toYaml
        );
    }

    /**
     * Save repository config.
     * @param token Token.
     * @param rname Repository name.
     * @param config Repository config.
     * @return Tuple3 of 'Status code, Resulting message, Error message'
     */
    public Tuple3<Integer, String, String> save(final String token, final RepositoryName rname,
        final String config) {
        return BaseService.handle(
            this.httpPut(
                Optional.of(token),
                BaseService.path(RepositoryService.REPOSITORY_PATH, rname),
                () -> new Yaml2Json().apply(config).toString()
            ),
            res -> String.format("Repository %s saved successfully", rname)
        );
    }

    /**
     * Remove repository.
     * @param token Token.
     * @param rname Repository name.
     * @return Tuple3 of 'Status code, Resulting message, Error message'
     */
    public Tuple3<Integer, String, String> remove(final String token, final RepositoryName rname) {
        return BaseService.handle(
            this.httpDelete(
                Optional.of(token),
                RepositoryService.path(RepositoryService.REPOSITORY_PATH, rname.toString())
            ),
            res -> String.format("Repository %s removed", rname)
        );
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
            result.add(BaseService.stripQuotes(item.toString()));
        }
        return result;
    }
}
