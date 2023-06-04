package sd2223.trab2.servers.java;

import java.util.List;
import java.util.logging.Logger;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.Feeds;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.Domain;
import sd2223.trab2.servers.kafka.KafkaMessage;
import sd2223.trab2.servers.kafka.KafkaPublisher;
import sd2223.trab2.servers.kafka.KafkaSubscriber;
import sd2223.trab2.servers.kafka.sync.SyncPoint;
import utils.JSON;

import static sd2223.trab2.api.java.Result.ok;

public class JavaFeedsRep<T extends JavaFeedsCommon<? extends Feeds>> implements FeedsPush, FeedsPull {

    private static final Logger Log = Logger.getLogger(JavaFeedsRep.class.getName());

    protected final T impl;

    private static final long REPLICA_ID = Domain.uuid();

    private static final String KAFKA_BROKERS = "kafka:9092";

    private static final String FROM_BEGINNING = "earliest";

    private static final String TOPIC = Domain.get();

    // Operation constants used in Kafka messages
    private static final String POST_MESSAGE = "postMessage";
    private static final String REMOVE_FROM_PERSONAL_FEED = "removeFromPersonalFeed";
    private static final String SUB_USER = "subUser";
    private static final String UNSUBSCRIBE_USER = "unsubscribeUser";
    private static final String DELETE_USER_FEED = "deleteUserFeed";

    private final KafkaPublisher publisher;

    private final SyncPoint<Object> syncPoint = SyncPoint.getInstance();


    public JavaFeedsRep(T impl) {
        this.impl = impl;
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        KafkaSubscriber subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);
        subscriber.start(false, (r) -> {
            System.out.printf("SeqN: %s %d %s\n", r.topic(), r.offset(), r.value());
            var version = r.offset();
            var kafkaMsg = JSON.decode(r.value(), KafkaMessage.class);
            long messageReplicaId = kafkaMsg.getReplicaId();
            List<Object> args = kafkaMsg.getArguments();
            switch (kafkaMsg.getOp()) {
                case POST_MESSAGE -> {
                    String user = (String) args.get(0);
                    String pwd = (String) args.get(1);
                    Message msg = JSON.decode(args.get(2).toString(), Message.class);
                    Long mid = JSON.decode(args.get(3).toString(), Long.class);
                    var result = messageReplicaId == REPLICA_ID ? this.impl.postMessage(user, pwd, msg) : postMessageRep(user, pwd, msg, mid);
                    syncPoint.setResult(version, result);
                }
                case REMOVE_FROM_PERSONAL_FEED -> {
                    String user = (String) args.get(0);
                    Long mid = JSON.decode(args.get(1).toString(), Long.class);
                    String pwd = (String) args.get(2);
                    var result = this.impl.removeFromPersonalFeed(user, mid, pwd);
                    syncPoint.setResult(version, result);
                }
                case SUB_USER -> {
                    String user = (String) args.get(0);
                    String userSub = (String) args.get(1);
                    String pwd = (String) args.get(2);
                    var result = subUserRep(user, userSub, pwd);
                    System.out.println("Result DEBUG: " + result);
                    syncPoint.setResult(version, result);
                }
                case UNSUBSCRIBE_USER -> {
                    String user = (String) args.get(0);
                    String userSub = (String) args.get(1);
                    String pwd = (String) args.get(2);
                    syncPoint.setResult(version, unsubscribeUserRep(user, userSub, pwd));

                }
                case DELETE_USER_FEED -> {
                    String user = (String) args.get(0);
                    var result = this.impl.deleteUserFeed(user);
                    syncPoint.setResult(version, result);
                    syncPoint.incOffset();
                }
            }
        });
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        var res = impl.preconditions.postMessage(user, pwd, msg); // Operation is executed locally
        if (res.isOK()) {
            // Serial number will be incremented by the publisher
            KafkaMessage message = new KafkaMessage(REPLICA_ID, POST_MESSAGE, user, pwd, msg, impl.serial.get() + 1L);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        var res = impl.preconditions.removeFromPersonalFeed(user, mid, pwd);
        if (!res.isOK())
            return res;
        var ufi = impl.feeds.get(user);
        if (ufi == null) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        synchronized (ufi.user()) {
            if (!ufi.messages().contains(mid)) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
        }
        KafkaMessage message = new KafkaMessage(REPLICA_ID, REMOVE_FROM_PERSONAL_FEED, user, mid, pwd);
        publisher.publish(TOPIC, JSON.encode(message));

        return res;
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return impl.getMessage(user, mid);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return impl.getMessages(user, time);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        var res = impl.preconditions.subUser(user, userSub, pwd);
        if (res.isOK()) {
            KafkaMessage message = new KafkaMessage(REPLICA_ID, SUB_USER, user, userSub, pwd);
            publisher.publish(TOPIC, JSON.encode(message));
        }
        return res;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        var res = impl.preconditions.unsubscribeUser(user, userSub, pwd);
        if (res.isOK()) {
            JavaFeedsCommon.FeedInfo ufi = impl.feeds.get(user);
            if (!ufi.following().contains(userSub))
                return Result.error(Result.ErrorCode.NOT_FOUND);
            else {
                KafkaMessage message = new KafkaMessage(REPLICA_ID, UNSUBSCRIBE_USER, user, userSub, pwd);
                publisher.publish(TOPIC, JSON.encode(message));
            }
        }

        return res;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return impl.listSubs(user);
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        var res = impl.preconditions.deleteUserFeed(user);
        if (!res.isOK())
            return res;
        if (!impl.feeds.containsKey(user)) {    // Preconditions do not check for users without feeds
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        KafkaMessage message = new KafkaMessage(REPLICA_ID, DELETE_USER_FEED, user);
        publisher.publish(TOPIC, JSON.encode(message));
        return res;
    }

    private Result<Long> postMessageRep(String user, String pwd, Message msg, Long mid) {
        msg.setId(mid);

        JavaFeedsCommon.FeedInfo ufi = impl.feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.messages().add(mid);
            impl.messages.putIfAbsent(mid, msg);
        }
        return Result.ok(mid);
    }

    private Result<Void> subUserRep(String user, String userSub, String pwd) {
        var ufi = impl.feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.following().add(userSub);
        }
        return ok();
    }

    private Result<Void> unsubscribeUserRep(String user, String userSub, String pwd) {
        JavaFeedsCommon.FeedInfo ufi = impl.feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new);
        synchronized (ufi.user()) {
            ufi.following().remove(userSub);
        }


        return ok();
    }

    @Override
    public Result<Void> push_PushMessage(PushMessage msg) {
        return ((FeedsPush) impl).push_PushMessage(msg);
    }

    @Override
    public Result<Void> push_updateFollowers(String user, String follower, boolean following) {
        return ((FeedsPush) impl).push_updateFollowers(user, follower, following);
    }

    @Override
    public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time) {
        return ((FeedsPull) impl).pull_getTimeFilteredPersonalFeed(user, time);
    }
}
