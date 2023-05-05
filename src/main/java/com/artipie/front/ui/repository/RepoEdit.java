/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryName;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.ui.HbPage;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.io.RuntimeIOException;

/**
 * Repository editor page.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoEdit extends RouteWrap.TemplateViewRoute {
    /**
     * Repository editor page.
     *
     * @param repository Repository service.
     * @param info Repository info templates.
     */
    public RepoEdit(final RepositoryService repository, final RepositoryInfo info) {
        super(
            new HbPage(
                "repository/edit",
                req -> {
                    final RepositoryName rname = new RepositoryName.FromRequest(req);
                    final String repo = req.params(":repo");
                    final String uid = req.session().attribute("uid");
                    final String conf = repository.repo(
                        req.session().attribute("token"), rname
                    );
                    return Map.of(
                        "title", String.format("Repository %s", rname),
                        "rname", rname,
                        "conf", conf,
                        "info", RepoEdit.repoType(conf)
                            .map(
                                type -> {
                                    String rendered;
                                    try {
                                        rendered = info.render(
                                            type,
                                            Map.of(
                                                "user", uid,
                                                "repo", repo,
                                                "type", type
                                            )
                                        );
                                    } catch (final RuntimeIOException exc) {
                                        rendered = "";
                                    }
                                    return rendered;
                                }
                            ).orElse("")
                    );
                }
            )
        );
    }

    /**
     * Provides repository type from yaml-configuration.
     * @param repoconf Yaml-content of repository configuration.
     * @return Repository type.
     */
    private static Optional<String> repoType(final String repoconf) {
        return Optional.ofNullable(repoconf)
            .flatMap(
                content -> {
                    Optional<YamlMapping> yaml;
                    try {
                        yaml = Optional.of(Yaml.createYamlInput(content).readYamlMapping());
                    } catch (final IOException exc) {
                        yaml = Optional.empty();
                    }
                    return yaml;
                }
            )
            .map(yaml -> yaml.yamlMapping("repo"))
            .map(repo -> repo.string("type"));
    }
}
