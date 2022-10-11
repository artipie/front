/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

/**
 * Artipie-endpoint's configuration.
 * @since 1.0
 */
public class ArtipieEndpoint {
    /**
     * Endpoint's host.
     */
    private final String host;

    /**
     * Endpoint's port.
     */
    private final int port;

    /**
     * Endpoint is secure.
     */
    private final boolean secure;

    /**
     * Ctor.
     * @param host Host.
     * @param port Port.
     * @param secure True if endpoint is secure.
     */
    public ArtipieEndpoint(final String host, final int port, final boolean secure) {
        this.host = host;
        this.port = port;
        this.secure = secure;
    }

    /**
     * Obtains endpoint's host.
     * @return Host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Obtains endpoint's port.
     * @return Port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Checks whether endpoint is secure.
     * @return True if endpoint is secure.
     */
    public boolean isSecure() {
        return this.secure;
    }
}
