package sd2223.trab2.servers.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.Domain;
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
        Log.info("Operation postMessage offset before: " + Domain.offset());
        if (version == null)
            version = -1L;
        System.out.println("version: " + version);
        version += Domain.offset();
        System.out.println("version after offset: " + version);
        Domain.setOffset(0);
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.postMessage(user, pwd, msg), version);
    }

    @Override
    public void removeFromPersonalFeed(Long version, String user, long mid, String pwd) {
        Log.info("Operation removeFromPersonalFeed offset before: " + Domain.offset());
        if (version == null)
            version = -1L;
        version += Domain.offset();
        Domain.setOffset(0);
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        fromJavaResult(impl.removeFromPersonalFeed(user, mid, pwd), version);
    }

    @Override
    public Message getMessage(Long version, String user, long mid) {
        if (version == null)
            version = -1L;
        version += Domain.offset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.getMessage(user, mid));
    }

    @Override
    public List<Message> getMessages(Long version, String user, long time) {
        Log.info("Operation getMessages offset before: " + Domain.offset());
        if (version == null)
            version = -1L;
        version += Domain.offset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.getMessages(user, time));
    }

    @Override
    public void subUser(Long version, String user, String userSub, String pwd) {
        Log.info("Operation subUser offset before: " + Domain.offset());
        if (version == null)
            version = -1L;
        version += Domain.offset();
        Domain.setOffset(0);
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        fromJavaResult(impl.subUser(user, userSub, pwd), version);
    }

    @Override
    public void unsubscribeUser(Long version, String user, String userSub, String pwd) {
        Log.info("Operation unsubscribeUser offset before: " + Domain.offset());
        if (version == null)
            version = -1L;
        version += Domain.offset();
        Domain.setOffset(0);
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        fromJavaResult(impl.unsubscribeUser(user, userSub, pwd), version);
    }

    @Override
    public List<String> listSubs(Long version, String user) {
        Log.info("Operation listSubs offset before: " + Domain.offset());
        if (version == null)
            version = -1L;
        version += Domain.offset();
        syncPoint.waitForVersion(version, Integer.MAX_VALUE);
        return fromJavaResult(impl.listSubs(user));
    }

    @Override
    public void deleteUserFeed(Long version, String user) {
        Log.info("Operation deleteUserFeed offset before: " + Domain.offset());
        var res = impl.deleteUserFeed(user);
        Log.info("Operation deleteUserFeed result: " + res);
        fromJavaResult(res);

    }
}
