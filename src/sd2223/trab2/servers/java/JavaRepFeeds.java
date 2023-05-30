package sd2223.trab2.servers.java;

import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;

import java.util.List;
import java.util.Set;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.kafka.KafkaPublisher;
import sd2223.trab2.servers.kafka.KafkaSubscriber;
import utils.JSON;

public class JavaRepFeeds extends JavaFeedsPush implements Feeds {

    private static final String KAFKA_BROKERS = "kafka:9092";

    private static final String FROM_BEGINNING = "earliest";

    private static final String TOPIC = Domain.get();

    private KafkaPublisher publisher;

    private KafkaSubscriber subscriber;


    public JavaRepFeeds( ) {
        super();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            System.out.printf("SeqN: %s %d %s\n", r.topic(), r.offset(), r.value());
            var msg = JSON.decode(r.value(), Message.class);
            super.postMessage(msg.getUser(), null, msg);
        });
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var res = super.preconditions.postMessage(user, pwd, msg);
        if (res.isOK()) {
            publisher.publish(TOPIC, JSON.encode(List.of("postMessage", user, pwd, msg)));
        }
        return res;
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return null;
    }

    @Override
    protected void deleteFromUserFeed(String user, Set<Long> mids) {

    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return null;
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return null;
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return null;
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        return null;
    }
}
