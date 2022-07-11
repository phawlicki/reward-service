package com.example.reward.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongoCmdOptions;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.IOException;
import java.util.Optional;

@Configuration
public class MongoTestConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(MongoTestConfiguration.class);
    private static final Version MONGO_VERSION = Version.V4_0_12;
    private static final String PROXY_HOST = "192.168.213.30";
    private static final int PROXY_PORT = 3128;

    @Bean
    public MongodConfig mongodConfig() throws IOException {
        Net net = new Net("127.0.0.1", Network.getFreeServerPort(), false);
        return MongodConfig.builder()
                .stopTimeoutInMillis(10L * 1000L)
                .replication(new Storage(null, "rs0", 0))
                .cmdOptions(MongoCmdOptions.builder().useNoJournal(false).build())
                .version(MONGO_VERSION)
                .net(net)
                .build();
    }

    @Bean(destroyMethod = "stop")
    public MongodExecutable mongodExecutable(MongodConfig mongodConfig, RuntimeConfig runtimeConfig) {
        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        return runtime.prepare(mongodConfig);
    }

    @Bean(value = "mongodProcess")
    public MongodProcess mongodProcess(MongodExecutable mongodExecutable) throws IOException {
        MongodProcess process = mongodExecutable.start();
        MongodConfig mongodConfig = mongodConfig();
        MongoClient client = MongoClients.create(String.format("mongodb://%s:%s", mongodConfig.net().getBindIp(),
                mongodConfig.net().getPort()));
        client.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document()));
        return process;
    }

    @Bean
    @DependsOn("mongodProcess")
    public MongoClient mongoClient(RuntimeConfig runtimeConfig) {
        try {
            MongodConfig mongodConfig = mongodConfig();
            mongodProcess(mongodExecutable(mongodConfig, runtimeConfig));
            return MongoClients
                    .create(String.format("mongodb://%s:%s/mongo?replicaSet=rs0", mongodConfig.net().getBindIp(),
                            mongodConfig.net().getPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    public RuntimeConfig embeddedMongoRuntimeConfig() {
        Optional<HttpProxyFactory> httpProxyFactory = defineProxy();
        Logger logger = LoggerFactory.getLogger("de.flapdoodle.embed.EmbeddedMongo");
        ProcessOutput processOutput = new ProcessOutput(
                Processors.logTo(logger, Slf4jLevel.INFO),
                Processors.logTo(logger, Slf4jLevel.INFO), Processors.named(
                "[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)));
        Command command = Command.MongoD;
        return Defaults.runtimeConfigFor(command)
                .processOutput(processOutput)
                .artifactStore(Defaults.extractedArtifactStoreFor(command)
                        .withDownloadConfig(Defaults.downloadConfigFor(command)
                                .proxyFactory(httpProxyFactory)
                                .build()))
                .build();
    }

    private Optional<HttpProxyFactory> defineProxy() {
        String branch_name = System.getenv().get("BRANCH_NAME");
        if (branch_name != null) {
            LOG.info("Tests is running inside jenkins");
            LOG.info("Proxy used to download embedded mongo is: {}:{}", PROXY_HOST, PROXY_PORT);
            return Optional.of(new HttpProxyFactory(PROXY_HOST, PROXY_PORT));
        }
        return Optional.empty();
    }
}
