/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;
import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.front.api.NotFoundException;
import com.artipie.front.misc.Yaml2Json;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * Implementation of {@link RepoPermissions} to handle repository permissions.
 * This implementation takes into account both .yml and .yaml extensions.
 * @since 0.1
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
public final class YamlRepoPermissions implements RepoPermissions {

    /**
     * Permissions yaml section.
     */
    private static final String PERMS = "permissions";

    /**
     * Repo yaml section.
     */
    private static final String REPO = "repo";

    /**
     * Artipie repository settings storage.
     */
    private final BlockingStorage blsto;

    /**
     * Ctor.
     * @param blsto Artipie repository settings storage
     */
    public YamlRepoPermissions(final BlockingStorage blsto) {
        this.blsto = blsto;
    }

    @Override
    public JsonObject get(final String repo) {
        return this.permsYaml(repo).map(YamlMapping::toString).map(new Yaml2Json())
            .map(JsonValue::asJsonObject)
            .orElse(Json.createObjectBuilder().build());
    }

    @Override
    public void add(final String repo, final String uid, final JsonArray perms) {
        final Optional<YamlMapping> creds = this.permsYaml(repo);
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
        if (creds.isPresent()) {
            for (final YamlNode node : creds.get().keys()) {
                final String usr = node.asScalar().value();
                if (!uid.equals(usr)) {
                    builder = builder.add(usr, creds.get().yamlSequence(usr));
                }
            }
        }
        YamlSequenceBuilder seq = Yaml.createYamlSequenceBuilder();
        for (final String item : perms.getValuesAs(JsonValue::toString)) {
            seq = seq.add(item.replaceAll("\"", ""));
        }
        builder = builder.add(uid, seq.build());
        this.updateRepoSettings(repo, builder.build());
    }

    @Override
    public void delete(final String repo, final String uid) {
        final Optional<YamlMapping> creds = this.permsYaml(repo);
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
        if (creds.isPresent() && creds.get().yamlSequence(uid) != null) {
            for (final YamlNode node : creds.get().keys()) {
                final String usr = node.asScalar().value();
                if (!uid.equals(usr)) {
                    builder = builder.add(usr, creds.get().yamlSequence(usr));
                }
            }
            this.updateRepoSettings(repo, builder.build());
            return;
        }
        throw new NotFoundException(
            String.format("User %s does have any permissions in repository %s", uid, repo)
        );
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void patch(final String repo, final JsonStructure perms) {
        final Optional<YamlMapping> creds = this.permsYaml(repo);
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
        final JsonObject revoke = perms.asJsonObject().getJsonObject("revoke");
        final JsonObject grant = perms.asJsonObject().getJsonObject("grant");
        final Set<String> granted = new HashSet<>(grant.size());
        if (creds.isPresent()) {
            for (final YamlNode node : creds.get().keys()) {
                final String usr = node.asScalar().value();
                granted.add(usr);
                Stream<String> stream = creds.get().yamlSequence(usr).values()
                    .stream().map(item -> item.asScalar().value());
                if (revoke.containsKey(usr)) {
                    stream = stream.filter(
                        item -> YamlRepoPermissions.toStream(revoke.getJsonArray(usr))
                            .noneMatch(rvk -> rvk.equals(item))
                    );
                }
                if (grant.containsKey(usr)) {
                    stream = Stream.concat(
                        stream, YamlRepoPermissions.toStream(grant.getJsonArray(usr))
                    );
                }
                final List<String> list = stream.collect(Collectors.toList());
                if (!list.isEmpty()) {
                    YamlSequenceBuilder seq = Yaml.createYamlSequenceBuilder();
                    for (final String perm : list) {
                        seq = seq.add(perm);
                    }
                    builder = builder.add(usr, seq.build());
                }
            }
        }
        for (final Map.Entry<String, JsonValue> entry : grant.entrySet()) {
            if (!granted.contains(entry.getKey())) {
                YamlSequenceBuilder seq = Yaml.createYamlSequenceBuilder();
                final List<String> list = YamlRepoPermissions
                    .toStream(entry.getValue().asJsonArray()).collect(Collectors.toList());
                for (final String perm : list) {
                    seq = seq.add(perm);
                }
                builder = builder.add(entry.getKey(), seq.build());
            }
        }
        this.updateRepoSettings(repo, builder.build());
    }

    /**
     * Updates credentials section in repository settings.
     * @param repo Repository name
     * @param creds New credentials section
     */
    private void updateRepoSettings(final String repo, final YamlMapping creds) {
        try {
            final YamlMapping stngs = Yaml.createYamlInput(
                new String(this.blsto.value(this.key(repo)), StandardCharsets.UTF_8)
            ).readYamlMapping().yamlMapping(YamlRepoPermissions.REPO);
            YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
            for (final YamlNode node : stngs.keys()) {
                final String map = node.asScalar().value();
                if (!YamlRepoPermissions.PERMS.equals(map)) {
                    builder = builder.add(map, stngs.value(map));
                }
            }
            builder = builder.add(YamlRepoPermissions.PERMS, creds);
            this.blsto.save(
                this.key(repo),
                Yaml.createYamlMappingBuilder().add(YamlRepoPermissions.REPO, builder.build())
                    .build().toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Get repository settings key.
     * @param name Repository name
     * @return The key of found
     * @throws com.artipie.front.api.NotFoundException If repository does not exist
     */
    private Key key(final String name) {
        Key res = new Key.From(String.format("%s.yaml", name));
        if (!this.blsto.exists(res)) {
            res = new Key.From(String.format("%s.yml", name));
            if (!this.blsto.exists(res)) {
                throw new NotFoundException(String.format("Repository %s not found", name));
            }
        }
        return res;
    }

    /**
     * Reads repo setting section permissions.
     * @param repo Repository name
     * @return Credentials section if present
     */
    private Optional<YamlMapping> permsYaml(final String repo) {
        try {
            return Optional.ofNullable(
                Yaml.createYamlInput(
                    new String(this.blsto.value(this.key(repo)), StandardCharsets.UTF_8)
                ).readYamlMapping().yamlMapping(YamlRepoPermissions.REPO)
                    .yamlMapping(YamlRepoPermissions.PERMS)
            );
        } catch (final IOException err) {
            throw new UncheckedIOException(err);
        }
    }

    /**
     * Transform json array to stream of strings.
     * {@link JsonValue#toString()} method adds extra quotes, so
     * we need to remove them.
     * @param arr Json array
     * @return Stream of strings
     */
    private static Stream<String> toStream(final JsonArray arr) {
        return arr.getValuesAs(JsonValue::toString)
            .stream().map(val -> val.replace("\"", ""));
    }
}
