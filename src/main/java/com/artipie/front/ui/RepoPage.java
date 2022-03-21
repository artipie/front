/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.ui;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.artipie.front.RequestAttr;
import com.artipie.front.api.GetRepository;
import com.artipie.front.api.Users;
import com.artipie.front.misc.RouteWrap;
import com.artipie.front.misc.ValueFromBody;
import com.artipie.front.settings.RepoSettings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Dashboard repo page.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RepoPage {

    /**
     * Dashboard repo page template view.
     * @since 0.1
     */
    public static final class TemplateView extends RouteWrap.TemplateViewRoute {

        /**
         * Template view wrap route.
         *
         * @param repos Repository settings
         */
        public TemplateView(final RepoSettings repos) {
            super(
                new HbPage(
                    "repo",
                    req -> {
                        final String uid = RequestAttr.Standard.USER_ID.readOrThrow(req);
                        final String name = GetRepository.REPO_PARAM.parse(req);
                        final Map<String, String> res = new HashMap<>(5);
                        res.put("user", uid);
                        res.put("title", uid);
                        res.put("name", name);
                        if (repos.exists(name, uid)) {
                            try {
                                final YamlMapping yaml = Yaml.createYamlInput(
                                    new ByteArrayInputStream(repos.value(name, uid))
                                ).readYamlMapping();
                                res.put("type", yaml.yamlMapping("repo").string("type"));
                                res.put("config", yaml.toString());
                            } catch (final IOException err) {
                                throw new UncheckedIOException(err);
                            }
                        } else {
                            res.put("type", req.queryParamOrDefault("type", "maven"));
                        }
                        return res;
                    }
                )
            );
        }
    }

    /**
     * Handles post request from dashboard repo page to remove/update repository.
     * @since 0.1
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     * @checkstyle ReturnCountCheck (100 lines)
     */
    public static final class Post implements Route {

        /**
         * Repository settings.
         */
        private final RepoSettings stn;

        /**
         * Ctor.
         * @param stn Repository settings
         */
        public Post(final RepoSettings stn) {
            this.stn = stn;
        }

        @Override
        @SuppressWarnings("PMD.OnlyOneReturn")
        public Object handle(final Request request, final Response response) {
            final ValueFromBody vals = new ValueFromBody(request.body());
            final String action = vals.byNameOrThrow("action");
            final String name = vals.byNameOrThrow("repo");
            final String uid = Users.USER_PARAM.parse(request);
            if ("update".equals(action)) {
                final YamlMapping yaml = Post.configsFromBody(vals);
                final YamlMapping repo = yaml.yamlMapping("repo");
                if (repo == null) {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return "Repo section is required";
                }
                if (repo.value("type") == null) {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return "Repository type is required";
                }
                if (repo.value("storage") == null) {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return "Repository storage is required";
                }
                if (this.stn.exists(name, uid)) {
                    this.stn.delete(name, uid);
                }
                this.stn.save(name, uid, yaml.toString().getBytes(StandardCharsets.UTF_8));
                response.redirect(String.format("/dashboard/%s/%s", uid, name));
            } else if ("delete".equals(action)) {
                this.stn.delete(name, uid);
                response.redirect("/dashboard");
            }
            response.status(HttpStatus.FOUND_302);
            return null;
        }

        /**
         * Obtains config from body.
         * @param vals Values in body
         * @return Config content from body
         */
        private static YamlMapping configsFromBody(final ValueFromBody vals) {
            try {
                return Yaml.createYamlInput(vals.byNameOrThrow("config")).readYamlMapping();
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }
    }

    /**
     * Handle get request while creating new repository.
     * @since 0.1
     */
    public static final class Get implements Route {

        @Override
        public Object handle(final Request request, final Response response) {
            response.status(HttpStatus.FOUND_302);
            response.redirect(
                String.format(
                    "/dashboard/%s/%s?type=%s",
                    RequestAttr.Standard.USER_ID.readOrThrow(request),
                    request.queryParams("repo"),
                    request.queryParams("type")
                )
            );
            return null;
        }
    }

}
