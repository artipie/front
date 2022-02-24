/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.api.NotFoundException;
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
        MatcherAssert.assertThat(
            new RepoSettings("flat", this.blsto).value(repo, "John"),
            new IsAnything<>()
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
        MatcherAssert.assertThat(
            new RepoSettings("org", this.blsto).value(repo, uid),
            new IsAnything<>()
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
