KAFKA_DIR=/mnt/c/Users/Polyconseil/Documents/asi/kafka

start-zookeeper:
	./start-zookeeper.sh $(KAFKA_DIR)
start-kafka:
	./start-kafka.sh $(KAFKA_DIR)
start-streams:
	./start-streams.sh $(KAFKA_DIR)
clean:
	rm -rf /tmp/kafka-*
	rm -rf /tmp/zookeeper
	
