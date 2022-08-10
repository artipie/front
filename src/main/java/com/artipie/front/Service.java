/*
 * The MIT License (MIT) Copyright (c) 2022 artipie.com
 * https://github.com/artipie/front/LICENSE.txt
 */
package com.artipie.front;

import com.amihaiemil.eoyaml.Yaml;
import com.artipie.front.api.ApiAuthFilter;
import com.artipie.front.api.NotFoundException;
import com.artipie.front.api.PostToken;
import com.artipie.front.api.Repositories;
import com.artipie.front.api.RepositoryPermissions;
import com.artipie.front.api.Storages;
import com.artipie.front.api.Users;
import com.artipie.front.auth.AccessFilter;
import com.artipie.front.auth.ApiTokens;
import com.artipie.front.auth.AuthByPassword;
import com.artipie.front.auth.Credentials;
import com.artipie.front.internal.HealthRoute;
import com.artipie.front.misc.RequestPath;
import com.artipie.front.settings.ArtipieYaml;
import com.artipie.front.settings.RepoData;
import com.artipie.front.settings.RepoSettings;
import com.artipie.front.settings.YamlRepoPermissions;
import com.artipie.front.ui.HbTemplateEngine;
import com.artipie.front.ui.PostSignIn;
import com.artipie.front.ui.RepoPage;
import com.artipie.front.ui.SignInPage;
import com.artipie.front.ui.UserPage;
import com.fasterxml.jackson.core.JsonParseException;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.json.Json;
import javax.json.JsonException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.ExceptionHandler;

