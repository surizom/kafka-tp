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

clean:
	rm -rf /tmp/kafka-*
	rm -rf /tmp/zookeeper
	
