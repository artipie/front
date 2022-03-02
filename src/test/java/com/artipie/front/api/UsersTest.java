/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.api;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.front.auth.YamlCredentialsTest;
import com.artipie.front.settings.ArtipieYaml;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import spark.Request;
import spark.Response;

/**
 * Test for {@link Users}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UsersTest {

    /**
     * Credentials file.
     */
    private static final String CREDS_YAML = "creds.yaml";

    /**
     * Test temp dir.
     * @checkstyle VisibilityModifierCheck (10 lines)
     */
    @TempDir
    Path temp;

    /**
     * Test ArtipieYaml instance.
     */
    private ArtipieYaml artipie;

    @BeforeEach
    void init() {
        this.artipie = new ArtipieYaml(
            Yaml.createYamlMappingBuilder().add(
                "meta",
                Yaml.createYamlMappingBuilder()
                    .add(
                        "storage",
                        Yaml.createYamlMappingBuilder()
                            .add("type", "fs")
                            .add("path", this.temp.toString()).build()
                    )
                    .add(
                        "credentials",
                        Yaml.createYamlMappingBuilder().add("type", "file")
                            .add("path", UsersTest.CREDS_YAML).build()
                    ).build()
            ).build()
        );
    }

    @Test
    void writesUsers() throws JSONException, IOException {
        Files.writeString(
            this.temp.resolve(UsersTest.CREDS_YAML),
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                // @checkstyle LineLengthCheck (1 line)
                new YamlCredentialsTest.User("Alice", "plain", "123", Optional.of("alice@example.com")),
                new YamlCredentialsTest.User("John", "sha256", "xxx", "reader", "dev-lead"),
                new YamlCredentialsTest.User("Mark", "sha256", "xxx")
            ).toString()
        );
        JSONAssert.assertEquals(
            new Users(this.artipie)
                .handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            String.join(
                "\n",
                "{",
                "  \"Alice\": {\"email\":\"alice@example.com\"},",
                "  \"John\": {\"groups\":[\"reader\",\"dev-lead\"]},",
                "  \"Mark\": {} ",
                "}"
            ),
            true
        );
    }

    @Test
    void writesEmptyWhenAbsent() throws JSONException {
        JSONAssert.assertEquals(
            new Users(this.artipie)
                .handle(Mockito.mock(Request.class), Mockito.mock(Response.class)),
            "{}",
            true
        );
    }

}
