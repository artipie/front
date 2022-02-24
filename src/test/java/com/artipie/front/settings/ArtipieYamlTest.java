/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.settings;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.artipie.asto.blocking.BlockingStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for {@link ArtipieYaml}.
 * @since 0.1
 * @checkstyle MethodNameCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ArtipieYamlTest {

    @Test
    void shouldSetFlatAsDefaultLayout() throws Exception {
        MatcherAssert.assertThat(
            new ArtipieYaml(
                Yaml.createYamlInput(String.join("", "meta:\n", "  storage:\n")).readYamlMapping()
            ).layout(),
            new IsEqual<>("flat")
        );
    }

    @Test
    void shouldBeOrgLayout() throws Exception {
        MatcherAssert.assertThat(
            new ArtipieYaml(
                Yaml.createYamlInput(
                    String.join(
                        "",
                        "meta:\n",
                        "  storage: []\n",
                        "  layout: org\n"
                    )
                ).readYamlMapping()
            ).layout(),
            new IsEqual<>("org")
        );
    }

    @Test
    void shouldBuildFileStorageFromSettings() {
        MatcherAssert.assertThat(
            new ArtipieYaml(this.config("some/path")).storage(),
            Matchers.notNullValue()
        );
    }

    @Test
    void shouldBuildS3StorageFromSettings() throws Exception {
        MatcherAssert.assertThat(
            new ArtipieYaml(
                Yaml.createYamlInput(
                    String.join(
                        "",
                        "meta:\n",
                        "  storage:\n",
                        "    type: s3\n",
                        "    bucket: my-bucket\n",
                        "    region: my-region\n",
                        "    endpoint: https://my-s3-provider.com\n",
                        "    credentials:\n",
                        "      type: basic\n",
                        "      accessKeyId: ***\n",
                        "      secretAccessKey: ***"
                    )
                ).readYamlMapping()
            ).storage(),
            Matchers.notNullValue()
        );
    }

    @Test
    void returnsRepoConfigs(@TempDir final Path tmp) {
        MatcherAssert.assertThat(
            new ArtipieYaml(this.config(tmp.toString())).repoConfigsStorage(),
            new IsInstanceOf(BlockingStorage.class)
        );
    }

    @Test
    void shouldReadCredentials(@TempDir final Path tmp) throws IOException {
        final String creds = "_credentials.yaml";
        Files.writeString(
            tmp.resolve(creds),
            YamlCredentialsTest.credYaml(
                YamlCredentialsTest.PasswordFormat.SIMPLE,
                // @checkstyle LineLengthCheck (1 line)
                new YamlCredentialsTest.User("Alice", "plain", "123"),
                new YamlCredentialsTest.User("John", "sha256", "xxx")
            ).toString()
        );
        MatcherAssert.assertThat(
            new ArtipieYaml(this.config(tmp.toString(), Optional.of(creds)))
                .credentialsYaml().isPresent(),
            new IsEqual<>(true)
        );
    }

    private YamlMapping config(final String stpath) {
        return this.config(stpath, Optional.empty());
    }

    private YamlMapping config(final String stpath, final Optional<String> creds) {
        YamlMappingBuilder meta = Yaml.createYamlMappingBuilder()
            .add(
                "storage",
                Yaml.createYamlMappingBuilder()
                    .add("type", "fs")
                    .add("path", stpath).build()
            )
            .add("repo_configs", "repos");
        if (creds.isPresent()) {
            meta = meta.add(
                "credentials",
                Yaml.createYamlSequenceBuilder()
                    .add(Yaml.createYamlMappingBuilder().add("type", "env").build())
                    .add(
                        Yaml.createYamlMappingBuilder()
                            .add("type", "file")
                            .add("path", creds.get()).build()
                    ).build()
            );
        }
        return Yaml.createYamlMappingBuilder().add("meta", meta.build()).build();
    }

}
