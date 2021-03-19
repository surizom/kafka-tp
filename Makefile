init:
	./init.sh
start-streams:
	./start-streams.sh
clean:
	rm -rf /tmp/kafka-*
	rm -rf /tmp/zookeeper
	
