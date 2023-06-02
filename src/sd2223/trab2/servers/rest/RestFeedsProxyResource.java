package sd2223.trab2.servers.rest;
import sd2223.trab2.api.rest.FeedsService;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.servers.mastodon.MastodonFeeds;

public class RestFeedsProxyResource extends RestFeedsResource<Feeds> implements FeedsService {

        public RestFeedsProxyResource() {
            super(MastodonFeeds.getInstance());
        }
}
