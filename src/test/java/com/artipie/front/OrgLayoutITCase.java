/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import java.io.IOException;
import java.util.Arrays;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test for service with `org` layout.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class OrgLayoutITCase {

    /**
     * Test deployments.
     * @checkstyle VisibilityModifierCheck (10 lines)
     */
    @RegisterExtension
    final TestService service = new TestService("org")
        .withResource(TestService.CREDS, "OrgLayoutITCase/_credentials.yaml")
        .withResource("_api_permissions.yml", "OrgLayoutITCase/_api_permissions.yml")
        .withResource("repos/Alice/pypi-repo.yaml", "OrgLayoutITCase/pypi-repo.yaml")
        .withResource("repos/Aladdin/maven/_storages.yaml", "OrgLayoutITCase/_storages.yaml");

    /**
     * Test http client.
     */
    private TestClient client;

    @BeforeEach
    void init() {
        this.client = new TestClient(this.service.port());
    }

    @Test
    void canManageRepoPerms() {
        final String alice = this.client.token("Alice", "wonderland");
        MatcherAssert.assertThat(
            "Failed to obtain auth token for Alice", alice, new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Alice failed to get perms",
            this.client.get("/api/repositories/Alice/pypi-repo/permissions", alice),
            new StringContainsInOrder(Arrays.asList("Alice", "read"))
        );
        final String aladdin = this.client.token("Aladdin", "opensesame");
        MatcherAssert.assertThat(
            "Aladdin failed to put perms",
            this.client.put(
                "/api/repositories/Alice/pypi-repo/permissions/Aladdin",
                aladdin, "[\"write\", \"tag\"]"
            ),
            new IsEqual<>(HttpStatus.CREATED_201)
        );
        MatcherAssert.assertThat(
            "Alice failed to remove perms",
            this.client.delete("/api/repositories/Alice/pypi-repo/permissions/Alice", alice),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Alice failed to get perms",
            this.client.get("/api/repositories/Alice/pypi-repo/permissions", alice),
            new StringContainsInOrder(Arrays.asList("Aladdin", "write", "tag"))
        );
    }

    @Test
    void canManageStorageAliases() {
        final String alice = this.client.token("Alice", "wonderland");
        MatcherAssert.assertThat(
            "Failed to obtain auth token for Alice", alice, new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Alice can get all storages info",
            this.client.get("/api/repositories/Aladdin/maven/storages", alice),
            new StringContainsInOrder(Arrays.asList("default", "temp"))
        );
        final String aladdin = this.client.token("Aladdin", "opensesame");
        MatcherAssert.assertThat(
            "Aladdin can add storage alias",
            this.client.put(
                "/api/repositories/Aladdin/maven/storages/local", aladdin,
                "{ \"type\": \"file\", \"path\": \"/usr/local\" }"
            ),
            new IsEqual<>(HttpStatus.CREATED_201)
        );
        MatcherAssert.assertThat(
            "Alice can delete storage alias",
            this.client.delete("/api/repositories/Aladdin/maven/storages/temp", alice),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Alice can get all storages info",
            this.client.get("/api/repositories/Aladdin/maven/storages", alice),
            new StringContainsInOrder(Arrays.asList("default", "local"))
        );
    }

    @AfterEach
    void stop() throws IOException {
        this.client.close();
    }

}
