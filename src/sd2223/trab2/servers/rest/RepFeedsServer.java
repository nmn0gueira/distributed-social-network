package sd2223.trab2.servers.rest;

import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.Domain;
import utils.Args;


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
        Args.use( args );
        Domain.set( args[0], Long.valueOf(args[1]));
        new RepFeedsServer().start();
    }
}