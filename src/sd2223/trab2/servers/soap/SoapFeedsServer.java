package sd2223.trab2.servers.soap;


import java.util.logging.Logger;

import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.Domain;
import utils.Args;

public class SoapFeedsServer extends AbstractSoapServer<SoapFeedsWebService<?>> {

	public static final int PORT = 14567;
	private static final Logger Log = Logger.getLogger(SoapFeedsServer.class.getName());

	protected SoapFeedsServer() {
		super(false, Log, Feeds.SERVICENAME, PORT,  Args.valueOf("-push", true) ? new SoapFeedsPushWebService() : new SoapFeedsPullWebService() );
	}

	public static void main(String[] args) throws Exception {
		Args.use(args);		
		Domain.set( args[0], Long.parseLong(args[1]));
		new SoapFeedsServer().start();
	}
}
