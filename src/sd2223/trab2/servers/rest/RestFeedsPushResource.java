package sd2223.trab2.servers.rest;

import jakarta.inject.Singleton;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.rest.FeedsServicePush;
import sd2223.trab2.servers.java.JavaFeedsPush;
import sd2223.trab2.servers.mastodon.Mastodon;

@Singleton
public class RestFeedsPushResource extends RestFeedsResource<FeedsPush> implements FeedsServicePush {

	public RestFeedsPushResource(boolean proxy) {
		super(proxy ? Mastodon.getInstance() : new JavaFeedsPush());
	}

	@Override
	public void push_PushMessage(PushMessage msg) {
		super.fromJavaResult( impl.push_PushMessage(msg));
	}

	@Override
	public void push_updateFollowers(String user, String follower, boolean following) {
		super.fromJavaResult( impl.push_updateFollowers(user, follower, following));
	}
}
