import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Arrays;
import java.util.Properties;

public class SplitterApplication {

    public static final String SPLITTER_INPUT_TOPIC = "splitter-input";

    public static final String SPLITTER_OUTPUT_TOPIC = "splitter-output";

    public static final String SPLIT_REGEX = ",";

    public static final String APPLICATION_ID = "Splitter";

    public static void main(String[] args){

        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Environment.BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);

        StreamsConfig config = new StreamsConfig(properties);

        StreamsBuilder builder = new StreamsBuilder();

        builder.<String, String>stream(SPLITTER_INPUT_TOPIC)
                .flatMapValues(s -> Arrays.asList(s.split(SPLIT_REGEX)))
                .to(SPLITTER_OUTPUT_TOPIC);

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();
    }
}
