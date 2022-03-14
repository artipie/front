/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.misc.RequestPath;
import com.artipie.front.settings.RepoPermissions;
import java.io.StringReader;
import java.util.Optional;
import javax.json.Json;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `PUT` request to add repository permissions.
 * @since 0.1
 */
public final class PutRepositoryPermissions implements Route {

    /**
     * Name of the user to add permissions for.
     */
    public static final RequestPath.Param NAME = new RequestPath.Param("uname");

    /**
     * Repository permissions.
     * @since 0.1
     */
    private final RepoPermissions perms;

    /**
     * Ctor.
     * @param perms Repository permissions
     */
    public PutRepositoryPermissions(final RepoPermissions perms) {
        this.perms = perms;
    }

    @Override
    public Object handle(final Request request, final Response response) {
        this.perms.add(
            Optional.ofNullable(GetUser.USER_PARAM.parse(request)).map(usr -> usr.concat("/"))
                .orElse("").concat(GetRepository.REPO_PARAM.parse(request)),
            PutRepositoryPermissions.NAME.parse(request),
            Json.createReader(new StringReader(request.body())).readArray()
        );
        response.status(HttpStatus.CREATED_201);
        return null;
    }
}
