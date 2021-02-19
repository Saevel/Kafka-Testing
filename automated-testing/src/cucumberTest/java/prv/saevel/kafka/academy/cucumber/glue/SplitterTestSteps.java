package prv.saevel.kafka.academy.cucumber.glue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import prv.saevel.kafka.academy.cucumber.TestConfiguration;
import prv.saevel.kafka.academy.cucumber.dsl.KafkaOutputPlaceholder;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;

@CucumberContextConfiguration
@SpringBootTest(classes = TestConfiguration.class)
public class SplitterTestSteps {
    @Autowired
    private KafkaSender<String, String> sender;

    @Autowired
    private KafkaReceiver<String, String> receiver;

    @Value("${kafka.test.timeout.ms}")
    private long kafkaTestTimeoutMs;

    @Value("${kafka.topics.splitter.input}")
    private String splitterInputTopic;

    private KafkaOutputPlaceholder<String, String> output;

    @Given("a subscription to the Splitter output topic")
    public void givenASubscription() throws InterruptedException {
        output = new KafkaOutputPlaceholder<>(receiver);
    }

    @When("a message with key: {word} and value: {word} is sent to the Splitter input topic")
    public void whenMessageSentToKafka(String key, String value){
        sender.send(
                Mono.just(SenderRecord.create(new ProducerRecord<>(splitterInputTopic, key, value), null))
        ).blockLast();
    }

    @Then("there should be a message with key: {word} and value: {word} on the Splitter output topic")
    public void thenMessageReceivedFromKafka(String key, String value) {
        output.expectKeyAndValue(key, value, Duration.ofMillis(kafkaTestTimeoutMs));
    }
}
