package prv.saevel.kafka.academy;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TransactionsAlertApplication {

    public static final Duration ANALYSIS_WINDOW = Duration.ofMinutes(2);

    public static final String USERS_TOPIC = "users";

    public static final String REKEYED_USERS_TOPIC = "users_rekeyed";

    public static final String TRANSACTIONS_TOPIC = "transactions";

    public static final String ALERTS_TOPIC = "alerts";

    public static final int ERROR_TRANSACTIONS_FROM_DIFFERENT_COUNTRIES = 12;

    public static final String TWO_TRANSACTIONS_FROM_DIFFERENT_COUNTIES_IN_SHORT_TIME_SUCCESSION = "Two transactions from different counties in short time succession";

    public static final String APPLICATION_ID = "Transactions-Alert";

    public static void main(String[] args){

        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Environment.BOOTSTRAP_SERVERS);
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, Environment.SCHEMA_REGISTRY_URL);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);

        StreamsConfig config = new StreamsConfig(properties);

        StreamsBuilder builder = new StreamsBuilder();

        SpecificAvroSerde<User> userSerde = new SpecificAvroSerde<>();
        userSerde.configure(properties, false);

        SpecificAvroSerde<Transaction> transactionSerde = new SpecificAvroSerde<>();
        transactionSerde.configure(properties, false);

        SpecificAvroSerde<Alert> alertSerde = new SpecificAvroSerde<>();
        alertSerde.configure(properties, false);

        Consumed<byte[], User> usersConsumer = Consumed.with(Serdes.ByteArray(), userSerde);
        Consumed<Long, User> rekeyedUsersConsumer = Consumed.with(Serdes.Long(), userSerde);
        Consumed<byte[], Transaction> transactionConsumed = Consumed.with(Serdes.ByteArray(), transactionSerde);

        Produced<Long, User> userPoducer = Produced.with(Serdes.Long(), userSerde);

        Produced<Long, Alert> alertProducer = Produced.with(Serdes.Long(), alertSerde);

        builder.stream(USERS_TOPIC, usersConsumer).selectKey((key, value) -> value.getId()).to(REKEYED_USERS_TOPIC, userPoducer);

        KTable<Long, User> users = builder.table(REKEYED_USERS_TOPIC, rekeyedUsersConsumer);

        KStream<Long, Transaction> transactions = builder.stream(TRANSACTIONS_TOPIC, transactionConsumed).selectKey((key, value) -> value.getUserId());

        Grouped<Long, Transaction> transactionGrouper = Grouped.with(Serdes.Long(), transactionSerde);

        Serde<LinkedList<Transaction>> listSerde = Serdes.serdeFrom(new JavaSerializer<LinkedList<Transaction>>(), new JavaDeserializer<LinkedList<Transaction>>());

        Joined<Long, Transaction, User> joiner = Joined.with(Serdes.Long(), transactionSerde, userSerde);

        transactions
                .groupBy((key, transaction) -> transaction.getUserId(), transactionGrouper)
                .windowedBy(TimeWindows.of(ANALYSIS_WINDOW))
                .aggregate(LinkedList::new, TransactionsAlertApplication::collectAsList, Materialized.with(Serdes.Long(), listSerde))
                .toStream()
                .filter((key, value) -> !value.isEmpty())
                .filter(TransactionsAlertApplication::hasTransactionsInDifferentCountries)
                .map(removeWindow())
                .flatMapValues(value -> value)
                .join(users, TransactionsAlertApplication::toAlert, joiner)
                .to(ALERTS_TOPIC, alertProducer);

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.cleanUp();
        streams.start();
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
                            transactions
                                    .stream()
                                    .filter(transaction -> transaction.getCountry() != null).anyMatch(otherTransaction -> !firstTransaction.getCountry().equals(otherTransaction.getCountry()))
                    ).orElse(false);
        }
    }
}
