/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import com.artipie.front.misc.RepoSettings;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `HEAD` request to check if repository exists.
 * @since 0.1
 */
public final class HeadRepository implements Route {

    /**
     * Repositories settings.
     */
    private final RepoSettings stn;

    /**
     * Ctor.
     * @param stn Repositories settings
     */
    public HeadRepository(final RepoSettings stn) {
        this.stn = stn;
    }

    @Override
    public String handle(final Request request, final Response response) {
        this.stn.key(
            request.params(GetRepository.PARAM),
            RequestAttr.Standard.USER_ID.readOrThrow(request)
        );
        return "OK";
    }
}
