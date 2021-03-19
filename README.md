Little word classification project to explore kafka stream processors

# How to run

- In the Makefile, set the KAFKA_DIR variable pointing toward your kafka installation
On separate terminals for each step :
- run `make start-zookeeper`
- run `make start-kafka`
- run `make start-topics` and `make compile-processors`
- run `make start-counter`
- run `make start-count-consumer` to watch its output
- run `make start-tagger`
- run `make start-tag-consumer` to watch its output
- run `make start-classifier`
- run `make start-line-consumer` to watch line feeding input
- run `start-producer` to start line input producer
- run `make stop-signal` to tell the classifier to print the classification

# Reinitialization
- run `make clean`