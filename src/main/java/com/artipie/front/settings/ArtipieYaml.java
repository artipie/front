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
import com.artipie.front.auth.AccessPermissions;
import com.artipie.front.auth.Credentials;
import com.artipie.front.auth.EnvCredentials;
import com.artipie.front.auth.GithubCredentials;
import com.artipie.front.auth.UserPermissions;
import com.artipie.front.auth.Users;
import com.artipie.front.auth.YamlCredentials;
import com.artipie.front.auth.YamlUsers;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Artipie yaml settings.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ArtipieYaml {

    /**
     * Yaml node credentials name.
     */
    public static final String NODE_CREDENTIALS = "credentials";

    /**
     * Yaml node type.
     */
    private static final String NODE_TYPE = "type";

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
     * Artipie users.
     * @return Users instance
     */
    public Users users() {
        return new YamlUsers(
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
        final List<Credentials> res = new ArrayList<>(3);
        this.yamlCredentials().ifPresent(item -> res.add(new YamlCredentials(item)));
        if (this.credentialsTypeSet("env")) {
            res.add(new EnvCredentials());
        }
        if (this.credentialsTypeSet("github")) {
            res.add(new GithubCredentials());
        }
        return new Credentials.Any(res);
    }

    /**
     * Read access permissions.
     * @return Instance of {@link AccessPermissions}
     */
    public AccessPermissions accessPermissions() {
        return this.apiPermissions("endpoints")
            .<AccessPermissions>map(AccessPermissions.FromYaml::new)
            .orElse(AccessPermissions.STUB);
    }

    /**
     * Read users api permissions.
     * @return Instance of {@link UserPermissions}
     */
    public UserPermissions userPermissions() {
        return this.apiPermissions("users")
            .<UserPermissions>map(UserPermissions.FromYaml::new)
            .orElse(UserPermissions.STUB);
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

    /**
     * File credentials key (=storage relative path) if set.
     * @return Key to credentials file.
     */
    private Optional<Key> fileCredentialsKey() {
        return Optional.ofNullable(
            this.meta().yamlSequence(ArtipieYaml.NODE_CREDENTIALS)
        ).map(
            seq -> seq.values().stream()
                .filter(node -> "file".equals(node.asMapping().string(ArtipieYaml.NODE_TYPE)))
                .findFirst().map(YamlNode::asMapping)
        ).orElse(Optional.ofNullable(this.meta().yamlMapping(ArtipieYaml.NODE_CREDENTIALS)))
        .map(file -> new Key.From(file.string("path")));
    }

    /**
     * Obtain credentials from file yaml mapping if file credentials are set.
     * @return YamlMapping if found
     */
    private Optional<YamlMapping> yamlCredentials() {
        final Optional<Key> key = this.fileCredentialsKey();
        Optional<YamlMapping> yaml = Optional.empty();
        if (key.isPresent() && this.storage().exists(key.get())) {
            try {
                yaml = Optional.of(
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
            yaml = Optional.of(Yaml.createYamlMappingBuilder().build());
        }
        return yaml;
    }

    /**
     * Are credentials with given type set?
     * @param type Credentials type
     * @return True if configured
     */
    private boolean credentialsTypeSet(final String type) {
        return Optional.ofNullable(
            this.meta().yamlSequence(ArtipieYaml.NODE_CREDENTIALS)
        ).map(
            seq -> seq.values().stream()
                .anyMatch(node -> type.equals(node.asMapping().string(ArtipieYaml.NODE_TYPE)))
        ).orElse(false);
    }

    /**
     * Read API permissions.
     * @param section Yaml section to read
     * @return Yaml mapping if the file exists
     */
    private Optional<YamlMapping> apiPermissions(final String section) {
        Optional<YamlMapping> res = Optional.empty();
        final Key key = new Key.From("_api_permissions.yml");
        if (this.storage().exists(key)) {
            try {
                res = Optional.of(
                    Yaml.createYamlInput(
                        new String(this.storage().value(key), StandardCharsets.UTF_8)
                    ).readYamlMapping().yamlMapping(section)
                );
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }
        return res;
    }
}
