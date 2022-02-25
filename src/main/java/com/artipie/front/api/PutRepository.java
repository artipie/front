/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import com.artipie.front.misc.Json2Yaml;
import com.artipie.front.settings.RepoSettings;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `PUT` request to create repository.
 * @since 0.1
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class PutRepository implements Route {

    /**
     * Repository settings.
     */
    private final RepoSettings stn;

    /**
     * Ctor.
     * @param stn Repository settings
     */
    public PutRepository(final RepoSettings stn) {
        this.stn = stn;
    }

    @Override
    @SuppressWarnings("PMD.OnlyOneReturn")
    public Object handle(final Request request, final Response response) {
        final String param = request.params(GetRepository.PARAM);
        final String uid = RequestAttr.Standard.USER_ID.readOrThrow(request);
        if (this.stn.exists(param, uid)) {
            response.status(HttpStatus.CONFLICT_409);
            return String.format("Repository %s already exists", param);
        }
        final JsonObject body = Json.createReader(new StringReader(request.body())).readObject();
        final JsonObject repo = body.getJsonObject("repo");
        if (repo == null) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return "Section `repo` is required";
        }
        if (!repo.containsKey("type")) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return "Repository type is required";
        }
        if (!repo.containsKey("storage")) {
            response.status(HttpStatus.BAD_REQUEST_400);
            return "Repository storage is required";
        }
        this.stn.save(
            param, uid,
            new Json2Yaml().apply(body.toString()).toString().getBytes(StandardCharsets.UTF_8)
        );
        response.status(HttpStatus.CREATED_201);
        return null;
    }
}
