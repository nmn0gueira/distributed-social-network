package sd2223.trab2.servers.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.java.JavaFeedsPull;
import sd2223.trab2.servers.java.JavaFeedsPush;
import sd2223.trab2.servers.java.JavaFeedsRep;
import sd2223.trab2.servers.kafka.sync.SyncPoint;
import utils.Args;

@Singleton
public class RestFeedsRepResource extends RestResource implements FeedsServiceRep {

    final protected Feeds impl;

    private final static Logger Log = Logger.getLogger(RestFeedsRepResource.class.getName());

    public RestFeedsRepResource() {
        this.impl = Args.valueOf("-push", true) ? new JavaFeedsRep<>(new JavaFeedsPush()) : new JavaFeedsRep<>(new JavaFeedsPull());
    }

    final private SyncPoint<?> syncPoint = SyncPoint.getInstance();



    @Override
    public long postMessage(Long version, String user, String pwd, Message msg) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.postMessage(user, pwd, msg), version);
    }

    @Override
    public void removeFromPersonalFeed(Long version, String user, long mid, String pwd) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        fromJavaResult(impl.removeFromPersonalFeed(user, mid, pwd), version);
    }

    @Override
    public Message getMessage(Long version, String user, long mid) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.getMessage(user, mid));
    }

    @Override
    public List<Message> getMessages(Long version, String user, long time) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.getMessages(user, time));
    }

    @Override
    public void subUser(Long version, String user, String userSub, String pwd) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        fromJavaResult(impl.subUser(user, userSub, pwd), version);
    }

    @Override
    public void unsubscribeUser(Long version, String user, String userSub, String pwd) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        fromJavaResult(impl.unsubscribeUser(user, userSub, pwd), version);
    }

    @Override
    public List<String> listSubs(Long version, String user) {
        if (version == null)
            version = -1L;
        version += syncPoint.getOffset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.listSubs(user));
    }

    @Override
    public void deleteUserFeed(Long version, String user) {
        fromJavaResult(impl.deleteUserFeed(user));
    }

    @Override
    public void push_PushMessage(PushMessage msg) {
        fromJavaResult(((FeedsPush) impl).push_PushMessage(msg));
    }

    @Override
    public void push_updateFollowers(String user, String follower, boolean following) {
        fromJavaResult(((FeedsPush) impl).push_updateFollowers(user, follower, following));
    }

    @Override
    public List<Message> pull_getTimeFilteredPersonalFeed(String user, long time) {
        return fromJavaResult(((FeedsPull) impl).pull_getTimeFilteredPersonalFeed(user, time));
    }
}
