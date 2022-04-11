/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import java.io.IOException;
import javax.json.Json;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Users management API endpoints IT cases.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UsersITCase {

    /**
     * Test deployments.
     * @checkstyle VisibilityModifierCheck (10 lines)
     */
    @RegisterExtension
    final TestService service = new TestService()
        .withResource(TestService.CREDS, "UsersITCase/_credentials.yaml")
        .withResource("_api_permissions.yml", "UsersITCase/_api_permissions.yml");

    /**
     * Test http client.
     */
    private TestClient client;

    @BeforeEach
    void init() {
        this.client = new TestClient(this.service.port());
    }

    @Test
    void canManageUsers() throws JSONException {
        final String aladdin = this.client.token("Aladdin", "opensesame");
        MatcherAssert.assertThat(
            "Failed to obtain auth token for Aladdin",
            aladdin,
            new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Aladdin failed to create new user",
            this.client.put(
                "/api/users/Olga", aladdin,
                Json.createObjectBuilder().add(
                    "Olga",
                    Json.createObjectBuilder().add("type", "plain").add("pass", "123").build()
                ).build().toString()
            ),
            new IsEqual<>(HttpStatus.CREATED_201)
        );
        MatcherAssert.assertThat(
            "Aladdin failed to check Olga exists",
            this.client.get("/api/users/Olga", aladdin),
            new StringContains("Olga")
        );
        MatcherAssert.assertThat(
            "Aladdin failed to delete Alice",
            this.client.delete("/api/users/Alice", aladdin),
            new IsEqual<>(HttpStatus.OK_200)
        );
        MatcherAssert.assertThat(
            "Aladdin failed to check Alice does not exist",
            this.client.head("/api/users/Alice", aladdin),
            new IsEqual<>(HttpStatus.NOT_FOUND_404)
        );
        final String mark = this.client.token("Mark", "123");
        MatcherAssert.assertThat(
            "Failed to obtain auth token for Mark",
            aladdin,
            new IsNot<>(Matchers.emptyString())
        );
        MatcherAssert.assertThat(
            "Mark failed to check Alice does not exist",
            this.client.head("/api/users/Alice", mark),
            new IsEqual<>(HttpStatus.NOT_FOUND_404)
        );
        MatcherAssert.assertThat(
            "Mark failed to get Aladdin info",
            this.client.get("/api/users/Aladdin", mark),
            new StringContains("Aladdin")
        );
        MatcherAssert.assertThat(
            "Mark failed to get Olga's info",
            this.client.get("/api/users/Olga", mark),
            new StringContains("Olga")
        );
        JSONAssert.assertEquals(
            this.client.get("/api/users", mark),
            "{\"Mark\":{}, \"Olga\": {}, \"Aladdin\": {}}", true
        );
    }

    @AfterEach
    void stop() throws IOException {
        this.client.close();
    }
}
