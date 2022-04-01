/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Permissions to access the endpoint.
 * @since 0.1
 */
public interface AccessPermissions {

    /**
     * Obtain the list of permissions, required to access the endpoint.
     * @param line Request line
     * @param method Request method
     * @return Permissions list
     */
    Collection<String> get(String line, String method);

    /**
     * Permissions to access the endpoint from yaml file.
     * @since 0.1
     */
    final class FromYaml implements AccessPermissions {

        /**
         * Access permissions map: the pair of patterns to match
         * request line and request method and permissions list.
         */
        private final Map<Pair<Pattern, Pattern>, List<String>> yaml;

        /**
         * Ctor.
         * @param yaml Access permissions yaml
         */
        public FromYaml(final YamlMapping yaml) {
            this.yaml = FromYaml.read(yaml);
        }

        @Override
        public Collection<String> get(final String line, final String method) {
            return Stream.concat(
                this.yaml.entrySet().stream().filter(
                    entry -> {
                        final Pair<Pattern, Pattern> pair = entry.getKey();
                        return pair.getKey().matcher(line).matches()
                            && pair.getValue().matcher(method).matches();
                    }
                ).map(Map.Entry::getValue).collect(Collectors.toList())
            ).collect(Collectors.toList());
        }

        /**
         * Reads yaml mapping into the map of the pair of patterns to match
         * request line and request method and permissions list.
         * @param yaml Yaml to read
         * @return The access permissions map
         */
        private static Map<Pair<Pattern, Pattern>, List<String>> read(final YamlMapping yaml) {
            final Map<Pair<Pattern, Pattern>, List<String>> res = new HashMap<>(yaml.keys().size());
            for (final YamlNode line : yaml.keys()) {
                final YamlMapping mapping = yaml.yamlMapping(line);
                for (final YamlNode method : mapping.keys()) {
                    res.put(
                        Pair.of(
                            Pattern.compile(FromYaml.unquote(line.asScalar().value())),
                            Pattern.compile(FromYaml.unquote(method.asScalar().value()))
                        ),
                        mapping.yamlSequence(method).values().stream().map(
                            node -> node.asScalar().value()
                        ).collect(Collectors.toList())
                    );
                }
            }
            return res;
        }

        /**
         * Removes extra quotes.
         * @param val Value from yaml
         * @return Unquoted string
         */
        private static String unquote(final String val) {
            return val.replaceAll("^[\"']", "").replaceAll("[\"']$", "");
        }
    }
}
