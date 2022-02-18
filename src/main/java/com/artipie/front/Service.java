/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.front.api.ApiAuthFilter;
import com.artipie.front.api.GetRepository;
import com.artipie.front.api.NotFoundException;
import com.artipie.front.api.Repositories;
import com.artipie.front.internal.HealthRoute;
import com.artipie.front.misc.RepoSettings;
import com.artipie.front.settings.ArtipieYaml;
import com.artipie.front.ui.PostSignIn;
import com.artipie.front.ui.SignInPage;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.json.Json;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.http.MimeTypes;
import spark.template.handlebars.HandlebarsTemplateEngine;

/**
 * Front service.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Service {

    /**
     * Name for argument port for service.
     */
    private static final Option PORT = new Option(
        "p", "port", true, "service port. Should be int value. Default value 8080"
    );

    /**
     * Name for argument config file for service.
     */
    private static final Option CONFIG = Option.builder().option("c").longOpt("config")
        .hasArg(true).desc("The path to artipie configuration file").required(true).build();

    /**
     * Artipie configuration.
     */
    @SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
    private final ArtipieYaml settings;

    /**
     * Spark service instance.
     */
    private volatile spark.Service ignite;

    /**
     * Service constructor.
     * @param settings Artipie configuration
     */
    private Service(final ArtipieYaml settings) {
        this.settings = settings;
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
        options.addOption(Service.CONFIG);
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            final var service = new Service(
                new ArtipieYaml(
                    Yaml.createYamlInput(new File(cmd.getOptionValue(Service.CONFIG)))
                        .readYamlMapping()
                )
            );
            service.start(Integer.parseInt(cmd.getOptionValue(Service.PORT, "8080")));
            Runtime.getRuntime().addShutdownHook(new Thread(service::stop, "shutdown"));
        } catch (final ParseException ex) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("com.artipie.front.Service", options);
            throw ex;
        } catch (final IOException ex) {
            Logger.error(Service.class, "Failed to read artipie setting yaml");
            System.exit(1);
        }
    }

    /**
     * Start service.
     * @param port Port for service
     */
    private void start(final int port) {
        if (this.ignite != null) {
            throw new IllegalStateException("already started");
        }
        Logger.info(this, "starting service on port: %d", port);
        this.ignite = spark.Service.ignite().port(port);
        this.ignite.get("/.health", new HealthRoute());
        this.ignite.path(
            "/api", () -> {
                this.ignite.before("/*", new ApiAuthFilter((tkn, time) -> "anonymous"));
                this.ignite.path(
                    "/repositories", () -> {
                        this.ignite.get(
                            "/",
                            MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Repositories(this.settings.repoConfigsStorage())
                        );
                        this.ignite.get(
                            String.format("/%s", GetRepository.PARAM),
                            MimeTypes.Type.APPLICATION_JSON.asString(),
                            new GetRepository(
                                new RepoSettings(
                                    this.settings.layout(), this.settings.repoConfigsStorage()
                                )
                            )
                        );
                    }
                );
            }
        );
        final var engine = new HandlebarsTemplateEngine("/html");
        this.ignite.path(
            "/signin",
            () -> {
                this.ignite.get(
                    "", MimeTypes.Type.APPLICATION_JSON.asString(),
                    new SignInPage(), engine
                );
                this.ignite.post(
                    "",
                    new PostSignIn(
                        (user, pass) -> Optional.of(user)
                            .filter(u -> u.equals("admin") && pass.equals("qwerty"))
                    )
                );
            }
        );
        this.ignite.before(AuthFilters.AUTHENTICATE);
        this.ignite.before(AuthFilters.SESSION_ATTRS);
        this.ignite.exception(
            NotFoundException.class, (ex, rqs, rsp) -> {
                rsp.type("application/json");
                rsp.body(
                    Json.createObjectBuilder().add("error", ex.getLocalizedMessage())
                    .build().toString()
                );
            }
        );
        this.ignite.awaitInitialization();
        Logger.info(this, "service started on port: %d", this.ignite.port());
    }

    /**
     * Stop service.
     */
    private void stop() {
        Logger.info(this, "stopping service");
        this.ignite.stop();
        this.ignite.awaitStop();
        Logger.info(this, "service stopped");
    }
}
