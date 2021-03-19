#!/bin/bash

eval "$1/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
        --topic streams-wordcount-output --from-beginning \
        --formatter kafka.tools.DefaultMessageFormatter --property print.key=true \
        --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
        --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer"
