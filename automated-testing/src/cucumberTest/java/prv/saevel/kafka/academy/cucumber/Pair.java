package prv.saevel.kafka.academy.cucumber;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@AllArgsConstructor
@Data
public class Pair<K, V> {

    private K key;

    private V value;

    @Override
    public boolean equals(Object o){
        if(o instanceof Pair){
            Pair p = (Pair) o;
            return key.equals(p.key) && value.equals(p.value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(key, value);
    }
}
