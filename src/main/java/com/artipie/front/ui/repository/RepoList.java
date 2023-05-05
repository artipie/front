/*
 * The MIT License (MIT) Copyright (c) 2022-2023 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui.repository;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.front.misc.RouteWrap;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.rest.SettingsService;
import com.artipie.front.ui.HbPage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * @param settings Settings service.
     */
    public RepoList(final RepositoryService repository, final SettingsService settings) {
        super(
            new HbPage(
                "repository/list",
                req -> {
                    final String token = req.session().attribute("token");
                    final List<String> names = repository.list(token);
                    final List<Repo> repos = new ArrayList<>(names.size());
                    names.stream().sorted().forEach(
                        name ->
                            repos.add(
                                new Repo(
                                    Integer.toString(settings.port()),
                                    name,
                                    repository.repo(token, name)
                                )
                            )
                    );
                    return Map.of(
                        "title", "Repository list",
                        "repos", repos
                    );
                }
            )
        );
    }

    /**
     * Repository information.
     * @since 0.1.3
     */
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static class Repo {
        /**
         * Repository name.
         */
        private final String name;

        /**
         * Artipie server port.
         */
        private final String port;

        /**
         * Repository configuration.
         */
        private Optional<YamlMapping> conf;

        /**
         * Ctor.
         * @param port Artipie's default port
         * @param name Name of repository.
         * @param conf Repository configuration content
         */
        @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
        public Repo(final String port, final String name, final String conf) {
            this.port = port;
            this.name = name;
            try {
                this.conf = Optional.of(Yaml.createYamlInput(conf).readYamlMapping());
            } catch (final IOException exc) {
                this.conf = Optional.empty();
            }
        }

        /**
         * Name of repository.
         * @return Name of repository.
         */
        public String name() {
            return this.name;
        }

        /**
         * Type of repository defined in configuration.
         * @return Repository type or empty string
         */
        public String type() {
            return this.repo().map(repo -> repo.string("type")).orElse("");
        }

        /**
         * Port of repository defined in configuration.
         * @return Repository port or default Artipie port
         */
        public String port() {
            return this.repo().map(repo -> repo.string("port")).orElse(this.port);
        }

        /**
         * Repository repo-section in yaml.
         * @return Repository repo-configuration
         */
        private Optional<YamlMapping> repo() {
            return this.conf.map(value -> value.yamlMapping("repo"));
        }
    }
}
