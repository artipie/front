/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.Layout;
import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.ui.HbPage;
import java.util.List;
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
     * @param layout Layout.
     */
    public RepoList(final RepositoryService repository, final Layout layout) {
        super(
            new HbPage(
                "repository/list",
                req -> {
                    final String uid = req.session().attribute("uid");
                    final String token = req.session().attribute("token");
                    final List<String> repos;
                    if (layout == Layout.FLAT) {
                        repos = repository.list(token);
                    } else {
                        repos = repository.list(token, uid);
                    }
                    return Map.of(
                        "title", "Repository list",
                        "repos", repos
                    );
                }
            )
        );
    }
}
