/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.front.api.NotFoundException;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Test for {@link YamlRepoPermissions}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class YamlRepoPermissionsTest {

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"yaml", "yml"})
    void readsPermissions(final String ext) throws JSONException {
        final String repo = "alice/my-maven";
        this.blsto.save(
            new Key.From(String.format("%s.%s", repo, ext)),
            new TestResource("RepoSettingsTest/my-maven.yaml").asBytes()
        );
        JSONAssert.assertEquals(
            new YamlRepoPermissions(this.blsto).get(repo).toString(),
            String.join(
                "\n",
                "{",
                "  \"Jane\": [\"read\", \"write\"],",
                "  \"Mark\": [\"*\"],",
                "  \"/readers\": [\"read\"]",
                "}"
                ),
            true
        );
    }

    @Test
    void throwsExceptionWhenRepoNotFound() {
        Assertions.assertThrows(
            NotFoundException.class,
            () -> new YamlRepoPermissions(this.blsto).get("any")
        );
    }

    @Test
    void returnsEmpty() {
        this.blsto.save(
            new Key.From("my-python.yaml"),
            String.join(
                "\n",
                "repo:",
                "  type: pypi"
            ).getBytes(StandardCharsets.UTF_8)
        );
        MatcherAssert.assertThat(
            new YamlRepoPermissions(this.blsto).get("my-python").toString(),
            new IsEqual<>("{}")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"yaml", "yml"})
    void addsNewUser(final String ext) throws JSONException {
        final String repo = "alice/my-maven";
        this.blsto.save(
            new Key.From(String.format("%s.%s", repo, ext)),
            new TestResource("RepoSettingsTest/my-maven.yaml").asBytes()
        );
        final YamlRepoPermissions perms = new YamlRepoPermissions(this.blsto);
        perms.add(repo, "Alice", Json.createArrayBuilder().add("write").build());
        JSONAssert.assertEquals(
            perms.get(repo).toString(),
            String.join(
                "\n",
                "{",
                "  \"Jane\": [\"read\", \"write\"],",
                "  \"Mark\": [\"*\"],",
                "  \"/readers\": [\"read\"],",
                "  \"Alice\": [\"write\"]",
                "}"
            ),
            true
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"yaml", "yml"})
    void updatesUser(final String ext) throws JSONException {
        final String repo = "alice/my-maven";
        this.blsto.save(
            new Key.From(String.format("%s.%s", repo, ext)),
            new TestResource("RepoSettingsTest/my-maven.yaml").asBytes()
        );
        final YamlRepoPermissions perms = new YamlRepoPermissions(this.blsto);
        perms.add(repo, "Mark", Json.createArrayBuilder().add("write").add("read").build());
        JSONAssert.assertEquals(
            perms.get(repo).toString(),
            String.join(
                "\n",
                "{",
                "  \"Jane\": [\"read\", \"write\"],",
                "  \"/readers\": [\"read\"],",
                "  \"Mark\": [\"write\", \"read\"]",
                "}"
            ),
            true
        );
    }
}
