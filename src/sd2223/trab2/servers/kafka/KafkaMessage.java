package sd2223.trab2.servers.kafka;

import java.util.Arrays;
import java.util.List;

public class KafkaMessage {

    private String op;
    private List<Object> args;

    public KafkaMessage(String op, Object ... args) {
        this.op = op;
        this.args = Arrays.asList(args);
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
                "op='" + op + '\'' +
                ", args=" + args +
                '}';
    }
}
