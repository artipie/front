/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.api.NotFoundException;
import com.artipie.front.misc.Json2Yaml;
import com.artipie.front.settings.ArtipieYaml;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonObject;

/**
 * Users from yaml file.
 *
 * @since 0.1
 */
public final class YamlUsers implements Users {

    /**
     * Yaml file key.
     */
    private final Key key;

    /**
     * Storage.
     */
    private final BlockingStorage blsto;

    /**
     * Ctor.
     *
     * @param key Yaml file key
     * @param blsto Storage
     */
    public YamlUsers(final Key key, final BlockingStorage blsto) {
        this.key = key;
        this.blsto = blsto;
    }

    @Override
    public Collection<? extends User> list() {
        final Optional<YamlMapping> users = this.users();
        return users.map(
            yaml -> yaml.keys().stream()
                .map(node -> node.asScalar().value()).map(
                    name -> new YamlCredentials.YamlUser(
                        users.get().yamlMapping(name),
                        name
                    )
                ).collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    @Override
    public void add(final JsonObject info, final String uid) {
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
        final Optional<YamlMapping> users = this.users();
        if (users.isPresent()) {
            for (final YamlNode node : users.get().keys()) {
                final String val = node.asScalar().value();
                builder = builder.add(val, users.get().yamlMapping(val));
            }
        }
        builder = builder.add(uid, new Json2Yaml().apply(info.toString()));
        this.blsto.save(
            this.key,
            Yaml.createYamlMappingBuilder().add(ArtipieYaml.NODE_CREDENTIALS, builder.build())
                .build().toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public void remove(final String uid) {
        if (this.users().map(yaml -> yaml.yamlMapping(uid) != null).orElse(false)) {
            YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
            final YamlMapping users = this.users().get();
            for (final YamlNode node : users.keys()) {
                final String val = node.asScalar().value();
                if (!uid.equals(val)) {
                    builder = builder.add(val, users.yamlMapping(val));
                }
            }
            this.blsto.save(
                this.key,
                Yaml.createYamlMappingBuilder()
                    .add(ArtipieYaml.NODE_CREDENTIALS, builder.build())
                    .build().toString().getBytes(StandardCharsets.UTF_8)
            );
            return;
        }
        throw new NotFoundException(String.format("User %s does not exist", uid));
    }

    /**
     * Read yaml mapping with users from yaml file.
     *
     * @return Users yaml mapping
     */
    private Optional<YamlMapping> users() {
        Optional<YamlMapping> res = Optional.empty();
        if (this.blsto.exists(this.key)) {
            try {
                res = Optional.ofNullable(
                    Yaml.createYamlInput(
                        new String(this.blsto.value(this.key), StandardCharsets.UTF_8)
                    ).readYamlMapping().yamlMapping("credentials")
                );
            } catch (final IOException err) {
                throw new UncheckedIOException(err);
            }
        }
        return res;
    }
}
