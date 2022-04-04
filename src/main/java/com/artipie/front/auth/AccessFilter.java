/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.artipie.front.RequestAttr;
import org.eclipse.jetty.http.HttpStatus;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Access HTTP filter.
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class AccessFilter implements Filter {

    /**
     * Access permissions.
     */
    private final AccessPermissions access;

    /**
     * Permissions.
     */
    private final UserPermissions perms;

    /**
     * Access filter.
     * @param access Access permissions
     * @param perms Permissions
     */
    public AccessFilter(final AccessPermissions access, final UserPermissions perms) {
        this.access = access;
        this.perms = perms;
    }

    @Override
    public void handle(final Request req, final Response rsp) throws Exception {
        final var uid = RequestAttr.Standard.USER_ID.read(req);
        if (uid.isEmpty()) {
            Spark.halt(HttpStatus.UNAUTHORIZED_401, "Authentication required");
        }
        final boolean allowed = this.access.get(req.requestMethod(), req.pathInfo())
            .stream().anyMatch(perm -> this.perms.allowed(uid.get(), perm));
        if (!allowed) {
            Spark.halt(HttpStatus.FORBIDDEN_403, "Request is not allowed");
        }
    }
}
