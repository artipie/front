/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import java.util.Arrays;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests for repository permissions.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RepoPermITCase {

    /**
     * Test deployments.
     * @checkstyle VisibilityModifierCheck (10 lines)
     */
    @RegisterExtension
    final TestService service = new TestService()
        .withResource(TestService.CREDS, "RepoPermITCase/_credentials.yaml")
        .withResource("_api_permissions.yml", "RepoPermITCase/_api_permissions.yml")
        .withResource("repos/pypi-repo.yaml", "RepoPermITCase/pypi-repo.yaml");

    /**
     * Test http client.
     */
    private TestClient client;

    @BeforeEach
    void init() {
        this.client = new TestClient(this.service.port());
    }

    @Test
    void canManageRepoPermissions() {
        final String alice = this.client.token("Alice", "wonderland");
        MatcherAssert.assertThat(
            "Failed to obtain auth token for Alice", alice, new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Alice failed to get perms",
            this.client.get("/api/repositories/pypi-repo/permissions", alice),
            new StringContainsInOrder(Arrays.asList("Aladdin", "read"))
        );
        final String aladdin = this.client.token("Aladdin", "opensesame");
        MatcherAssert.assertThat(
            "Aladdin failed to put perms",
            this.client.put(
                "/api/repositories/pypi-repo/permissions/Aladdin", aladdin, "[\"write\", \"tag\"]"
            ),
            new IsEqual<>(HttpStatus.CREATED_201)
        );
        MatcherAssert.assertThat(
            "Alice failed to get perms",
            this.client.get("/api/repositories/pypi-repo/permissions", alice),
            new StringContainsInOrder(Arrays.asList("Aladdin", "write", "tag"))
        );
        MatcherAssert.assertThat(
            "Aladdin failed to patch permissions",
            this.client.patch(
                "/api/repositories/pypi-repo/permissions", aladdin,
                String.join(
                    "\n",
                    "{",
                    "  \"grant\" : {",
                    "    \"Elen\": [\"write\", \"read\"],",
                    "    \"Alice\": [\"read\"]",
                    "  },",
                    "  \"revoke\": {",
                    "    \"Aladdin\": [\"write\"]",
                    "  }",
                    "}"
                )
            ),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Alice failed to get perms",
            this.client.get("/api/repositories/pypi-repo/permissions", alice),
            new StringContainsInOrder(
                Arrays.asList("Aladdin", "tag", "Elen", "write", "read", "Alice", "read")
            )
        );
        MatcherAssert.assertThat(
            "Aladdin failed to remove perms",
            this.client.delete("/api/repositories/pypi-repo/permissions/Aladdin", aladdin),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Alice failed to get perms",
            this.client.get("/api/repositories/pypi-repo/permissions", alice),
            new StringContainsInOrder(
                Arrays.asList("Elen", "write", "read", "Alice", "read")
            )
        );
    }

}
