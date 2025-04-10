package sd2223.trab2.servers.soap;

import jakarta.jws.WebService;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.soap.FeedsException;
import sd2223.trab2.api.soap.push.FeedsService;
import sd2223.trab2.servers.java.JavaFeedsPush;

@WebService(serviceName=FeedsService.NAME, targetNamespace=FeedsService.NAMESPACE, endpointInterface=FeedsService.INTERFACE)
public class SoapFeedsPushWebService extends SoapFeedsWebService<FeedsPush> implements FeedsService {

	public SoapFeedsPushWebService() {
		super( new JavaFeedsPush() );
	}

	@Override
	public void push_PushMessage(PushMessage msg) throws FeedsException {
		super.fromJavaResult( impl.push_PushMessage(msg));
	}

	@Override
	public void push_updateFollowers(String user, String follower, boolean following) throws FeedsException{
		super.fromJavaResult( impl.push_updateFollowers(user, follower, following));
	}

}