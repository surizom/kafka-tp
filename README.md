Little word classification project to explore kafka stream processors

# How to run

In the Makefile, set the KAFKA_DIR variable pointing toward your kafka installation !

On separate terminals for each step :

- run `make start-zookeeper`
- run `make start-kafka`
- run `make start-topics` and `make compile-processors`
- run `make start-splitter`
- run `make start-word-consumer` to watch its output
- run `make start-lexique-intrepreter`
- run `make start-lexique-consumer` to watch its output
- run `make start-classifier`
- run `make start-line-consumer` to watch line feeding input
- run `start-producer` to start line input producer from the book specified in the Makefile
- run `make stop-signal` to tell the classifier to print the classification

# Reinitialization
- run `make clean`