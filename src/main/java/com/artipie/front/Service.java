/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.internal.HealthRoute;
import com.jcabi.log.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Front service.
 * @since 1.0
 */
public final class Service {

    /**
     * Name for argument port for service
     */
    private static final String PORT_ARG_NAME = "port";

    /**
     * Configuration storage.
     */
    private final Storage storage;

    /**
     * Spark service instance.
     */
    private volatile spark.Service ignite;

    /**
     * Service contructor.
     * @param storage Config storage
     */
    private Service(final Storage storage) {
        this.storage = storage;
    }

    /**
     * Start service.
     * @param port port for service
     */
    private void start(final int port) {
        if (this.ignite != null) {
            throw new IllegalStateException("already started");
        }
        Logger.info(this, "starting service on port: %d", port);
        this.ignite = spark.Service.ignite().port(port);
        this.ignite.get("/.health", new HealthRoute());
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

    /**
     * Entry point.
     * @param args CLA
     */
    public static void main(final String... args) throws ParseException {
        final Options options = new Options();
        options.addOption(null, Service.PORT_ARG_NAME,true,"service port");

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);

        final var service = new Service(new InMemoryStorage());
        service.start(Integer.parseInt(cmd.getOptionValue(Service.PORT_ARG_NAME,"8080")));
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> service.stop(), "shutdown")
        );
    }
}
