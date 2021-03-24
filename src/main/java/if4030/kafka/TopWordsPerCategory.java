package if4030.kafka;

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
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Produced;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Un composant Kafka sera en charge de recevoir les lignes à partir du topic lines-stream
 * et produira des mots sur un topic words-stream ; seuls les mots constitués de caractères alphabétiques en minuscules
 * (ou du caractère - pour tenir compte des mots composés) seront envoyés sur le topic words-stream.
 */
public final class TopWordsPerCategory {

    public static final String INPUT_TOPIC = "tagged-words-stream";
    public static final String STOP_TOPIC = "stop-topic";

    static Properties getStreamsConfig(final String[] args) throws IOException {
        final Properties props = new Properties();
        if (args != null && args.length > 0) {
            try (final FileInputStream fis = new FileInputStream(args[0])) {
                props.load(fis);
            }
            if (args.length > 1) {
                System.out.println("Warning: Some command line arguments were ignored. This demo only accepts an optional configuration file.");
            }
        }
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "lexical-translation");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    static Map<String, Map<String, Long>> categoriesMap = new HashMap<>();

    static void getTaggedWords(final StreamsBuilder builder) {
        final KStream<String, String> source = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        final KStream<String, String> stopStream = builder.stream(STOP_TOPIC);

        source.peek((key, value) -> {
            if (categoriesMap.containsKey(key)) {
                categoriesMap.get(key).put(value, categoriesMap.get(key).get(value) + 1);
            } else {
                categoriesMap.put(key, new HashMap<>());
                categoriesMap.get(key).put(value, 1L);
            }
        });

        stopStream.peek((key, value) -> {
            System.out.println(key);
            System.out.println(value);
            if ("END".equals(value)) printTop20();
        });
    }

    static void printTop20() {
        categoriesMap.entrySet().stream().forEach(
                category -> category
                        .getValue()
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .limit(20)
                        .forEach((entry) -> System.out.println("catégorie : " + category.getKey() + "; word : " + entry.getKey() + "; occurences : " + entry.getValue())));
    }

    public static void main(final String[] args) throws IOException {

        final Properties props = getStreamsConfig(args);

        final StreamsBuilder builder = new StreamsBuilder();
        getTaggedWords(builder);
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-taggedWords-shutdown-hook") {
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
