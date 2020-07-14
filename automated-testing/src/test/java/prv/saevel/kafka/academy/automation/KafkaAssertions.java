package prv.saevel.kafka.academy.automation;

import lombok.AllArgsConstructor;
import org.hamcrest.Matcher;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@AllArgsConstructor
public class KafkaAssertions<K, V> {

    private KafkaReceiver<K, V> receiver;

    public void assertKeyValue(Duration timeout, Matcher<Pair<K, V>> assertion){
        StepVerifier.create(receiver.receive().buffer(timeout))
                .assertNext(records -> assertThat(
                        records.stream().map(this::toPair).collect(Collectors.toList()),
                        hasItem(assertion)
                )).thenCancel()
                .verify();
    }

    public void assertKeyValue(Duration timeout, Matcher<Pair<K, V>> assertion, String msg){
        StepVerifier.create(receiver.receive().buffer(timeout))
                .assertNext(records -> assertThat(
                        msg,
                        records.stream().map(this::toPair).collect(Collectors.toList()),
                        hasItem(assertion)
                )).thenCancel()
                .verify();
    }

    public void expectKeyAndValue(Duration timeout, K key, V value){
        assertKeyValue(timeout, equalTo(new Pair<>(key, value)));
    }

    public void expectKeyAndValue(Duration timeout, K key, V value, String message){
        assertKeyValue(timeout, equalTo(new Pair<>(key, value)), message);
    }

    private <K, V>Pair<K, V> toPair(ReceiverRecord<K, V> record){
        return new Pair<>(record.key(), record.value());
    }
}
