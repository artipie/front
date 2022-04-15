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
 * Test for API endpoints to manage storage aliases.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class StorageAliasesITCase {

    /**
     * Test deployments.
     * @checkstyle VisibilityModifierCheck (10 lines)
     */
    @RegisterExtension
    final TestService service = new TestService()
        .withResource(TestService.CREDS, "StorageAliasesITCase/_credentials.yaml")
        .withResource("_api_permissions.yml", "StorageAliasesITCase/_api_permissions.yml")
        .withResource("repos/_storages.yaml", "StorageAliasesITCase/_storages.yaml");

    /**
     * Test http client.
     */
    private TestClient client;

    @BeforeEach
    void init() {
        this.client = new TestClient(this.service.port());
    }

    @Test
    void canManageStorages() {
        final String aladdin = this.client.token("Aladdin", "opensesame");
        MatcherAssert.assertThat(
            "Failed to obtain auth token for Aladdin", aladdin, new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Aladdin can get all storages info",
            this.client.get("/api/storages", aladdin),
            new StringContainsInOrder(Arrays.asList("default", "temp"))
        );
        MatcherAssert.assertThat(
            "Aladdin can check alias exists",
            this.client.head("/api/storages/temp", aladdin),
            new IsEqual<>(HttpStatus.FOUND_302)
        );
        final String alice = this.client.token("Alice", "wonderland");
        MatcherAssert.assertThat(
            "Alice can add storage alias",
            this.client.put(
                "/api/storages/local", alice, "{ \"type\": \"file\", \"path\": \"/usr/local\" }"
            ),
            new IsEqual<>(HttpStatus.CREATED_201)
        );
        MatcherAssert.assertThat(
            "Alice can check new storage alias exists",
            this.client.head("/api/storages/local", alice),
            new IsEqual<>(HttpStatus.FOUND_302)
        );
        MatcherAssert.assertThat(
            "Aladdin can check new storage alias exists",
            this.client.head("/api/storages/local", aladdin),
            new IsEqual<>(HttpStatus.FOUND_302)
        );
        MatcherAssert.assertThat(
            "Alice can delete storage alias",
            this.client.delete("/api/storages/temp", alice),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Alice can get storages aliases",
            this.client.get("/api/storages", alice),
            new StringContainsInOrder(Arrays.asList("default", "local"))
        );
        MatcherAssert.assertThat(
            "Aladdin can check storage alias exists",
            this.client.head("/api/storages/temp", aladdin),
            new IsEqual<>(HttpStatus.NOT_FOUND_404)
        );
    }

    @AfterEach
    void stop() throws IOException {
        this.client.close();
    }
}
