package prv.saevel.kafka.academy.automation;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Pair<K, V> {
    private K key;

    private V value;
}
