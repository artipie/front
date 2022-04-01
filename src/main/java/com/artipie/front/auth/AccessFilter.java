/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.artipie.front.RequestAttr;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.http.HttpStatus;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import wtf.g4s8.tuples.Pair;

/**
 * Access HTTP filter.
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class AccessFilter implements Filter {

    /**
     * Permissions allowed to handle request.
     */
    private static final Map<Pair<String, String>, Collection<String>> PERM_REQ = Map.of(
        Pair.of("GET", "/repositories.*"), List.of("repo-read"),
        Pair.of("HEAD", "/repositories.*"), List.of("repo-read"),
        Pair.of("PUT", "/repositories.*"), List.of("repo-write"),
        Pair.of("DELETE", "/repositories.*"), List.of("repo-write")
    );

    /**
     * Permissions.
     */
    private final UserPermissions perms;

    /**
     * Access filter.
     * @param perms Permissions
     */
    public AccessFilter(final UserPermissions perms) {
        this.perms = perms;
    }

    @Override
    public void handle(final Request req, final Response rsp) throws Exception {
        final var uid = RequestAttr.Standard.USER_ID.read(req);
        if (uid.isEmpty()) {
            Spark.halt(HttpStatus.UNAUTHORIZED_401, "Authentication required");
        }
        final boolean allowed = AccessFilter.PERM_REQ.getOrDefault(
            Pair.of(req.requestMethod(), req.pathInfo()), Collections.emptyList()
        ).stream().anyMatch(perm -> this.perms.allowed(uid.get(), perm));
        if (!allowed) {
            Spark.halt(HttpStatus.FORBIDDEN_403, "Request is not allowed");
        }
    }
}
