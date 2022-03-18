/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Storages}.
 * @since 0.1
 */
class StoragesGetTest {

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
        "alice,my-maven,alice/my-maven/_storages.yml",
        "alice,,alice/_storages.yml",
        ",internal-pypi,internal-pypi/_storages.yaml",
        ",,_storages.yml"
    })
    void returnStorages(final String uid, final String repo, final String key)
        throws JSONException {
        StoragesGetTest.createSettings(this.blsto, new Key.From(key));
        final var resp = Mockito.mock(Response.class);
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn(repo);
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn(uid);
        final String handle = new Storages.Get(this.blsto).handle(rqs, resp);
        JSONAssert.assertEquals(
            handle,
            String.join(
                "\n",
                "{\"storages\": {",
                "  \"default\": {\"type\":\"file\",\"path\":\"/data/default\"},",
                "  \"temp\": {\"type\":\"file\",\"path\":\"/data/temp\"},",
                "  \"mounted\": {\"type\":\"file\",\"path\":\"/data/mounted\"}",
                "  }",
                "}"
            ),
            true
        );
    }

    static void createSettings(final BlockingStorage blsto, final Key key) {
        YamlMappingBuilder builder = Yaml.createYamlMappingBuilder();
        for (final String alias : new String[]{"default", "temp", "mounted"}) {
            builder = builder.add(
                alias,
                Yaml.createYamlMappingBuilder().add("type", "file")
                    .add("path", String.format("/data/%s", alias)).build()
            );
        }
        blsto.save(
            key,
            Yaml.createYamlMappingBuilder().add("storages", builder.build())
                .build().toString().getBytes(StandardCharsets.UTF_8)
        );
    }

}
