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
 * Test for {@link Storages.Put}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class StoragesPutTest {

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
        "ann,my-nuget,ann/my-nuget/_storages.yml",
        "ann,,ann/_storages.yml",
        ",internal-rpm,internal-rpm/_storages.yaml",
        ",,_storages.yml"
    })
    void addsStorageAlias(final String uid, final String repo, final String key) {
        StoragesGetTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        Mockito.when(rqs.params(Storages.ST_ALIAS.toString())).thenReturn("local");
        Mockito.when(rqs.body()).thenReturn("{ \"type\": \"file\", \"path\": \"/usr/local\" }");
        new Storages.Put(this.blsto).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CREATED_201);
        MatcherAssert.assertThat(
            new String(this.blsto.value(new Key.From(key)), StandardCharsets.UTF_8),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "storages:",
                    "  default:",
                    "    type: file",
                    "    path: /data/default",
                    "  temp:",
                    "    type: file",
                    "    path: /data/temp",
                    "  mounted:",
                    "    type: file",
                    "    path: /data/mounted",
                    "  local:",
                    "    type: file",
                    "    path: /usr/local"
                )
            )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "jacob,my-maven,jacob/my-maven/_storages.yml",
        "jacob,,jacob/_storages.yml",
        ",internal-gem,internal-gem/_storages.yaml",
        ",,_storages.yml"
    })
    void returnsConflictWhenAliasAlreadyExists(final String uid, final String repo,
        final String key) {
        StoragesGetTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        Mockito.when(rqs.params(Storages.ST_ALIAS.toString())).thenReturn("temp");
        Mockito.when(rqs.body()).thenReturn("{ \"type\": \"file\", \"path\": \"/usr/temp\" }");
        new Storages.Put(this.blsto).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CONFLICT_409);
    }

}
