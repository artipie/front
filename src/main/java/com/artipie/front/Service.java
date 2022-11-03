/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.artipie.front.internal.HealthRoute;
import com.artipie.front.rest.AuthService;
import com.artipie.front.rest.RepositoryService;
import com.artipie.front.rest.SettingsService;
import com.artipie.front.ui.HbTemplateEngine;
import com.artipie.front.ui.PostSignIn;
import com.artipie.front.ui.SignInPage;
import com.artipie.front.ui.repository.RepoAddConfig;
import com.artipie.front.ui.repository.RepoAddInfo;
import com.artipie.front.ui.repository.RepoEdit;
import com.artipie.front.ui.repository.RepoList;
import com.artipie.front.ui.repository.RepoRemove;
import com.artipie.front.ui.repository.RepoSave;
import com.artipie.front.ui.repository.RepositoryInfo;
import com.artipie.front.ui.repository.RepositoryTemplate;
import com.fasterxml.jackson.core.JsonParseException;
import com.jcabi.log.Logger;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.ExceptionHandler;
import spark.ModelAndView;

/**
 * Front service.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @checkstyle JavaNCSSCheck (500 lines)
 * @checkstyle MethodLengthCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength"})
public final class Service {
    /**
     * Name for argument port for service.
     */
    private static final Option PORT = new Option(
        "p", "port", true, "service port. Should be int value. Default value 8080"
    );

    /**
     * Name for argument artipie rest endpoint.
     */
    private static final Option REST = new Option(
        "r", "rest", true, "The artipie rest endpoint. Default value http://localhost:8086"
    );

    /**
     * Spark service instance.
     */
    private volatile spark.Service ignite;

    /**
     * Template engine.
     */
    private final HbTemplateEngine engine;

    /**
     * Service constructor.
     */
    Service() {
        this.engine = new HbTemplateEngine("/html");
    }

    /**
     * Entry point.
     * @param args CLA
     * @throws ParseException If cli line argument has wrong format
     */
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public static void main(final String... args) throws ParseException {
        final Options options = new Options();
        options.addOption(Service.PORT);
        options.addOption(Service.REST);
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            final var service = new Service();
            service.start(
                Integer.parseInt(new Param(Service.PORT, "ARTIPIE_PORT", "8080").get(cmd)),
                new Param(Service.REST, "ARTIPIE_REST", "http://localhost:8086").get(cmd)
            );
            Runtime.getRuntime().addShutdownHook(new Thread(service::stop, "shutdown"));
        } catch (final ParseException ex) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("com.artipie.front.Service", options);
            throw ex;
        }
    }

    /**
     * Start service.
     * @param port Port for service
     * @param rest Artipie rest endpoint
     */
    void start(final int port, final String rest) {
        if (this.ignite != null) {
            throw new IllegalStateException("already started");
        }
        Logger.info(this, "starting service on port: %d", port);
        this.ignite = spark.Service.ignite().port(port);
        this.ignite.get("/.health", new HealthRoute());
        this.ignite.path(
            "/signin",
            () -> {
                this.ignite.get(
                    "",
                    MimeTypes.Type.APPLICATION_JSON.asString(),
                    new SignInPage(),
                    this.engine
                );
                this.ignite.post(
                    "",
                    new PostSignIn(new AuthService(rest))
                );
            }
        );
        this.ignite.path(
            "/dashboard",
            () -> {
                this.ignite.get(
                    "",
                    (req, res) -> {
                        res.redirect("/dashboard/repository/list");
                        return "Ok";
                    }
                );
                final RepositoryService repository = new RepositoryService(rest);
                final RepositoryInfo info = new RepositoryInfo();
                final RepositoryTemplate template = new RepositoryTemplate();
                final Layout layout = new SettingsService(rest).layout();
                this.ignite.path(
                    "/repository", () -> {
                        this.ignite.get(
                            "/list",
                            new RepoList(repository, layout),
                            this.engine
                        );
                        if (layout == Layout.FLAT) {
                            this.ignite.get(
                                "/edit/:repo",
                                new RepoEdit(repository, layout, info),
                                this.engine
                            );
                            this.ignite.post(
                                "/update/:repo",
                                new RepoSave(repository, layout),
                                this.engine
                            );
                            this.ignite.post(
                                "/remove/:repo",
                                new RepoRemove(repository, layout),
                                this.engine
                            );
                        } else {
                            this.ignite.get(
                                "/edit/:user/:repo",
                                new RepoEdit(repository, layout, info),
                                this.engine
                            );
                            this.ignite.post(
                                "/update/:user/:repo",
                                new RepoSave(repository, layout),
                                this.engine
                            );
                            this.ignite.post(
                                "/remove/:user/:repo",
                                new RepoRemove(repository, layout),
                                this.engine
                            );
                        }
                        this.ignite.get("/add/info", new RepoAddInfo(), this.engine);
                        this.ignite.get(
                            "/add/config",
                            new RepoAddConfig(layout, info, template),
                            this.engine
                        );
                    }
                );
            }
        );
        this.ignite.before(AuthFilters.AUTHENTICATE);
        this.ignite.before(AuthFilters.SESSION_ATTRS);
        this.ignite.exception(JsonException.class, Service.error(HttpStatus.BAD_REQUEST_400));
        this.ignite.exception(JsonParseException.class, Service.error(HttpStatus.BAD_REQUEST_400));
        this.ignite.exception(RestException.class, this.restError());
        this.ignite.exception(Exception.class, this.error());
        this.ignite.awaitInitialization();
        Logger.info(this, "service started on port: %d", this.ignite.port());
    }

    /**
     * Stop service.
     */
    void stop() {
        Logger.info(this, "stopping service");
        this.ignite.stop();
        this.ignite.awaitStop();
        Logger.info(this, "service stopped");
    }

    /**
     * Handle exceptions by writing error in json body and returning
     * provided status.
     * @param status Status to return
     * @return Instance of {@link ExceptionHandler}
     */
    private static ExceptionHandler<Exception> error(final int status) {
        return (ex, rqs, rsp) -> {
            rsp.type(MimeTypes.Type.APPLICATION_JSON.toString());
            rsp.body(
                Json.createObjectBuilder().add("error", ex.getLocalizedMessage())
                    .build().toString()
            );
            rsp.status(status);
        };
    }

    /**
     * Handle RestException by rendering html-page with errorMessage and http status code
     * received from rest service.
     * @return Instance of {@link ExceptionHandler}
     */
    private ExceptionHandler<RestException> restError() {
        return (exc, rqs, rsp) -> {
            rsp.type(MimeTypes.Type.TEXT_HTML.asString());
            rsp.body(
                this.engine.render(
                    new ModelAndView(
                        Map.of(
                            "errorMessage", exc.getMessage(),
                            "statusCode", exc.statusCode()
                        ),
                        "restError"
                    )
                )
            );
        };
    }

    /**
     * Handle Exception by rendering html-page with errorMessage.
     * @return Instance of {@link ExceptionHandler}
     */
    private ExceptionHandler<Exception> error() {
        return (exc, rqs, rsp) -> {
            rsp.type(MimeTypes.Type.TEXT_HTML.asString());
            rsp.body(
                this.engine.render(
                    new ModelAndView(
                        Map.of(
                            "errorMessage", ExceptionUtils.getMessage(exc),
                            "stackTrace", ExceptionUtils.getStackTrace(exc)
                        ),
                        "error"
                    )
                )
            );
        };
    }

    /**
     * Parameter.
     * @since 1.0
     */
    private static class Param {
        /**
         * Option cmd argument.
         */
        private final Option option;

        /**
         * Environment argument.
         */
        private final String env;

        /**
         * Default value.
         */
        private final String def;

        /**
         * Ctor.
         * @param option Option cmd argument name.
         * @param envparam Environment argument name.
         * @param def Default value.
         */
        Param(final Option option, final String envparam, final String def) {
            this.option = option;
            this.env = envparam;
            this.def = def;
        }

        /**
         * Get parameter from cmd-arguments or environment or by default.
         * @param cmd Command line.
         * @return Parameter value.
         */
        public String get(final CommandLine cmd) {
            String param = cmd.getOptionValue(this.option);
            if (param == null) {
                param = System.getenv(this.env);
                if (param == null) {
                    param = this.def;
                }
            }
            return param;
        }
    }
}
