package prv.saevel.kafka.academy.cucumber.dsl;

import org.hamcrest.Matcher;
import prv.saevel.kafka.academy.cucumber.Pair;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KeyValuePairAssertions {

    private KeyValuePairAssertions(){}

    public static <K, V> void assertKeysAndValues(List<Pair<K, V>> data,
                                                  Matcher<Iterable<? super Pair<K, V>>> assertion,
                                                  String msg){
        assertThat(msg, data, assertion);
    }


    public static <K, V> void assertKeysAndValues(List<Pair<K, V>> data,
                                                   Matcher<Iterable<? super Pair<K, V>>> assertion){
        assertKeysAndValues(data, assertion,"Key-Value pair does not match");
    }

    public static <K, V> void expectKeyAndValue(List<Pair<K, V>> data, K key, V value){
        assertKeysAndValues(data, hasItem(equalTo(new Pair<>(key, value))));
    }

    public static <K, V> void expectKeyAndValue(List<Pair<K, V>> data, K key, V value, String message){
        assertKeysAndValues(data, hasItem(equalTo(new Pair<>(key, value))), message);
    }

    public static <K, V> void expectNoSuchKeyAndValue(List<Pair<K, V>> data, K key, V value){
        assertKeysAndValues(data, not(hasItem(equalTo(new Pair<>(key, value)))));
    }

    public static <K, V> void expectNoSuchKeyAndValue(List<Pair<K, V>> data, K key, V value, String message){
        assertKeysAndValues(data, not(hasItem(equalTo(new Pair<>(key, value)))), message);
    }
}
