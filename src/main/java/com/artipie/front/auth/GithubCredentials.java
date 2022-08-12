/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front.auth;

import com.artipie.ArtipieException;
import com.jcabi.github.RtGithub;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GitHub authentication uses username prefixed by provider name {@code github.com}
 * and personal access token as a password.
 * See <a href="https://developer.github.com/v3/oauth_authorizations/">GitHub docs</a>
 * for details.
 * @implNote This implementation is not case sensitive.
 * @since 0.2
 */
public final class GithubCredentials implements Credentials {

    /**
     * Username pattern, starts with provider name {@code github.com}, slash,
     * and GitHub username, e.g. {@code github.com/octocat}.
     */
    private static final Pattern PTN_NAME = Pattern.compile("^github\\.com/(.+)$");

    /**
     * Github username resolver by personal access token.
     */
    private final Function<String, String> github;

    /**
     * New GitHub authentication.
     * @checkstyle ReturnCountCheck (10 lines)
     */
    public GithubCredentials() {
        this(
            token -> {
                try {
                    return new RtGithub(token).users().self().login();
                } catch (final IOException unauthorized) {
                    return "";
                }
            }
        );
    }

    /**
     * Primary constructor.
     * @param github Resolves GitHub token to username
     */
    GithubCredentials(final Function<String, String> github) {
        this.github = github;
    }

    @Override
    public Optional<User> user(final String username) {
        Optional<User> result = Optional.empty();
        final Matcher matcher = GithubCredentials.PTN_NAME.matcher(username);
        if (matcher.matches()) {
            result = Optional.of(new GithubUser(matcher.group(1)));
        }
        return result;
    }

    @Override
    public void reload() {
        // does nothing
    }

    @Override
    public String toString() {
        return String.format("%s()", this.getClass().getSimpleName());
    }

    /**
     * Implementation of {@link User} for github user.
     * @since 0.2
     */
    class GithubUser implements User {

        /**
         * Username.
         */
        private final String uname;

        /**
         * Ctor.
         * @param uname Name of the user
         */
        GithubUser(final String uname) {
            this.uname = uname;
        }

        @Override
        public boolean validatePassword(final String pass) {
            boolean res = false;
            try {
                final String login = GithubCredentials.this.github.apply(pass)
                    .toLowerCase(Locale.US);
                if (login.equalsIgnoreCase(this.uname)) {
                    res = true;
                }
            } catch (final AssertionError error) {
                if (error.getMessage() == null
                    || !error.getMessage().contains("401 Unauthorized")) {
                    throw new ArtipieException(error);
                }
            }
            return res;
        }

        @Override
        public String uid() {
            return this.uname;
        }

        @Override
        public Set<? extends String> groups() {
            return Collections.emptySet();
        }

        @Override
        public Optional<String> email() {
            return Optional.empty();
        }
    }
}
