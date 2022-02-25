/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.api.NotFoundException;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link RepoSettings}.
 * @since 0.2
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepoSettingsTest {

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
    void findsSettingsWhenFlat(final String ext) {
        final String repo = "docker";
        this.blsto.save(new Key.From(String.format("%s.%s", repo, ext)), new byte[]{});
        this.blsto.save(new Key.From("any"), new byte[]{});
        final RepoSettings stn = new RepoSettings("flat", this.blsto);
        final String uid = "John";
        MatcherAssert.assertThat(
            "Failed to obtain value",
            stn.value(repo, uid),
            new IsAnything<>()
        );
        MatcherAssert.assertThat(
            "Failed to check if repo exists",
            stn.exists(repo, uid)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"yaml", "yml"})
    void findsSettingsWhenOrg(final String ext) {
        final String repo = "conda";
        final String uid = "Olga";
        this.blsto.save(
            new Key.From(new Key.From(uid, String.format("%s.%s", repo, ext))),
            new byte[]{}
        );
        this.blsto.save(new Key.From("Mark/conda.yaml"), new byte[]{});
        final RepoSettings stn = new RepoSettings("org", this.blsto);
        MatcherAssert.assertThat(
            "Failed to obtain value",
            stn.value(repo, uid),
            new IsAnything<>()
        );
        MatcherAssert.assertThat(
            "Failed to check if repo exists",
            stn.exists(repo, uid)
        );
    }

    @Test
    void returnsFalseWhenRepoDoesNotExists() {
        MatcherAssert.assertThat(
            new RepoSettings("org", this.blsto).exists("any", "my-repo"),
            new IsEqual<>(false)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"org", "flat"})
    void savesValue(final String layout) {
        final String uid = "Alice";
        final RepoSettings stn = new RepoSettings(layout, this.blsto);
        final String name = "my-rpm";
        stn.save(name, uid, "abc123".getBytes(StandardCharsets.UTF_8));
        MatcherAssert.assertThat(
            stn.exists(name, uid),
            new IsEqual<>(true)
        );
    }

    @Test
    void deletesValue() {
        final byte[] bytes = "abc123".getBytes();
        final Key.From key = new Key.From("my-favorite-repo.yaml");
        this.blsto.save(key, bytes);
        new RepoSettings("flat", this.blsto).delete("my-favorite-repo", "any");
        MatcherAssert.assertThat(
            this.blsto.exists(key),
            new IsEqual<>(false)
        );
    }

    @Test
    void throwsExceptionWhenDoesNotExists() {
        Assertions.assertThrows(
            NotFoundException.class,
            () -> new RepoSettings("flat", this.blsto).value("rpm", "Jane")
        );
    }

}
