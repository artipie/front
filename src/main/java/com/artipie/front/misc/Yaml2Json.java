/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;
import javax.json.Json;
import javax.json.JsonStructure;

/**
 * Transform yaml to json.
 * @since 0.1
 */
public final class Yaml2Json implements Function<String, JsonStructure> {

    @Override
    public JsonStructure apply(final String yaml) {
        try {
            return Json.createReader(
                new ByteArrayInputStream(
                    new ObjectMapper().writeValueAsBytes(
                        new ObjectMapper(new YAMLFactory()).readValue(yaml, Object.class)
                    )
                )
            ).read();
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
