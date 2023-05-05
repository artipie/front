/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * Create repository info view page.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoCreate extends RouteWrap.TemplateViewRoute {
    /**
     * Add repository info page.
     */
    public RepoCreate() {
        super(
            new HbPage(
                "repository/create",
                req -> Map.of(
                    "title", "Create repository",
                    "uid", req.session().attribute("uid")
                )
            )
        );
    }
}
