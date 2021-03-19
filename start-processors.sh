#!/bin/bash

java -cp target/tp-kafka-0.0.1-SNAPSHOT.jar if4030.kafka.WordCounter
java -cp target/tp-kafka-0.0.1-SNAPSHOT.jar if4030.kafka.WordTagger
java -cp target/tp-kafka-0.0.1-SNAPSHOT.jar if4030.kafka.ClassificationPrinter
