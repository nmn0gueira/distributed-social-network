package sd2223.trab2.servers.java;

//import static sd2223.trab2.api.java.Result.error;
//import static sd2223.trab2.api.java.Result.ok;

import java.util.List;
import java.util.Set;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.kafka.KafkaMessage;
import sd2223.trab2.servers.kafka.KafkaPublisher;
import sd2223.trab2.servers.kafka.KafkaSubscriber;
import utils.JSON;

public class JavaRepFeeds extends JavaFeedsPush implements Feeds {

    private static final String KAFKA_BROKERS = "kafka:9092";

    private static final String FROM_BEGINNING = "earliest";

    private static final String TOPIC = Domain.get();

    // Operation constants used in Kafka messages
    private static final String POST_MESSAGE = "postMessage";
    private static final String REMOVE_FROM_PERSONAL_FEED = "removeFromPersonalFeed";
    private static final String DELETE_FROM_USER_FEED = "deleteFromUserFeed";
    private static final String GET_MESSAGE = "getMessage";
    private static final String GET_MESSAGES = "getMessages";
    private static final String SUB_USER = "subUser";
    private static final String UNSUBSCRIBE_USER = "unsubscribeUser";
    private static final String LIST_SUBS = "listSubs";
    private static final String DELETE_USER_FEED = "deleteUserFeed";

    private KafkaPublisher publisher;

    private KafkaSubscriber subscriber;


    public JavaRepFeeds( ) {
        super();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            System.out.printf("SeqN: %s %d %s\n", r.topic(), r.offset(), r.value());
            var message = JSON.decode(r.value(), KafkaMessage.class);
            List args = message.getArguments();
            switch (message.getOp()) {
                case POST_MESSAGE:
                    String user = (String) args.get(0);
                    String pwd = (String) args.get(1);
                    Message msg = (Message) args.get(2);
                    super.postMessage(user, pwd, msg);
                    break;
                case REMOVE_FROM_PERSONAL_FEED:
                    user = (String) args.get(0);
                    long mid = (long) args.get(1);
                    pwd = (String) args.get(2);
                    super.removeFromPersonalFeed(user, mid, pwd);
                    break;
                case DELETE_FROM_USER_FEED:
                    user = (String) args.get(0);
                    Set<Long> mids = (Set<Long>) args.get(1);
                    super.deleteFromUserFeed(user, mids);
                    break;
                case GET_MESSAGE:
                    user = (String) args.get(0);
                    mid = (Long) args.get(1);
                    super.getMessage(user, mid);
                    break;
                case GET_MESSAGES:
                    user = (String) args.get(0);
                    long time = (long) args.get(1);
                    super.getMessages(user, time);
                    break;
                case SUB_USER:
                    user = (String) args.get(0);
                    String userSub = (String) args.get(1);
                    pwd = (String) args.get(2);
                    super.subUser(user, userSub, pwd);
                    break;
                case UNSUBSCRIBE_USER:
                    user = (String) args.get(0);
                    userSub = (String) args.get(1);
                    pwd = (String) args.get(2);
                    super.unsubscribeUser(user, userSub, pwd);
                    break;
                case LIST_SUBS:
                    user = (String) args.get(0);
                    super.listSubs(user);
                    break;
                case DELETE_USER_FEED:
                    user = (String) args.get(0);
                    super.deleteUserFeed(user);
                    break;
            }
        });
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var res = super.preconditions.postMessage(user, pwd, msg);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(POST_MESSAGE, user, pwd, msg);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        var res = super.preconditions.removeFromPersonalFeed(user, mid, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(REMOVE_FROM_PERSONAL_FEED, user, mid, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    protected void deleteFromUserFeed(String user, Set<Long> mids) { // Não há precondições aqui?
            KafkaMessage message = new KafkaMessage(DELETE_FROM_USER_FEED, user, mids);
            publisher.publish(TOPIC, JSON.encode(message));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        var res = super.preconditions.getMessage(user, mid);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(GET_MESSAGE, user, mid);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        var res = super.preconditions.getMessages(user, time);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(GET_MESSAGES, user, time);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        var res = super.preconditions.subUser(user, userSub, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(SUB_USER, user, userSub, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        var res = super.preconditions.unsubscribeUser(user, userSub, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(UNSUBSCRIBE_USER, user, userSub, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        var res = super.preconditions.listSubs(user);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(LIST_SUBS, user);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        var res = super.preconditions.deleteUserFeed(user);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(DELETE_USER_FEED, user);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }
}
