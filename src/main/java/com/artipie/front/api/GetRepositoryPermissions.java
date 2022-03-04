/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import com.artipie.front.misc.Yaml2Json;
import com.artipie.front.settings.RepoSettings;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `GET` request to obtain repository permissions.
 * @since 0.1
 */
public final class GetRepositoryPermissions implements Route {

    /**
     * Repository settings section `permissions` name.
     */
    private static final String PERMISSIONS = "permissions";

    /**
     * Repository settings.
     */
    private final RepoSettings stn;

    /**
     * Ctor.
     * @param stn Repository settings
     */
    public GetRepositoryPermissions(final RepoSettings stn) {
        this.stn = stn;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObject perms = new Yaml2Json().apply(
            new String(
                this.stn.value(
                    request.params(GetRepository.PARAM),
                    RequestAttr.Standard.USER_ID.readOrThrow(request)
                ),
                StandardCharsets.UTF_8
            )
        ).asJsonObject().getJsonObject("repo").getJsonObject(GetRepositoryPermissions.PERMISSIONS);
        return Json.createObjectBuilder().add(
            GetRepositoryPermissions.PERMISSIONS,
            Optional.ofNullable(perms).orElse(Json.createObjectBuilder().build())
        ).build().toString();
    }
}
