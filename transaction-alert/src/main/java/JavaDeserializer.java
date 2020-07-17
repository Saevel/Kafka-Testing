import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class JavaDeserializer<T> implements Deserializer<T> {

    @Override
    public T deserialize(String topic, byte[] data) {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Cannot deserialize bytes to class");
        }
    }
}
