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
                case "postMessage":
                    String user = (String) args.get(0);
                    String pwd = (String) args.get(1);
                    Message msg = (Message) args.get(2);
                    super.postMessage(user, pwd, msg);
                    break;
                case "removeFromPersonalFeed":
                    user = (String) args.get(0);
                    long mid = (long) args.get(1);
                    pwd = (String) args.get(2);
                    super.removeFromPersonalFeed(user, mid, pwd);
                    break;
                case "deleteFromUserFeed":
                    user = (String) args.get(0);
                    Set<Long> mids = (Set<Long>) args.get(1);
                    super.deleteFromUserFeed(user, mids);
                    break;
                case "getMessage":
                    user = (String) args.get(0);
                    mid = (Long) args.get(1);
                    super.getMessage(user, mid);
                    break;
                case "getMessages":
                    user = (String) args.get(0);
                    long time = (long) args.get(1);
                    super.getMessages(user, time);
                    break;
                case "subUser":
                    user = (String) args.get(0);
                    String userSub = (String) args.get(1);
                    pwd = (String) args.get(2);
                    super.subUser(user, userSub, pwd);
                    break;
                case "unsubscribeUser":
                    user = (String) args.get(0);
                    userSub = (String) args.get(1);
                    pwd = (String) args.get(2);
                    super.unsubscribeUser(user, userSub, pwd);
                    break;
                case "listSubs":
                    user = (String) args.get(0);
                    super.listSubs(user);
                    break;
                case "deleteUserFeed":
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
            KafkaMessage message = new KafkaMessage("postMessage", user, pwd, msg);
            publisher.publish(TOPIC, JSON.encode(message));
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
