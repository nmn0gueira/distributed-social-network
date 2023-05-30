package sd2223.trab2.servers.java;

import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;
import static sd2223.trab2.api.java.Result.ErrorCode.NOT_FOUND;
import static sd2223.trab2.clients.Clients.FeedsPushClients;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.kafka.KafkaPublisher;
import sd2223.trab2.servers.kafka.KafkaSubscriber;
import utils.JSON;

public class JavaFeedsPushRep extends JavaFeedsPush implements FeedsServiceRep {

    private static final String KAFKA_BROKERS = "kafka:9092";

    private static final String FROM_BEGINNING = "earliest";

    private static final String TOPIC = Domain.get();

    private KafkaPublisher publisher;

    private KafkaSubscriber subscriber;


    public JavaFeedsPushRep( ) {
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
        var res = preconditions.postMessage(user, pwd, msg);
        if (res.isOK()) {
            publisher.publish(TOPIC, JSON.encode(List.of("postMessage", user, pwd, msg)));
        }
        return res;
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
