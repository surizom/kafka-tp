package if4030.kafka;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

/**
 * Demonstrates, using the high-level KStream DSL, how to implement the
 * WordCount program that computes a simple word occurrence histogram from an
 * input text.
 * <p>
 * In this example, the input stream reads from a topic named
 * "streams-plaintext-input", where the values of messages represent lines of
 * text; and the histogram output is written to topic "streams-wordcount-output"
 * where each record is an updated count of a single word.
 * <p>
 * Before running this example you must create the input topic and the output
 * topic (e.g. via {@code bin/kafka-topics.sh --create ...}), and write some
 * data to the input topic (e.g. via {@code bin/kafka-console-producer.sh}).
 * Otherwise you won't see any data arriving in the output topic.
 */
public final class WordTagger {

    public static final String INPUT_TOPIC = "words-stream";
    public static final String OUTPUT_TOPIC = "tagged-words-stream";

    private static Map<String, String> tagMap = new HashMap<>();

    static Properties getStreamsConfig(final String[] args) throws IOException {
        final Properties props = new Properties();
        if (args != null && args.length > 0) {
            try (final FileInputStream fis = new FileInputStream(args[0])) {
                props.load(fis);
            }
            if (args.length > 1) {
                System.out.println(
                        "Warning: Some command line arguments were ignored. This demo only accepts an optional configuration file.");
            }
        }
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // setting offset reset to earliest so that we can re-run the demo code with the
        // same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    static void createWordTagStream(final StreamsBuilder builder) {
        final KStream<String, Long> source = builder.stream(INPUT_TOPIC);

        final KTable<String, Long> taggedWords = source.map((key, value) -> KeyValue.pair(tagMap.get(key), value))
                .groupByKey().reduce(Long::sum);

        // need to override value serde to Long type
        taggedWords.toStream().to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));
    }

    public static void main(final String[] args) throws IOException, URISyntaxException {
        final Properties props = getStreamsConfig(args);

        final StreamsBuilder builder = new StreamsBuilder();
        createWordTagStream(builder);
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        File lexique = Paths.get(INPUT_TOPIC.getClass().getClassLoader().getResource("abc.txt").toURI()).toFile();

        try (Scanner scanner = new Scanner(lexique);) {
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(";");
                tagMap.put(line[0], line[2]);
            }
        }

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
