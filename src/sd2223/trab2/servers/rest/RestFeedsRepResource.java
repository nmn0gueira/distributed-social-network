package sd2223.trab2.servers.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.java.JavaFeedsPull;
import sd2223.trab2.servers.java.JavaFeedsPush;
import sd2223.trab2.servers.java.JavaFeedsRep;
import utils.Args;

@Singleton
public class RestFeedsRepResource extends RestResource implements FeedsServiceRep {

    final protected Feeds impl;

    private final static Logger Log = Logger.getLogger(RestFeedsRepResource.class.getName());

    public RestFeedsRepResource() {
        this.impl = Args.valueOf("-push", true) ? new JavaFeedsRep<>(new JavaFeedsPush()) : new JavaFeedsRep<>(new JavaFeedsPull());
    }


    @Override
    public long postMessage(Long version, String user, String pwd, Message msg) {
        return fromJavaResult(impl.postMessage(user, pwd, msg), version);
    }

    @Override
    public void removeFromPersonalFeed(Long version, String user, long mid, String pwd) {
        fromJavaResult(impl.removeFromPersonalFeed(user, mid, pwd), version);
    }

    @Override
    public Message getMessage(Long version, String user, long mid) {
        return fromJavaResult(impl.getMessage(user, mid), version);
    }

    @Override
    public List<Message> getMessages(Long version, String user, long time) {
        return fromJavaResult(impl.getMessages(user, time), version);
    }

    @Override
    public void subUser(Long version, String user, String userSub, String pwd) {
        fromJavaResult(impl.subUser(user, userSub, pwd), version);
    }

    @Override
    public void unsubscribeUser(Long version, String user, String userSub, String pwd) {
        fromJavaResult(impl.unsubscribeUser(user, userSub, pwd), version);
    }

    @Override
    public List<String> listSubs(Long version, String user) {
        return fromJavaResult(impl.listSubs(user), version);
    }

    @Override
    public void deleteUserFeed(Long version, String user) {
        fromJavaResult(impl.deleteUserFeed(user), version);
    }
}
