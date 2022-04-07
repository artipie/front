/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import java.util.Map;
import org.eclipse.jetty.http.HttpStatus;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Auth Spark filters.
 * @since 1.0
 * @checkstyle IndentationCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public enum AuthFilters implements Filter {
    /**
     * Authenticate user if session is not valid.
     */
    AUTHENTICATE(
        (req, rsp) -> {
            if (req.pathInfo().equals("/signin") || req.pathInfo().equals("/token")) {
                return;
            }
            if (req.session() == null || !req.session().attributes().contains("uid")) {
                rsp.redirect("/signin");
                Spark.halt(HttpStatus.UNAUTHORIZED_401);
            }
        }
    ),

    /**
     * Add session attributes.
     */
    SESSION_ATTRS(
        (req, rsp) -> {
            final var attrs = Map.of(
                "uid", RequestAttr.Standard.USER_ID
            );
            if (req.session() == null) {
                attrs.values().forEach(attr -> attr.remove(req));
            }
            attrs.forEach(
                (name, attr) -> {
                    if (req.session().attributes().contains(name)) {
                        attr.write(req, req.session().attribute(name));
                    } else {
                        attr.remove(req);
                    }
                }
            );
        }
    );

    /**
     * Filter function.
     */
    private final Filter func;

    /**
     * Private enum ctor.
     * @param func Filter function
     */
    AuthFilters(final Filter func) {
        this.func = func;
    }

    @Override
    public void handle(final Request req, final Response rsp) throws Exception {
        if (!req.pathInfo().startsWith("/api")) {
            this.func.handle(req, rsp);
        }
    }
}
