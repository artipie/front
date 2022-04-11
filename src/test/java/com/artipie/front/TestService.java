/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.asto.test.TestResource;
import com.artipie.front.auth.ApiTokens;
import com.artipie.front.settings.ArtipieYaml;
import com.artipie.front.settings.ArtipieYamlTest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extension to tests to launch the service locally.
 * @since 0.1
 * @checkstyle MagicNumberCheck (500 lines)
 */
public final class TestService implements BeforeEachCallback, AfterEachCallback {

    /**
     * Credentials file name.
     */
    public static final String CREDS = "_credentials.yaml";

    /**
     * Temp directory.
     */
    private final Path tmp;

    /**
     * Test service instance.
     */
    private Service service;

    /**
     * Test port.
     */
    private int prt;

    /**
     * Ctor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public TestService() {
        try {
            this.tmp = Files.createTempDirectory("front-it-case");
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final byte[] key = new byte[20];
        Arrays.fill(key, (byte) 0);
        this.service = new Service(
            new ApiTokens(key, new Random()),
            new ArtipieYaml(
                ArtipieYamlTest.config(
                    this.tmp.toString(),
                    Optional.of(
                        Yaml.createYamlMappingBuilder().add("type", "file")
                            .add("path", TestService.CREDS).build()
                    )
                )
            )
        );
        this.prt = this.randomFreePort();
        this.service.start(this.prt);
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        if (this.service != null) {
            this.service.stop();
        }
        FileUtils.deleteQuietly(this.tmp.toFile());
    }

    /**
     * Returns port.
     * @return The port
     */
    public int port() {
        return this.prt;
    }

    /**
     * Add resource to the service.
     * @param path Relative to temp dir resource path
     * @param data Resource data
     * @return Itself
     */
    public TestService withResource(final String path, final byte[] data) {
        try {
            FileUtils.writeByteArrayToFile(this.tmp.resolve(path).toFile(), data);
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
        return this;
    }

    /**
     * Add resource to the service.
     * @param path Relative to temp dir resource path
     * @param location Resource location
     * @return Itself
     */
    public TestService withResource(final String path, final String location) {
        return this.withResource(path, new TestResource(location).asBytes());
    }

    /**
     * Get free port.
     * @return Free port
     * @throws IOException On error
     */
    private static int randomFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
