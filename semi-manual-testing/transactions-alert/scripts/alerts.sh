kafka-avro-console-consumer --bootstrap-server broker:29092 \
--property parse.key=true \
--topic alerts \
--from-beginning \
--property schema.registry.url=http://localhost:8081