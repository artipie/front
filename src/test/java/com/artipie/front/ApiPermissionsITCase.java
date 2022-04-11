/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.google.common.collect.Lists;
import java.io.IOException;
import javax.json.Json;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Service IT.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ApiPermissionsITCase {

    /**
     * Test deployments.
     * @checkstyle VisibilityModifierCheck (10 lines)
     */
    @RegisterExtension
    final TestService service = new TestService()
        .withResource(TestService.CREDS, "ApiPermissionsITCase/_credentials.yaml")
        .withResource("_api_permissions.yml", "ApiPermissionsITCase/_api_permissions.yml")
        .withResource("repos/maven-repo.yaml", "ApiPermissionsITCase/maven-repo.yaml");

    /**
     * Test http client.
     */
    private TestClient client;

    @BeforeEach
    void init() {
        this.client = new TestClient(this.service.port());
    }

    @Test
    void aliceCanGetRepoAndUsers() {
        final String token = this.client.token("Alice", "wonderland");
        MatcherAssert.assertThat(
            "Failed to obtain auth token",
            token,
            new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Failed to get users info",
            this.client.get("/api/users", token),
            new StringContainsInOrder(Lists.newArrayList("Alice", "John"))
        );
        MatcherAssert.assertThat(
            "Failed to get check John exists",
            this.client.head("/api/users/John", token),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Failed to get Alice info",
            this.client.get("/api/users/Alice", token),
            new StringContains("Alice")
        );
        MatcherAssert.assertThat(
            "Failed to get repos info",
            this.client.get("/api/repositories", token),
            new StringContains("maven-repo")
        );
        MatcherAssert.assertThat(
            "Failed to check maven repo exists",
            this.client.head("/api/repositories/maven-repo", token),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Failed to get maven repo info",
            this.client.get("/api/repositories/maven-repo", token),
            new StringContainsInOrder(Lists.newArrayList("type", "maven"))
        );
    }

    @Test
    void aliceCannotWrite() {
        final String token = this.client.token("Alice", "wonderland");
        MatcherAssert.assertThat(
            "Should not be able to write users",
            this.client.put("/api/users/Olga", token, "any"),
            new IsEqual<>(HttpStatus.FORBIDDEN_403)
        );
        MatcherAssert.assertThat(
            "Should not be able to write repositories",
            this.client.put("/api/repositories/python", token, "any"),
            new IsEqual<>(HttpStatus.FORBIDDEN_403)
        );
        MatcherAssert.assertThat(
            "Should not be able to read storages",
            this.client.head("/api/storages/any", token),
            new IsEqual<>(HttpStatus.FORBIDDEN_403)
        );
    }

    @Test
    void johnCanReachAll() {
        final String token = this.client.token("John", "123");
        MatcherAssert.assertThat(
            "Failed to check if storage alias exists",
            this.client.head("/api/storages/any", token),
            new IsEqual<>(HttpStatus.NOT_FOUND_404)
        );
        MatcherAssert.assertThat(
            "Failed to check maven repo exists",
            this.client.head("/api/repositories/maven-repo", token),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Failed to check Alice exists",
            this.client.head("/api/users/Alice", token),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Failed to check Olga exists",
            this.client.head("/api/users/Olga", token),
            new IsEqual<>(HttpStatus.NOT_FOUND_404)
        );
        MatcherAssert.assertThat(
            "Aladdin failed to create new user",
            this.client.put(
                "/api/users/Olga", token,
                Json.createObjectBuilder().add(
                    "Olga",
                    Json.createObjectBuilder().add("type", "plain").add("pass", "123").build()
                ).build().toString()
            ),
            new IsEqual<>(HttpStatus.CREATED_201)
        );
        MatcherAssert.assertThat(
            "Failed to get Alice info",
            this.client.get("/api/users/Olga", token),
            new StringContains("Olga")
        );
        MatcherAssert.assertThat(
            "Failed to head any endpoint",
            this.client.head("/api/any", token),
            new IsEqual<>(HttpStatus.NOT_FOUND_404)
        );
    }

    @AfterEach
    void stop() throws IOException {
        this.client.close();
    }
}
