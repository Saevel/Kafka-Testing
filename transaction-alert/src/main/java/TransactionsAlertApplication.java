import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import prv.saevel.kafka.academy.testing.transaction.alert.Alert;
import prv.saevel.kafka.academy.testing.transaction.alert.Transaction;
import prv.saevel.kafka.academy.testing.transaction.alert.User;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class TransactionsAlertApplication {

    public static final Duration ANALYSIS_WINDOW = Duration.ofMinutes(15);

    public static final String USERS_TOPIC = "users";

    public static final String TRANSACTIONS_TOPIC = "transactions";

    public static final String ALERTS_TOPIC = "alerts";

    public static final int ERROR_TRANSACTIONS_FROM_DIFFERENT_COUNTRIES = 12;

    public static final String TWO_TRANSACTIONS_FROM_DIFFERENT_COUNTIES_IN_SHORT_TIME_SUCCESSION = "Two transactions from different counties in short time succession";

    public static void main(String[] args){
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Environment.BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.LongSerde.class.getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class.getName());
        properties.put("schema.registry.url", Environment.SCHEMA_REGISTRY_URL);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "Transactions-Alert");

        StreamsConfig config = new StreamsConfig(properties);

        StreamsBuilder builder = new StreamsBuilder();

        KTable<Long, User> users = builder.table(USERS_TOPIC);

        KStream<Long, Transaction> transactions = builder.stream(TRANSACTIONS_TOPIC);

        transactions
                .groupBy((key, transaction) -> transaction.getUserId())
                .windowedBy(TimeWindows.of(ANALYSIS_WINDOW))
                .aggregate(() -> new LinkedList<>(), TransactionsAlertApplication::collectAsList)
                .filter((key, value) -> value.isEmpty())
                .filter(TransactionsAlertApplication::hasTransactionsInDifferentCountries)
                .toStream()
                .map(removeWindow())
                .flatMapValues(value -> value)
                .join(users, TransactionsAlertApplication::toAlert)
                .to(ALERTS_TOPIC);

        try(KafkaStreams streams = new KafkaStreams(builder.build(), config)){
            streams.start();
        }
    }

    private static Alert toAlert(Transaction transaction, User user) {
        return Alert.newBuilder()
                .setUserId(user.getId())
                .setTransactionId(transaction.getId())
                .setCode(ERROR_TRANSACTIONS_FROM_DIFFERENT_COUNTRIES)
                .setMessage(TWO_TRANSACTIONS_FROM_DIFFERENT_COUNTIES_IN_SHORT_TIME_SUCCESSION)
                .build();
    }

    private static KeyValueMapper<Windowed<Long>, LinkedList<Transaction>, KeyValue<? extends Long, ? extends LinkedList<Transaction>>> removeWindow() {
        return (key, value) -> new KeyValue<>(key.key(), value);
    }

    private static LinkedList<Transaction> collectAsList(Long id, Transaction transaction, LinkedList<Transaction> accumulator) {
        accumulator.add(transaction);
        return accumulator;
    }

    private static boolean hasTransactionsInDifferentCountries(Object any, List<Transaction> transactions){
        if(transactions.isEmpty()){
            return false;
        } else {
            Stream<Transaction> transactionStream = transactions.stream()
                    .filter(transaction -> transaction.getCountry() != null);

            return transactionStream
                    .findFirst()
                    .map(firstTransaction ->
                            transactionStream.allMatch(otherTransaction -> firstTransaction.getCountry().equals(otherTransaction.getCountry()))
                    ).orElse(false);
        }
    }
}
