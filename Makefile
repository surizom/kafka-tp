KAFKA_DIR=/mnt/c/Users/Polyconseil/Documents/asi/kafka

start-zookeeper:
	./start-zookeeper.sh $(KAFKA_DIR)
start-kafka:
	./start-kafka.sh $(KAFKA_DIR)
start-topics:
	./start-topics.sh $(KAFKA_DIR)
compile-processors:
	mvn clean package
start-counter:
	java -cp target/tp-kafka-0.0.1-SNAPSHOT.jar if4030.kafka.WordCounter
start-tagger:
	java -cp target/tp-kafka-0.0.1-SNAPSHOT.jar if4030.kafka.WordTagger
start-classifier:
	java -cp target/tp-kafka-0.0.1-SNAPSHOT.jar if4030.kafka.ClassificationPrinter
start-producer:
	cat books/shorttext.txt | $(KAFKA_DIR)/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 \
        --topic lines-stream
stop-signal:
	echo "END" | $(KAFKA_DIR)/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 \
        --topic stop-topic
start-line-consumer:
	$(KAFKA_DIR)/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
        --topic lines-stream --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter --property print.key=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
start-count-consumer:
	$(KAFKA_DIR)/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
        --topic words-stream --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter --property print.key=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
start-tag-consumer:
	$(KAFKA_DIR)/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
        --topic tagged-words-stream --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter --property print.key=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
clean:
	rm -rf /tmp/kafka-*
	rm -rf /tmp/zookeeper
	
