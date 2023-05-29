package sd2223.trab2.servers.rest;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.Domain;
import utils.Args;

import java.util.logging.Logger;

public class ProxyFeedsServer extends AbstractRestServer{

    public static final int PORT = 5678;

    private static Logger Log = Logger.getLogger(ProxyFeedsServer.class.getName());

    protected ProxyFeedsServer() {
        super( Log, Feeds.SERVICENAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register(RestFeedsPushResource.class);
        config.register(new CustomLoggingFilter());
    }

    public static void main(String[] args) throws Exception {
        Args.use( args);
        Domain.set( args[0], Long.valueOf(args[1]));
        new ProxyFeedsServer().start();
    }
}
