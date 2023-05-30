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
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.kafka.KafkaPublisher;
import sd2223.trab2.servers.kafka.KafkaSubscriber;
import utils.JSON;

public class JavaFeedsPushRep extends JavaFeedsPush implements FeedsPush {

    private static final String KAFKA_BROKERS = "kafka:9092";

    private static final String FROM_BEGINNING = "earliest";

    private static final String TOPIC = Domain.get();

    private KafkaPublisher publisher;

    private KafkaSubscriber subscriber;


    public JavaFeedsPushRep( ) {
        super();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var res = super.postMessage(user, pwd, msg);
        if (res.isOK()) {
            publisher.publish(TOPIC, JSON.encode(msg));
        }
        return res;
    }


}
