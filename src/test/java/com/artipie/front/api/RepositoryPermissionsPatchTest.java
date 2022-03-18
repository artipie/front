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
 * Test for {@link RepositoryPermissions.Patch}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepositoryPermissionsPatchTest {

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
    void patchesPermissions() {
        final Key.From key = new Key.From("internal-maven.yaml");
        this.blsto.save(
            key,
            String.join(
                "\n",
                "repo:",
                "  type: maven",
                "  permissions:",
                "    alice:",
                "      - read"
            ).getBytes(StandardCharsets.UTF_8)
        );
        final var rqs = Mockito.mock(Request.class);
        final var resp = Mockito.mock(Response.class);
        Mockito.when(rqs.params(GetRepository.REPO_PARAM.toString())).thenReturn("internal-maven");
        Mockito.when(rqs.body()).thenReturn(
            String.join(
                "\n",
                "{",
                "  \"grant\" : {",
                "    \"Elen\": [\"write\", \"read\"]",
                "  },",
                "  \"revoke\": {",
                "    \"alice\": [\"read\"]",
                "  }",
                "}"
            )
        );
        new RepositoryPermissions.Patch(this.perms).handle(rqs, resp);
        Mockito.verify(resp).status(HttpStatus.OK_200);
        MatcherAssert.assertThat(
            new String(this.blsto.value(key), StandardCharsets.UTF_8),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "repo:",
                    "  type: maven",
                    "  permissions:",
                    "    Elen:",
                    "      - write",
                    "      - read"
                )
            )
        );
    }

}
