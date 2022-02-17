/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.api.ApiAuthFilter;
import com.artipie.front.internal.HealthRoute;
import com.artipie.front.settings.ArtipieYaml;
import com.jcabi.log.Logger;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
    private static final Option CONFIG = Option.builder().option("f").longOpt("config-file")
        .hasArg(true).desc("The path to artipie configuration file").required(true).build();

    /**
     * Configuration storage.
     */
    @SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
    private final BlockingStorage storage;

    /**
     * Spark service instance.
     */
    private volatile spark.Service ignite;

    /**
     * Service constructor.
     * @param storage Config storage
     */
    private Service(final BlockingStorage storage) {
        this.storage = storage;
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
                    Yaml.createYamlInput(cmd.getOptionValue(Service.CONFIG)).readYamlMapping()
                ).storage()
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
        this.ignite.before("/api/*", new ApiAuthFilter((tkn, time) -> "anonymous"));
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
