/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import com.artipie.asto.test.TestResource;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Json2Yaml}.
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class Json2YamlTest {

    @Test
    void convertsJsonYoYaml() {
        MatcherAssert.assertThat(
            new Json2Yaml().apply(
                new String(
                    new TestResource("Json2YamlTest/test.json").asBytes(), StandardCharsets.UTF_8
                )
            ).toString(),
            new IsEqual<>(
                String.join(
                    System.lineSeparator(),
                    "repo:",
                    "  type: \"docker-proxy\"",
                    "  remotes:",
                    "    -",
                    "      url: registry1.docker.io",
                    "    -",
                    "      url: mcr.microsoft.com",
                    "  storage:",
                    "    type: fs",
                    "    path: /var/artipie/data/",
                    "  permissions:",
                    "    alice:",
                    "      - read",
                    "    bob:",
                    "      - *",
                    "  url: \"http://artipie:8080/my-proxy-docker\""
                )
            )
        );
    }

}
