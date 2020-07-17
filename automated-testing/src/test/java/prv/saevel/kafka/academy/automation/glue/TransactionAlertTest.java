package prv.saevel.kafka.academy.automation.glue;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import prv.saevel.kafka.academy.automation.KafkaAssertions;
import prv.saevel.kafka.academy.testing.transaction.alert.Alert;
import prv.saevel.kafka.academy.testing.transaction.alert.Transaction;
import prv.saevel.kafka.academy.testing.transaction.alert.User;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.util.Arrays;

public class TransactionAlertTest {

    @Autowired
    private KafkaSender<Long, User> userSender;

    @Autowired
    private KafkaSender<Long, Transaction> transactionSender;

    @Autowired
    private ReceiverOptions<Long, Alert> alertReceiverOptions;

    @Value("${kafka.test.timeout.ms}")
    private long kafkaTestTimeoutMs;

    @Value("${kafka.topics.users}")
    private String usersTopic;

    @Value("${kafka.topics.transactions}")
    private String transactionsTopic;

    @Value("${kafka.topics.alerts}")
    private String alertsTopic;

    @Value("${errors.transactions.from.different.countries.in.short.succession.code}")
    private int transactionFromDifferentCountriesInShortSuccessionCode;

    @Value("${errors.transactions.from.different.countries.in.short.succession.message}")
    private String transactionFromDifferentCountriesInShortSuccessionMessage;

    @When("there is a user with id: {long}, email: {word} and phone number: {word}")
    public void whenUserPublished(long userId, String email, String phoneNumber){
        User user = User.newBuilder().setId(userId).setEmail(email).setPhoneNumber(phoneNumber).build();
        userSender.send(Mono.just(
                SenderRecord.create(new ProducerRecord<>(usersTopic, userId, user), null)
        )).blockLast();
    }

    @When("in comes a transaction with id: {long}, amount: {double}, type: {word}, country: {word} and user id: {long}")
    public void whenNewTransaction(long transactionId, double amount, String type, String country, long userId){
        Transaction transaction = Transaction.newBuilder()
                .setOutgoing(true)
                .setAmount(amount)
                .setCountry(country)
                .setId(transactionId)
                .setType(type)
                .setUserId(userId)
                .build();

        transactionSender.send(Mono.just(
                SenderRecord.create(new ProducerRecord<>(transactionsTopic, userId, transaction), null)
        )).blockLast();
    }

    @Then("there should be an alert published with user id: {long} and transaction id: {long}")
    public void shouldAlertOfPotentialFraud(long userId, long transactionId){

        Alert alert = Alert.newBuilder()
                .setCode(transactionFromDifferentCountriesInShortSuccessionCode)
                .setMessage(transactionFromDifferentCountriesInShortSuccessionMessage)
                .setUserId(userId)
                .setTransactionId(transactionId)
                .build();

        new KafkaAssertions<>(KafkaReceiver.create(alertReceiverOptions.subscription(Arrays.asList(alertsTopic))))
                .expectKeyAndValue(Duration.ofMillis(kafkaTestTimeoutMs), userId, alert);
    }
}
