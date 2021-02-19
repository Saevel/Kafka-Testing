kafka-avro-console-producer --topic transactions \
--broker-list broker:29092 \
--property key.schema='{"type": "long"}' \
--property parse.key=true \
--property key.separator=":"\
--property value.schema='{"type": "record","name": "Transaction", "namespace": "prv.saevel.kafka.academy.testing.transaction.alert", "fields": [{"name": "id", "type": "long"}, {"name": "amount", "type": "double", "default": 0}, {"name": "outgoing", "type": "boolean"}, {"name": "type", "type": "string"}, {"name": "country", "type": ["null", "string"], "default": null}, {"name": "userId", "type": ["null", "long"], "default": null}]}'