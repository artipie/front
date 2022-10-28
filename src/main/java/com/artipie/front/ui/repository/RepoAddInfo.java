/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * Add repository info view page.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoAddInfo extends RouteWrap.TemplateViewRoute {
    /**
     * Add repository info page.
     */
    public RepoAddInfo() {
        super(
            new HbPage(
                "repository/add_info",
                req -> Map.of(
                    "title", "Add repository"
                )
            )
        );
    }
}
