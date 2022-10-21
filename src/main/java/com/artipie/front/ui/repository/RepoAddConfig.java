/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryName;
import com.artipie.front.ui.HbPage;
import java.util.Map;

/**
 * Add repository config view page.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoAddConfig extends RouteWrap.TemplateViewRoute {
    /**
     * Add repository info page.
     * @param layout Layout
     * @param info Infor template
     * @param template Repository template
     */
    public RepoAddConfig(final String layout, final RepositoryInfo info,
        final RepositoryTemplate template) {
        super(
            new HbPage(
                "repository/add_config",
                req -> {
                    final String name = req.queryParams("name");
                    final String type = req.queryParams("type");
                    final String user = req.session().attribute("uname");
                    final String rname;
                    if ("flat".equals(layout)) {
                        rname = new RepositoryName.Flat(name).toString();
                    } else {
                        rname = new RepositoryName.Org(name, user).toString();
                    }
                    return Map.of(
                        "title", "Add repository",
                        "uname", user,
                        "rname", rname,
                        "type", type,
                        "info", info.render(
                            type,
                            Map.of(
                                "user", user,
                                "repo", name,
                                "type", type
                            )
                        ),
                        "template", template.render(
                            type,
                            Map.of(
                                "user", user,
                                "repo", name,
                                "type", type
                            )
                        )
                    );
                }
            )
        );
    }
}
