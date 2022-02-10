
package com.artipie.front;

import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.jcabi.log.Logger;

/**
 *
 * @since 1.0
 */
public final class Service {

    private final Storage storage;
    private volatile spark.Service ignite;

    /**
     * Service contructor.
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
        this.ignite.get("/health", (req, res) -> "OK");
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
     */
    public static void main(final String... args) {
        // TODO: create storage from config file
        final var service = new Service(new InMemoryStorage());
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> service.stop(), "shutdown")
        );
        service.start();
    }
}
