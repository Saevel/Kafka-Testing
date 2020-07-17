package prv.saevel.kafka.academy.automation.glue;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import prv.saevel.kafka.academy.automation.KafkaAssertions;
import prv.saevel.kafka.academy.automation.TestConfiguration;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.util.Arrays;

@CucumberContextConfiguration
@SpringBootTest(classes = TestConfiguration.class)
public class SplitterTest {
    @Autowired
    private KafkaSender<String, String> sender;

    @Autowired
    private ReceiverOptions<String, String> receiverOptions;

    @Value("${kafka.test.timeout.ms}")
    private long kafkaTestTimeoutMs;

    @Value("${kafka.topics.splitter.input}")
    private String splitterInputTopic;

    @Value("${kafka.topics.splitter.output}")
    private String splitterOutputTopic;

    @When("a message with key: {word} and value: {word} is sent to the Splitter input topic")
    public void whenMessageSentToKafka(String key, String value){
        sender.send(
                Mono.just(SenderRecord.create(new ProducerRecord<>(splitterInputTopic, key, value), null))
        ).blockLast();
    }

    @Then("there should be a message with key: {word} and value: {word} on the Splitter output topic")
    public void thenMessageReceivedFromKafka(String key, String value){
        new KafkaAssertions<>(KafkaReceiver.create(receiverOptions.subscription(Arrays.asList(splitterOutputTopic))))
                .expectKeyAndValue(Duration.ofMillis(kafkaTestTimeoutMs), key, value);
    }
}
