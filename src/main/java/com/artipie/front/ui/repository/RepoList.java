/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.ui.HbPage;
import io.vavr.Tuple3;
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
    public RepoList(final RepositoryService repository, final String layout) {
        super(
            new HbPage(
                "repository/list",
                req -> {
                    final String uname = req.session().attribute("uname");
                    final String token = req.session().attribute("token");
                    final Tuple3<Integer, List<String>, String> result;
                    if ("flat".equals(layout)) {
                        result = repository.list(token);
                    } else {
                        result = repository.list(token, uname);
                    }
                    return Map.of(
                        "title", "Repository list",
                        "result", result,
                        "error", ""
                    );
                }
            )
        );
    }
}
