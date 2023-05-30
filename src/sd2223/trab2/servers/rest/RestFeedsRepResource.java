package sd2223.trab2.servers.rest;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.rest.FeedsServiceRep;

import java.util.List;

public class RestFeedsRepResource extends RestFeedsResource<Feeds> implements FeedsServiceRep {
    public RestFeedsRepResource(Feeds impl) {
        super(impl);
    }

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
