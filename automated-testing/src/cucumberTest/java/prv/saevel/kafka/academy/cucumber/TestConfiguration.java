package prv.saevel.kafka.academy.cucumber;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import prv.saevel.kafka.academy.testing.transaction.alert.Alert;
import prv.saevel.kafka.academy.testing.transaction.alert.Transaction;
import prv.saevel.kafka.academy.testing.transaction.alert.User;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Arrays;
import java.util.HashMap;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Configuration
public class TestConfiguration {
    @Scope(SCOPE_PROTOTYPE)
    @Bean("producerConfig")
    public HashMap<String, Object> producerConfig(@Value("${kafka.bootstrap.servers}") String bootstrapServers){
        HashMap<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return producerConfig;
    }

    @Scope(SCOPE_PROTOTYPE)
    @Bean("consumerConfig")
    public HashMap<String, Object> consumerConfig(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
                                                  @Value("${kafka.group.id}") String groupId){
        HashMap<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return consumerConfig;
    }

    @Bean
    public KafkaSender<String, String> stringSender(@Autowired @Qualifier("producerConfig") HashMap<String, Object> producerConfig){
        HashMap<String, Object> config = (HashMap<String, Object>)producerConfig.clone();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return KafkaSender.create(SenderOptions.create(config));
    }

    @Bean
    public KafkaSender<Long, User> userSender(@Autowired @Qualifier("producerConfig") HashMap<String, Object> producerConfig,
                                              @Value("${schema.registry.url}") String schemaRegistryUrl){
        HashMap<String, Object> config = (HashMap<String, Object>)producerConfig.clone();

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        return KafkaSender.create(SenderOptions.create(config));
    }

    @Bean
    public KafkaSender<Long, Transaction> transactionSender(@Autowired @Qualifier("producerConfig") HashMap<String, Object> producerConfig,
                                                            @Value("${schema.registry.url}") String schemaRegistryUrl){
        HashMap<String, Object> config = (HashMap<String, Object>)producerConfig.clone();

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        return KafkaSender.create(SenderOptions.create(config));
    }

    @Scope(SCOPE_PROTOTYPE)
    @Bean
    public KafkaReceiver<Long, Alert> alertReceiver(@Autowired @Qualifier("consumerConfig") HashMap<String, Object> consumerConfig,
                                                    @Value("${schema.registry.url}") String schemaRegistryUrl,
                                                    @Value("${kafka.topics.alerts}") String alertsTopic){
        HashMap<String, Object> config = (HashMap<String, Object>) consumerConfig.clone();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        config.put("schema.registry.url", schemaRegistryUrl);
        config.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return KafkaReceiver.create(ReceiverOptions.<Long, Alert>create(config).subscription(Arrays.asList(alertsTopic)));
    }

    @Scope(SCOPE_PROTOTYPE)
    @Bean
    public KafkaReceiver<String, String> stringReceiver(@Autowired @Qualifier("consumerConfig") HashMap<String, Object> consumerConfig,
                                                        @Value("${kafka.topics.splitter.output}") String splitterOutputTopic) {
        HashMap<String, Object> config = (HashMap<String, Object>) consumerConfig.clone();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return KafkaReceiver.create(ReceiverOptions.<String, String>create(config).subscription(Arrays.asList(splitterOutputTopic)));
    }
}
