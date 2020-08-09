package prv.saevel.kafka.academy.cucumber.dsl;

import org.hamcrest.Matcher;
import prv.saevel.kafka.academy.cucumber.Pair;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.util.List;

public class KafkaOutputPlaceholder<K, V> {

    private KafkaReceiver<K, V> receiver;

    public KafkaOutputPlaceholder(KafkaReceiver<K, V> receiver){
        this.receiver = receiver;
    }

    public void assertKeysAndValues(Matcher<Iterable<? super Pair<K, V>>> assertion,
                                    Duration timeout,
                                    String msg) {
        KeyValuePairAssertions.assertKeysAndValues(getData(timeout), assertion, msg);
    }

    public void assertKeysAndValues(Matcher<Iterable<? super Pair<K, V>>> assertion,
                                    Duration timeout) {
        KeyValuePairAssertions.assertKeysAndValues(getData(timeout), assertion);
    }

    public void expectKeyAndValue(K key, V value, Duration timeout) {
        KeyValuePairAssertions.expectKeyAndValue(getData(timeout), key, value);
    }

    public void expectKeyAndValue(K key, V value, Duration timeout, String message) {
        KeyValuePairAssertions.expectKeyAndValue(getData(timeout), key, value, message);
    }

    public void expectNoSuchKeyAndValue(K key, V value, Duration timeout) {
        KeyValuePairAssertions.expectNoSuchKeyAndValue(getData(timeout), key, value);
    }

    public void expectNoSuchKeyAndValue(K key, V value, Duration timeout, String message) {
        KeyValuePairAssertions.expectNoSuchKeyAndValue(getData(timeout), key, value, message);
    }

    private <K, V> Pair<K, V> toPair(ReceiverRecord<K, V> record){
        return new Pair<>(record.key(), record.value());
    }

    private List<Pair<K, V>> getData(Duration timeout){
        return receiver.receive().map(this::toPair).buffer(timeout).next().block();
    }
}
