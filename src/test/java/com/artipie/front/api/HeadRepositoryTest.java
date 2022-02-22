/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.RequestAttr;
import com.artipie.front.misc.RepoSettings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link HeadRepository}.
 * @since 0.1
 */
class HeadRepositoryTest {

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
    }

    @ParameterizedTest
    @CsvSource({
        "flat,binary-repo.yaml,binary-repo",
        "org,Mark/pypi.yml,pypi"
    })
    void returnOkWhenRepoFound(final String layout, final String key, final String name) {
        final String uid = "Mark";
        this.blsto.save(new Key.From(key), new byte[]{});
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.PARAM)).thenReturn(name);
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn(uid);
        MatcherAssert.assertThat(
            new HeadRepository(new RepoSettings(layout, this.blsto))
                .handle(rqs, Mockito.mock(Response.class)),
            new IsAnything<>()
        );
    }

    @Test
    void throwsExceptionWhenNotFound() {
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.PARAM)).thenReturn("my-repo");
        Mockito.when(rqs.attribute(RequestAttr.Standard.USER_ID.attrName())).thenReturn("any");
        Assertions.assertThrows(
            NotFoundException.class,
            () -> new HeadRepository(new RepoSettings("flat", this.blsto))
                .handle(rqs, Mockito.mock(Response.class))
        );
    }

}
