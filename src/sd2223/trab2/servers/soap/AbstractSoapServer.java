package sd2223.trab2.servers.soap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import sd2223.trab2.discovery.Discovery;
import sd2223.trab2.servers.java.AbstractServer;
import utils.IP;

import javax.net.ssl.SSLContext;

public class AbstractSoapServer<T> extends AbstractServer {
	private static final String SOAP_CTX = "/soap";

	protected final int port;

	final T webservice;
	
	protected AbstractSoapServer( boolean enableSoapDebug, Logger log, String service, int port, T webservice) {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostName(), port, SOAP_CTX));
		this.port = port;
		this.webservice = webservice;
		
		if(enableSoapDebug ) {
			System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
		}
	}
	
	protected void start() throws IOException, NoSuchAlgorithmException {
		var server = HttpsServer.create( new InetSocketAddress(IP.hostName(), port), 0 );

		server.setExecutor(Executors.newCachedThreadPool() );
		server.setHttpsConfigurator( new HttpsConfigurator(SSLContext.getDefault() ) );

		var endpoint = Endpoint.create(webservice);
		endpoint.publish(server.createContext(SOAP_CTX));

		server.start();

		Discovery.getInstance().announce(service, serverURI);
		Log.info(String.format("%s Soap Server ready @ %s\n", service, serverURI));
	}
}
