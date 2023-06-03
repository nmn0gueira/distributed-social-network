package sd2223.trab2.servers.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.LoggerFactory;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.Domain;
import utils.Args;

import java.util.logging.Logger;


public class RepFeedsServer extends AbstractRestServer {
    public static final int PORT = 15678;

    private static final Logger Log = Logger.getLogger(RepFeedsServer.class.getName());

    RepFeedsServer() {
        super( Log, Feeds.SERVICENAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register(new RestFeedsRepResource());
    }

    public static void main(String[] args) throws Exception {
        // Disable Kafka logger
        ch.qos.logback.classic.Logger kafkaLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.kafka");
        kafkaLogger.setLevel(ch.qos.logback.classic.Level.OFF);

        // Disable ZooKeeper logger
        ch.qos.logback.classic.Logger zookeeperLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.zookeeper");
        zookeeperLogger.setLevel(ch.qos.logback.classic.Level.OFF);

        Args.use( args );
        Domain.set( args[0], Long.valueOf(args[1]));
        new RepFeedsServer().start();
    }
}