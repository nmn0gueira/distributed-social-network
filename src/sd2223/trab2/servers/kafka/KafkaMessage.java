package sd2223.trab2.servers.kafka;

import java.util.Arrays;
import java.util.List;

public class KafkaMessage {

    private final long replicaId;
    private final String op;
    private final List<Object> args;

    public KafkaMessage(long replicaId, String op, Object ... args) {
        this.replicaId = replicaId;
        this.op = op;
        this.args = Arrays.asList(args);
    }

    public long getReplicaId() {
        return replicaId;
    }

    public String getOp() {
        return op;
    }

    public List<Object> getArguments() {
        return args;
    }

    @Override
    public String toString() {
        return "KafkaMessage{" +
                "replicaId=" + replicaId +
                ", op='" + op + '\'' +
                ", args=" + args +
                '}';
    }
}
