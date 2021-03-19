#!/bin/bash
eval "$1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 \
        --replication-factor 1 --partitions 1 \
        --topic lines-stream"

eval "$1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 \
        --replication-factor 1 --partitions 1 \
        --topic words-stream"

eval "$1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 \
        --replication-factor 1 --partitions 1 \
        --topic tagged-words-stream"

eval "$1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 \
        --replication-factor 1 --partitions 1 \
        --topic stop-topic"
