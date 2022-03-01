/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.artipie.ArtipieException;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.SubStorage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.auth.Credentials;
import com.artipie.front.auth.Users;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Artipie yaml settings.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ArtipieYaml {

    /**
     * Yaml node credentials name.
     */
    public static final String NODE_CREDENTIALS = "credentials";

    /**
     * YAML file content.
     */
    private final YamlMapping content;

    /**
     * Ctor.
     * @param content YAML file content
     */
    public ArtipieYaml(final YamlMapping content) {
        this.content = content;
    }

    /**
     * Artipie storage.
     * @return Storage
     */
    public BlockingStorage storage() {
        return new BlockingStorage(this.asto());
    }

    /**
     * Artipie layout: flat or org.
     * @return Layout value
     */
    public String layout() {
        return Optional.ofNullable(this.meta().string("layout")).orElse("flat");
    }

    /**
     * Yaml settings meta section.
     * @return Meta yaml section
     */
    public YamlMapping meta() {
        return this.content.yamlMapping("meta");
    }

    /**
     * Repository configurations storage.
     * @return Storage, where repo configs are located
     */
    public BlockingStorage repoConfigsStorage() {
        return new BlockingStorage(
            Optional.ofNullable(this.meta().string("repo_configs"))
            .<Storage>map(str -> new SubStorage(new Key.From(str), this.asto()))
            .orElse(this.asto())
        );
    }

    /**
     * Credentials from config yaml mapping if the credentials type is file and
     * this file exists.
     * @return Credentials file yaml
     */
    public Optional<YamlMapping> fileCredentials() {
        final Optional<Key> key = this.fileCredentialsKey();
        Optional<YamlMapping> res = Optional.empty();
        if (key.isPresent() && this.storage().exists(key.get())) {
            try {
                res = Optional.of(
                    Yaml.createYamlInput(
                        new String(
                            this.storage().value(key.get()),
                            StandardCharsets.UTF_8
                        )
                    ).readYamlMapping()
                );
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        } else if (key.isPresent()) {
            res = Optional.of(Yaml.createYamlMappingBuilder().build());
        }
        return res;
    }

    /**
     * File credentials key (=storage relative path) if set.
     * @return Key to credentials file.
     */
    public Optional<Key> fileCredentialsKey() {
        return Optional.ofNullable(
            this.meta().yamlSequence(ArtipieYaml.NODE_CREDENTIALS)
        ).map(
            seq -> seq.values().stream()
                .filter(node -> "file".equals(node.asMapping().string("type"))).findFirst()
                .map(YamlNode::asMapping)
        ).orElse(Optional.ofNullable(this.meta().yamlMapping(ArtipieYaml.NODE_CREDENTIALS)))
            .map(file -> new Key.From(file.string("path")));
    }

    /**
     * Artipie users.
     * @return Users instance
     */
    public Users users() {
        return new Users.FromYamlFile(
            this.fileCredentialsKey().orElseThrow(
                () -> new IllegalStateException("Only users from file auth are supported")
            ), this.storage()
        );
    }

    /**
     * Credentials from config.
     * @return Credentials
     */
    public Credentials credentials() {
        return new Credentials.FromYaml(
            this.fileCredentials()
                .orElseThrow(() -> new NotImplementedException("Not implemented yet"))
        );
    }

    @Override
    public String toString() {
        return String.format("YamlSettings{\n%s\n}", this.content.toString());
    }

    /**
     * Abstract storage.
     * @return Asto
     */
    private Storage asto() {
        return new YamlStorage(
            Optional.ofNullable(
                this.meta().yamlMapping("storage")
            ).orElseThrow(
                () -> new ArtipieException(
                    String.format(
                        "Failed to find storage configuration in \n%s", this.content.toString()
                    )
                )
            )
        ).storage();
    }
}
