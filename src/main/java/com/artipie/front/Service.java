/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.internal.HealthRoute;
import com.jcabi.log.Logger;

/**
 * Front service.
 * @since 1.0
 */
public final class Service {

    /**
     * Configuration storage.
     */
    private final Storage storage;

    /**
     * Spart service instance.
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
     */
    private void start() {
        if (this.ignite != null) {
            throw new IllegalStateException("already started");
        }
        // TODO: parse port from CLI args: --port=8080
        Logger.info(this, "starting service");
        this.ignite = spark.Service.ignite().port(8080);
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
    public static void main(final String... args) {
        // TODO: create storage from config file
        final var service = new Service(new InMemoryStorage());
        service.start();
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> service.stop(), "shutdown")
        );
    }
}
