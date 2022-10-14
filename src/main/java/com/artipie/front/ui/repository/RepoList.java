/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * List of repositories page.
 *
 * @checkstyle AvoidDuplicateLiterals (500 lines)
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoList extends RouteWrap.TemplateViewRoute {
    /**
     * List of repositories page.
     *
     * @param repository Repository service.
     */
    public RepoList(final RepositoryService repository) {
        super(
            new HbPage(
                "repository/list",
                req ->
                    Map.of(
                        "title", "Repository list",
                        "repos", repository.list(req.session().attribute("token"))
                    )
            )
        );
    }
}
