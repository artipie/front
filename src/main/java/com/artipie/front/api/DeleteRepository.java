/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.front.RequestAttr;
import com.artipie.front.settings.RepoSettings;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handle `DELETE` request to delete repository settings.
 * @since 0.1
 */
public final class DeleteRepository implements Route {

    /**
     * Repository settings.
     */
    private final RepoSettings stn;

    /**
     * Ctor.
     * @param stn Repository settings
     */
    public DeleteRepository(final RepoSettings stn) {
        this.stn = stn;
    }

    @Override
    public Object handle(final Request request, final Response response) {
        this.stn.delete(
            request.params(GetRepository.PARAM),
            RequestAttr.Standard.USER_ID.readOrThrow(request)
        );
        return null;
    }
}
