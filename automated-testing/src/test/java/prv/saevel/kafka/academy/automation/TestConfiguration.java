package prv.saevel.kafka.academy.automation;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import prv.saevel.kafka.academy.testing.transaction.alert.Alert;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;

@Configuration
public class TestConfiguration {

    @Bean("producerConfig")
    public HashMap<String, Object> producerConfig(@Value("${kafka.bootstrap.servers}") String bootstrapServers){
        HashMap<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return producerConfig;
    }

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
    public ReceiverOptions<Long, Alert> alertReceiverOptions(@Autowired @Qualifier("consumerConfig") HashMap<String, Object> consumerConfig,
                                                             @Value("${schema.registry.url}") String schemaRegistryUrl){
        HashMap<String, Object> config = (HashMap<String, Object>) consumerConfig.clone();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        config.put("schema.registry.url", schemaRegistryUrl);

        return ReceiverOptions.create(config);
    }

    @Bean
    public ReceiverOptions<String, String> stringReceiverOptions(@Autowired @Qualifier("consumerConfig") HashMap<String, Object> consumerConfig) {
        HashMap<String, Object> config = (HashMap<String, Object>) consumerConfig.clone();
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return ReceiverOptions.create(config);
    }
}
