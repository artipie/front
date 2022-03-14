/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.settings.YamlStorages;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain storages list.
 * @since 0.1
 */
public final class GetStorages implements Route {

    /**
     * Artipie storage.
     */
    private final BlockingStorage strgs;

    /**
     * Ctor.
     * @param strgs Artipie storages
     */
    public GetStorages(final BlockingStorage strgs) {
        this.strgs = strgs;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObjectBuilder res = Json.createObjectBuilder();
        final Optional<String> usr = Optional.ofNullable(GetUser.USER_PARAM.parse(request));
        Optional<Key> repo = Optional.ofNullable(GetRepository.REPO_PARAM.parse(request))
            .map(Key.From::new);
        if (usr.isPresent()) {
            repo = repo.map(key -> new Key.From(usr.map(Key.From::new).get(), key));
        }
        new YamlStorages(repo, this.strgs).list()
            .forEach(item -> res.add(item.alias(), item.info()));
        response.type(MimeTypes.Type.APPLICATION_JSON.asString());
        return Json.createObjectBuilder().add("storages", res.build()).build().toString();
    }
}
