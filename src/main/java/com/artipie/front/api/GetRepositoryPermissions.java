/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.settings.RepoPermissions;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import org.eclipse.jetty.http.MimeTypes;
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
    private final RepoPermissions stn;

    /**
     * Ctor.
     * @param stn Repository settings
     */
    public GetRepositoryPermissions(final RepoPermissions stn) {
        this.stn = stn;
    }

    @Override
    public String handle(final Request request, final Response response) {
        final JsonObject res = this.stn.get(
            Optional.ofNullable(GetUser.USER_PARAM.parse(request)).map(usr -> usr.concat("/"))
                .orElse("").concat(GetRepository.NAME_PARAM.parse(request))
        );
        response.type(MimeTypes.Type.APPLICATION_JSON.toString());
        return Json.createObjectBuilder().add(GetRepositoryPermissions.PERMISSIONS, res)
            .build().toString();
    }
}
