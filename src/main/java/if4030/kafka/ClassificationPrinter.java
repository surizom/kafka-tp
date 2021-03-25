package if4030.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

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
public final class ClassificationPrinter {

    public static final String STOP_TOPIC = "stop-topic";
    public static final String INPUT_TOPIC = "tagged-words-stream";

    private static Map<String, Map<String, Long>> lexiqueOccurenceByCategory = new HashMap<>();

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
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "classification-printer");
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

    static void updateOccurences(String category, String lemme) {
        if (lexiqueOccurenceByCategory.get(category) == null) {
            lexiqueOccurenceByCategory.put(category, Map.of(lemme, 1L));
        } else {
            lexiqueOccurenceByCategory.put(category,
                    generateUpdatedCategory(lemme, lexiqueOccurenceByCategory.get(category)));
        }
    }

    static Map<String, Long> generateUpdatedCategory(String lemme, Map<String, Long> categoryMap) {

        Map<String, Long> newMap = categoryMap.entrySet().stream().filter(entry -> !entry.getKey().equals(lemme))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (categoryMap.get(lemme) == null) {
            newMap.put(lemme, 1L);
        } else {
            newMap.put(lemme, categoryMap.get(lemme) + 1L);
        }

        return newMap;
    }

    static void createWordTagStream(final StreamsBuilder builder) {
        final KStream<String, String> source = builder.stream(INPUT_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String()));

        final KStream<String, String> stopSource = builder.stream(STOP_TOPIC);

        source.filter((key, value) -> key != null && !key.isBlank() && value != null && !value.isBlank())
                .peek((key, value) -> updateOccurences(key, value));

        stopSource.filter((key, value) -> value.equals("END")).peek((key, value) -> printTop20Tags());
    }

    static void printTop20Tags() {
        lexiqueOccurenceByCategory.entrySet().stream().forEach(category -> printTop20InCategory(category));
    }

    static void printTop20InCategory(Entry<String, Map<String, Long>> category) {
        System.out.println("---------------------");
        System.out.println("Catégorie : " + category.getKey());
        category.getValue().entrySet().stream().sorted(Entry.<String, Long>comparingByValue().reversed()).limit(20)
                .forEach((entry) -> System.out
                        .println("mot : " + entry.getKey() + "; occurences : " + entry.getValue()));
    }

    public static void main(final String[] args) throws IOException {
        final Properties props = getStreamsConfig(args);

        final StreamsBuilder builder = new StreamsBuilder();
        createWordTagStream(builder);
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("classification-printer-shutdown-hook") {
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
