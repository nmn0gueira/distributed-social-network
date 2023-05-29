package sd2223.trab2.servers.rest;

import jakarta.inject.Singleton;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.rest.FeedsServicePush;
import sd2223.trab2.servers.mastodon.Mastodon;

@Singleton
public class ProxyFeedsResource extends RestFeedsResource<FeedsPush> implements FeedsServicePush {

    public ProxyFeedsResource() {
        super(Mastodon.getInstance());
    }

    @Override
    public void push_PushMessage(PushMessage msg) {
        super.fromJavaResult(impl.push_PushMessage(msg));
    }

    @Override
    public void push_updateFollowers(String user, String follower, boolean following) {
        super.fromJavaResult(impl.push_updateFollowers(user, follower, following));
    }
}
