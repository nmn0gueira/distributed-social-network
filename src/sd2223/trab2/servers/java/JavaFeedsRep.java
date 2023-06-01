package sd2223.trab2.servers.java;

import java.util.List;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.kafka.KafkaMessage;
import sd2223.trab2.servers.kafka.KafkaPublisher;
import sd2223.trab2.servers.kafka.KafkaSubscriber;
import utils.JSON;

public class JavaFeedsRep<T extends JavaFeedsCommon<? extends Feeds>> implements Feeds {

    private final T impl;

    private static final String KAFKA_BROKERS = "kafka:9092";

    private static final String FROM_BEGINNING = "earliest";

    private static final String TOPIC = Domain.get();

    // Operation constants used in Kafka messages
    private static final String POST_MESSAGE = "postMessage";
    private static final String REMOVE_FROM_PERSONAL_FEED = "removeFromPersonalFeed";
    private static final String GET_MESSAGE = "getMessage";
    private static final String GET_MESSAGES = "getMessages";
    private static final String SUB_USER = "subUser";
    private static final String UNSUBSCRIBE_USER = "unsubscribeUser";
    private static final String LIST_SUBS = "listSubs";
    private static final String DELETE_USER_FEED = "deleteUserFeed";

    private final KafkaPublisher publisher;


    public JavaFeedsRep(T impl) {
        super();
        this.impl = impl;
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            System.out.printf("SeqN: %s %d %s\n", r.topic(), r.offset(), r.value());
            var message = JSON.decode(r.value(), KafkaMessage.class);
            List<Object> args = message.getArguments();
            switch (message.getOp()) {
                case POST_MESSAGE:
                    String user = (String) args.get(0);
                    String pwd = (String) args.get(1);
                    Message msg = (Message) args.get(2);
                    impl.postMessage(user, pwd, msg);
                    break;
                case REMOVE_FROM_PERSONAL_FEED:
                    user = (String) args.get(0);
                    long mid = (long) args.get(1);
                    pwd = (String) args.get(2);
                    impl.removeFromPersonalFeed(user, mid, pwd);
                    break;
                case GET_MESSAGE:
                    user = (String) args.get(0);
                    mid = (Long) args.get(1);
                    impl.getMessage(user, mid);
                    break;
                case GET_MESSAGES:
                    user = (String) args.get(0);
                    long time = (long) args.get(1);
                    impl.getMessages(user, time);
                    break;
                case SUB_USER:
                    user = (String) args.get(0);
                    String userSub = (String) args.get(1);
                    pwd = (String) args.get(2);
                    impl.subUser(user, userSub, pwd);
                    break;
                case UNSUBSCRIBE_USER:
                    user = (String) args.get(0);
                    userSub = (String) args.get(1);
                    pwd = (String) args.get(2);
                    impl.unsubscribeUser(user, userSub, pwd);
                    break;
                case LIST_SUBS:
                    user = (String) args.get(0);
                    impl.listSubs(user);
                    break;
                case DELETE_USER_FEED:
                    user = (String) args.get(0);
                    impl.deleteUserFeed(user);
                    break;
            }
        });
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var res = impl.preconditions.postMessage(user, pwd, msg);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(POST_MESSAGE, user, pwd, msg);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        var res = impl.preconditions.removeFromPersonalFeed(user, mid, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(REMOVE_FROM_PERSONAL_FEED, user, mid, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        var res = impl.preconditions.getMessage(user, mid);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(GET_MESSAGE, user, mid);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        var res = impl.preconditions.getMessages(user, time);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(GET_MESSAGES, user, time);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        var res = impl.preconditions.subUser(user, userSub, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(SUB_USER, user, userSub, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        var res = impl.preconditions.unsubscribeUser(user, userSub, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(UNSUBSCRIBE_USER, user, userSub, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        var res = impl.preconditions.listSubs(user);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(LIST_SUBS, user);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        var res = impl.preconditions.deleteUserFeed(user);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(DELETE_USER_FEED, user);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }
}
