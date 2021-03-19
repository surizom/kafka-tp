KAFKA_DIR=/mnt/c/Users/Polyconseil/Documents/asi/kafka

start-zookeeper:
	./start-zookeeper.sh $(KAFKA_DIR)
start-kafka:
	./start-kafka.sh $(KAFKA_DIR)
start-topics:
	./start-topics.sh $(KAFKA_DIR)
compile-processors: $(wildcard *.java)
	mvn clean package
start-processors: compile-processors
	./start-processors.sh
clean:
	rm -rf /tmp/kafka-*
	rm -rf /tmp/zookeeper
	
