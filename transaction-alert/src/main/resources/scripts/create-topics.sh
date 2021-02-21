kafka-topics --create --topic splitter-input --partitions 2 --replication-factor 1

kafka-topics --create --topic splitter-output --partitions 2 --replication-factor 1

kafka-topics --create --topic users --partitions 2 --replication-factor 1

kafka-topics --create --topic users_rekeyed --partitions 2 --replication-factor 1

kafka-topics --create --topic transactions --partitions 2 --replication-factor 1

kafka-topics --create --topic alerts --partitions 2 --replication-factor 1