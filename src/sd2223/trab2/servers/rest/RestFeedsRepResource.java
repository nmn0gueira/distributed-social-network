package sd2223.trab2.servers.rest;

import java.util.List;

import jakarta.inject.Singleton;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.java.JavaFeedsPull;
import sd2223.trab2.servers.java.JavaFeedsPush;
import sd2223.trab2.servers.java.JavaFeedsRep;
import sd2223.trab2.servers.kafka.sync.SyncPoint;
import utils.Args;

@Singleton
public class RestFeedsRepResource extends RestFeedsResource<Feeds> implements FeedsServiceRep {

    public RestFeedsRepResource() {
        super(Args.valueOf("-push", true) ? new JavaFeedsRep<>(new JavaFeedsPush()) : new JavaFeedsRep<>(new JavaFeedsPull()));
    }

    final protected SyncPoint<?> syncPoint = SyncPoint.getInstance();

    @Override
    public long postMessage(Long version, String user, String pwd, Message msg) {
        return 0;
    }

    @Override
    public void removeFromPersonalFeed(Long version, String user, long mid, String pwd) {

    }

    @Override
    public Message getMessage(Long version, String user, long mid) {
        return null;
    }

    @Override
    public List<Message> getMessages(Long version, String user, long time) {
        return null;
    }

    @Override
    public void subUser(Long version, String user, String userSub, String pwd) {

    }

    @Override
    public void unsubscribeUser(Long version, String user, String userSub, String pwd) {

    }

    @Override
    public List<String> listSubs(Long version, String user) {
        return null;
    }

    @Override
    public void deleteUserFeed(Long version, String user) {

    }
}
