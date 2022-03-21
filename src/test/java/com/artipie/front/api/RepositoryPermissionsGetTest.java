/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.front.settings.RepoPermissions;
import com.artipie.front.settings.YamlRepoPermissions;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link RepositoryPermissions.Get}.
 * @since 0.1
 */
class RepositoryPermissionsGetTest {

    /**
     * Test storage.
     */
    private BlockingStorage blsto;

    /**
     * Repository permissions.
     */
    private RepoPermissions perms;

    @BeforeEach
    void init() {
        this.blsto = new BlockingStorage(new InMemoryStorage());
        this.perms = new YamlRepoPermissions(this.blsto);
    }

    @Test
    void returnsPermissions() throws JSONException {
        this.blsto.save(
            new Key.From("my-maven.yaml"),
            new TestResource("GetRepositoryPermissionsTest/my-maven.yaml").asBytes()
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn("my-maven");
        JSONAssert.assertEquals(
            new RepositoryPermissions.Get(this.perms).handle(rqs, Mockito.mock(Response.class)),
            String.join(
                "\n",
                "{\"permissions\": {",
                "  \"Jane\": [\"read\", \"write\"],",
                "  \"Mark\": [\"*\"],",
                "  \"/readers\": [\"read\"]",
                "}}"
            ),
            true
        );
    }

    @Test
    void returnEmptyWhenPermissionsAreNotSet() throws JSONException {
        this.blsto.save(
            new Key.From("alice/my-python.yaml"),
            String.join(
                "\n",
                "repo:",
                "  type: pypi"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn("my-python");
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn("alice");
        JSONAssert.assertEquals(
            new RepositoryPermissions.Get(this.perms).handle(rqs, Mockito.mock(Response.class)),
            "{\"permissions\": {}}",
            true
        );
    }
}
