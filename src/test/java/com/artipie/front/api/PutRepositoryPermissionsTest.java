/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.front.settings.RepoPermissions;
import com.artipie.front.settings.YamlRepoPermissions;
import java.nio.charset.StandardCharsets;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;

/**
 * Test for {@link RepositoryPermissions.Put}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class PutRepositoryPermissionsTest {

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
    void addsPermissions() {
        final Key.From key = new Key.From("mark/my-python.yaml");
        this.blsto.save(
            key,
            String.join(
                "\n",
                "repo:",
                "  type: pypi",
                "  permissions:",
                "    mark:",
                "      - read"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        final var resp = Mockito.mock(Response.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn("my-python");
        Mockito.when(rqs.params(Users.USER_PARAM.toString())).thenReturn("mark");
        Mockito.when(rqs.params(RepositoryPermissions.NAME.toString())).thenReturn("alice");
        Mockito.when(rqs.body()).thenReturn("[\"read\", \"write\"]");
        new RepositoryPermissions.Put(this.perms).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.CREATED_201);
        MatcherAssert.assertThat(
            new String(this.blsto.value(key), StandardCharsets.UTF_8),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "repo:",
                    "  type: pypi",
                    "  permissions:",
                    "    mark:",
                    "      - read",
                    "    alice:",
                    "      - read",
                    "      - write"
                )
            )
        );
    }
}
