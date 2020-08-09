package prv.saevel.kafka.academy.cucumber.dsl;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import prv.saevel.kafka.academy.cucumber.Pair;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class KafkaAssertions {

    private KafkaAssertions() {
    }

    public static <K, V> void assertKeysAndValues(Flux<ReceiverRecord<K, V>> data,
                                                  Matcher<Iterable<? super Pair<K, V>>> assertion,
                                                  Duration timeout,
                                                  String msg) {
        StepVerifier
                .create(data.map(KafkaAssertions::toPair).buffer(timeout))
                .assertNext(pairs -> MatcherAssert.assertThat(msg, pairs, assertion))
                .thenCancel()
                .verify();
    }


    public static <K, V> void assertKeysAndValues(Flux<ReceiverRecord<K, V>> data,
                                                  Matcher<Iterable<? super Pair<K, V>>> assertion,
                                                  Duration timeout) {
        assertKeysAndValues(data, assertion, timeout, "Key-value pair does not match");
    }

    public static <K, V> void expectKeyAndValue(Flux<ReceiverRecord<K, V>> data, K key, V value, Duration timeout) {
        assertKeysAndValues(data, hasItem(equalTo(new Pair<>(key, value))), timeout);
    }

    public static <K, V> void expectKeyAndValue(Flux<ReceiverRecord<K, V>> data, K key, V value, Duration timeout, String message) {
        assertKeysAndValues(data, hasItem(equalTo(new Pair<>(key, value))), timeout, message);
    }

    public static <K, V> void expectNoSuchKeyAndValue(Flux<ReceiverRecord<K, V>> data, K key, V value, Duration timeout) {
        assertKeysAndValues(data, not(hasItem(equalTo(new Pair<>(key, value)))), timeout);
    }

    public static <K, V> void expectNoSuchKeyAndValue(Flux<ReceiverRecord<K, V>> data, K key, V value, Duration timeout, String message) {
        assertKeysAndValues(data, not(hasItem(equalTo(new Pair<>(key, value)))), timeout, message);
    }

    private static <K, V> Pair<K, V> toPair(ReceiverRecord<K, V> record) {
        return new Pair<>(record.key(), record.value());
    }
}
