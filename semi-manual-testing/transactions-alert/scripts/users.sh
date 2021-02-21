kafka-avro-console-producer --topic users \
--broker-list broker:29092 \
--property key.serializer=org.apache.kafka.common.serialization.LongSerializer \
--property parse.key=true \
--property key.schema='{"type":"long"}' \
--property key.separator=":" \
--property value.schema='{"type": "record","name": "User","namespace": "prv.saevel.kafka.academy.testing.transaction.alert", "fields": [{"name": "id", "type": "long"}, {"name": "email", "type": ["null", "string"], "default": null}, {"name": "phoneNumber", "type": ["null", "string"], "default": null}]}'