/**
 * Front service.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @checkstyle JavaNCSSCheck (500 lines)
 * @checkstyle MethodLengthCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength"})
public final class Service {

    /**
     * Name for argument port for service.
     */
    private static final Option PORT = new Option(
        "p", "port", true, "service port. Should be int value. Default value 8080"
    );

    /**
     * Name for argument config file for service.
     */
    private static final Option CONFIG = Option.builder().option("c").longOpt("config")
        .hasArg(true).desc("The path to artipie configuration file").required(true).build();

    /**
     * Api tokens.
     */
    private final ApiTokens tkn;

    /**
     * Artipie configuration.
     */
    @SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
    private final ArtipieYaml settings;

    /**
     * Spark service instance.
     */
    private volatile spark.Service ignite;

    /**
     * Service constructor.
     * @param tkn Api tokens
     * @param settings Artipie configuration
     */
    Service(final ApiTokens tkn, final ArtipieYaml settings) {
        this.tkn = tkn;
        this.settings = settings;
    }

    /**
     * Entry point.
     * @param args CLA
     * @throws ParseException If cli line argument has wrong format
     */
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public static void main(final String... args) throws ParseException {
        final Options options = new Options();
        options.addOption(Service.PORT);
        options.addOption(Service.CONFIG);
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            final var service = new Service(
                new ApiTokens(DigestUtils.sha1(System.getenv("TKN_KEY")), new Random()),
                new ArtipieYaml(
                    Yaml.createYamlInput(new File(cmd.getOptionValue(Service.CONFIG)))
                        .readYamlMapping()
                )
            );
            service.start(Integer.parseInt(cmd.getOptionValue(Service.PORT, "8080")));
            Runtime.getRuntime().addShutdownHook(new Thread(service::stop, "shutdown"));
        } catch (final ParseException ex) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("com.artipie.front.Service", options);
            throw ex;
        } catch (final IOException ex) {
            Logger.error(Service.class, "Failed to read artipie setting yaml");
            System.exit(1);
        }
    }

    /**
     * Start service.
     * @param port Port for service
     */
    void start(final int port) {
        if (this.ignite != null) {
            throw new IllegalStateException("already started");
        }
        Logger.info(this, "starting service on port: %d", port);
        this.ignite = spark.Service.ignite().port(port);
        this.ignite.get("/.health", new HealthRoute());
        final Credentials creds = this.settings.credentials();
        this.ignite.post(
            "/token",
            new PostToken(AuthByPassword.withCredentials(creds), this.tkn)
        );
        this.ignite.path(
            "/api", () -> {
                this.ignite.before(
                    "/*", new ApiAuthFilter(new ApiAuthFilter.ApiTokenValidator(this.tkn))
                );
                this.ignite.before(
                    "/*",
                    new AccessFilter(
                        this.settings.accessPermissions(), this.settings.userPermissions()
                    )
                );
                this.ignite.path(
                    "/repositories", () -> {
                        final RepoSettings stn = new RepoSettings(
                            this.settings.layout(), this.settings.repoConfigsStorage()
                        );
                        this.ignite.get(
                            "", MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Repositories.GetAll(stn)
                        );
                        final RequestPath path = new RequestPath().with(Repositories.REPO_PARAM);
                        this.ignite.get(
                            path.toString(), MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Repositories.Get(stn)
                        );
                        this.ignite.head(path.toString(), new Repositories.Head(stn));
                        this.ignite.delete(
                            path.toString(),
                            new Repositories.Delete(stn, new RepoData(stn))
                        );
                        this.ignite.put(path.toString(), new Repositories.Put(stn));
                        this.ignite.put(
                            path.with("move").toString(),
                            new Repositories.Move(stn, new RepoData(stn))
                        );
                        final RequestPath repo = this.repoPath();
                        this.ignite.get(
                            repo.with("permissions").toString(),
                            new RepositoryPermissions.Get(
                                new YamlRepoPermissions(this.settings.repoConfigsStorage())
                            )
                        );
                        this.ignite.put(
                            repo.with("permissions").with(RepositoryPermissions.NAME).toString(),
                            new RepositoryPermissions.Put(
                                new YamlRepoPermissions(this.settings.repoConfigsStorage())
                            )
                        );
                        this.ignite.delete(
                            repo.with("permissions").with(RepositoryPermissions.NAME).toString(),
                            new RepositoryPermissions.Delete(
                                new YamlRepoPermissions(this.settings.repoConfigsStorage())
                            )
                        );
                        this.ignite.patch(
                            repo.with("permissions").toString(),
                            new RepositoryPermissions.Patch(
                                new YamlRepoPermissions(this.settings.repoConfigsStorage())
                            )
                        );
                        this.ignite.get(
                            repo.with("storages").toString(),
                            MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Storages.GetAll(this.settings.repoConfigsStorage())
                        );
                        this.ignite.get(
                            repo.with("storages").with(Storages.ST_ALIAS).toString(),
                            new Storages.Get(this.settings.repoConfigsStorage())
                        );
                        this.ignite.head(
                            repo.with("storages").with(Storages.ST_ALIAS).toString(),
                            new Storages.Head(this.settings.repoConfigsStorage())
                        );
                        this.ignite.delete(
                            repo.with("storages").with(Storages.ST_ALIAS).toString(),
                            new Storages.Delete(this.settings.repoConfigsStorage())
                        );
                        this.ignite.put(
                            repo.with("storages").with(Storages.ST_ALIAS).toString(),
                            new Storages.Put(this.settings.repoConfigsStorage())
                        );
                    }
                );
                this.ignite.path(
                    "/storages", () -> {
                        final RequestPath usr = this.userPath();
                        this.ignite.get(
                            usr.toString(), MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Storages.GetAll(this.settings.repoConfigsStorage())
                        );
                        this.ignite.get(
                            usr.with(Storages.ST_ALIAS).toString(),
                            MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Storages.Get(this.settings.repoConfigsStorage())
                        );
                        this.ignite.head(
                            usr.with(Storages.ST_ALIAS).toString(),
                            new Storages.Head(this.settings.repoConfigsStorage())
                        );
                        this.ignite.delete(
                            usr.with(Storages.ST_ALIAS).toString(),
                            new Storages.Delete(this.settings.repoConfigsStorage())
                        );
                        this.ignite.put(
                            usr.with(Storages.ST_ALIAS).toString(),
                            new Storages.Put(this.settings.repoConfigsStorage())
                        );
                    }
                );
                this.ignite.path(
                    "/users", () -> {
                        this.ignite.get(
                            "", MimeTypes.Type.APPLICATION_JSON.asString(),
                            new Users.GetAll(this.settings.users())
                        );
                        final String path = new RequestPath().with(Users.USER_PARAM).toString();
                        this.ignite.get(path, new Users.GetUser(this.settings.users()));
                        this.ignite.put(path, new Users.Put(this.settings.users(), creds));
                        this.ignite.head(path, new Users.Head(this.settings.users()));
                        this.ignite.delete(path, new Users.Delete(this.settings.users(), creds));
                    }
                );
            }
        );
        final var engine = new HbTemplateEngine("/html");
        this.ignite.path(
            "/signin",
            () -> {
                this.ignite.get(
                    "", MimeTypes.Type.APPLICATION_JSON.asString(),
                    new SignInPage(), engine
                );
                this.ignite.post("", new PostSignIn(AuthByPassword.withCredentials(creds)));
            }
        );
        this.ignite.path(
            "/dashboard",
            () -> {
                final RepoSettings stn = new RepoSettings(
                    this.settings.layout(), this.settings.repoConfigsStorage()
                );
                this.ignite.get("", new UserPage(stn), engine);
                this.ignite.get(
                    new RequestPath().with(Users.USER_PARAM).toString(), new UserPage(stn), engine
                );
                this.ignite.get(
                    new RequestPath().with(Users.USER_PARAM)
                        .with(Repositories.REPO_PARAM).toString(),
                    new RepoPage.TemplateView(stn), engine
                );
                this.ignite.post(
                    new RequestPath().with("api").with("repos").with(Users.USER_PARAM).toString(),
                    new RepoPage.Post(stn)
                );
                this.ignite.get(
                    new RequestPath().with("api").with("repos").with(Users.USER_PARAM).toString(),
                    new RepoPage.Get()
                );
            }
        );
        this.ignite.before(AuthFilters.AUTHENTICATE);
        this.ignite.before(AuthFilters.SESSION_ATTRS);
        this.ignite.exception(NotFoundException.class, Service.error(HttpStatus.NOT_FOUND_404));
        this.ignite.exception(JsonException.class, Service.error(HttpStatus.BAD_REQUEST_400));
        this.ignite.exception(JsonParseException.class, Service.error(HttpStatus.BAD_REQUEST_400));
        this.ignite.awaitInitialization();
        Logger.info(this, "service started on port: %d", this.ignite.port());
    }

    /**
     * Stop service.
     */
    void stop() {
        Logger.info(this, "stopping service");
        this.ignite.stop();
        this.ignite.awaitStop();
        Logger.info(this, "service stopped");
    }

    /**
     * Returns repository name path parameter. When artipie layout is org, repository name
     * has username path prefix: uname/reponame.
     * @return Repository name path parameter
     */
    private RequestPath repoPath() {
        RequestPath res = new RequestPath().with(Repositories.REPO_PARAM);
        if ("org".equals(this.settings.layout())) {
            res = new RequestPath().with(Users.USER_PARAM).with(Repositories.REPO_PARAM);
        }
        return res;
    }

    /**
     * Returns username path parameter. When artipie layout is org, username
     * is required, otherwise - not.
     * @return Username path parameter
     */
    private RequestPath userPath() {
        RequestPath res = new RequestPath();
        if ("org".equals(this.settings.layout())) {
            res = res.with(Users.USER_PARAM);
        }
        return res;
    }

    /**
     * Handle exceptions by writing error in json body and returning
     * provided status.
     * @param status Status to return
     * @return Instance of {@link ExceptionHandler}
     */
    private static ExceptionHandler<Exception> error(final int status) {
        return (ex, rqs, rsp) -> {
            rsp.type(MimeTypes.Type.APPLICATION_JSON.toString());
            rsp.body(
                Json.createObjectBuilder().add("error", ex.getLocalizedMessage())
                    .build().toString()
            );
            rsp.status(status);
        };
    }
}
