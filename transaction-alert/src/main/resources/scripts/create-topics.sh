kafka-topics --create --topic splitter-input --bootstrap-server localhost:29092 --partitions 2 --replication-factor 1

kafka-topics --create --topic splitter-output --bootstrap-server localhost:29092 --partitions 2 --replication-factor 1

kafka-topics --create --topic users --bootstrap-server localhost:29092 --partitions 2 --replication-factor 1

kafka-topics --create --topic users_rekeyed --bootstrap-server localhost:29092 --partitions 2 --replication-factor 1

kafka-topics --create --topic transactions --bootstrap-server localhost:29092 --partitions 2 --replication-factor 1

kafka-topics --create --topic alerts --bootstrap-server localhost:29092 --partitions 2 --replication-factor 1