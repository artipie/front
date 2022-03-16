/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Storages.Delete}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class StoragesDeleteTest {

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
        "john,my-conda,john/my-conda/_storages.yml",
        "john,,john/_storages.yml",
        ",internal-bin,internal-bin/_storages.yaml",
        ",,_storages.yml"
    })
    void removesStorage(final String uid, final String repo, final String key) {
        GetStoragesTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(GetUser.USER_PARAM.toString())).thenReturn(uid);
        Mockito.when(rqs.params(Storages.ST_ALIAS.toString())).thenReturn("temp");
        new Storages.Delete(this.blsto).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.OK_200);
        MatcherAssert.assertThat(
            new String(this.blsto.value(new Key.From(key)), StandardCharsets.UTF_8),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "storages:",
                    "  default:",
                    "    type: file",
                    "    path: /data/default",
                    "  mounted:",
                    "    type: file",
                    "    path: /data/mounted"
                )
            )
        );
    }

}
