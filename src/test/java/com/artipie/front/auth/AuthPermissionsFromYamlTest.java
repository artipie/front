/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.Yaml;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AuthPermissions.FromYaml}.
 * @since 0.1
 */
class AuthPermissionsFromYamlTest {

    @Test
    void findsPermission() {
        final String uid = "jane";
        final String prm = "read-repo";
        MatcherAssert.assertThat(
            new AuthPermissions.FromYaml(
                Yaml.createYamlMappingBuilder()
                    .add(uid, Yaml.createYamlSequenceBuilder().add(prm).add("write-repo").build())
                    .add("olga", Yaml.createYamlSequenceBuilder().add(prm).build()).build()
            ).allowed(uid, prm),
            new IsEqual<>(true)
        );
    }

    @Test
    void returnsFalseWhenYamlIsEmpty() {
        MatcherAssert.assertThat(
            new AuthPermissions.FromYaml(Yaml.createYamlMappingBuilder().build())
                .allowed("any", "write"),
            new IsEqual<>(false)
        );
    }

    @Test
    void returnsFalseWhenPermissionNotFound() {
        final String uid = "mark";
        final String prm = "users-write";
        MatcherAssert.assertThat(
            new AuthPermissions.FromYaml(
                Yaml.createYamlMappingBuilder()
                    .add(
                        uid,
                        Yaml.createYamlSequenceBuilder()
                            .add("storages-read")
                            .add("repo-read")
                            .add("users-read").build()
                    ).build()
            ).allowed(uid, prm),
            new IsEqual<>(false)
        );
    }

}
