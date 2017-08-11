package sparkswood.kafka;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleProducer {

    public static void main(final String[] args) throws JsonProcessingException {
        final Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, "simpleProducer1");
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try(final KafkaProducer<String, String> producer = new KafkaProducer<>(configs);) {

            final Random r = new Random(System.currentTimeMillis());

            final ObjectMapper m = new ObjectMapper();

            for(int i=0;i<99;i++) {
                final String key = String.format("k%s", new DecimalFormat("00").format(i));

                final Map<String, Integer> obj = new HashMap<>();
                for(int j=0;j<9;j++) {
                    obj.put("p" + j, r.nextInt(100));
                }

                final String value = m.writeValueAsString(obj);
                producer.send(
                        new ProducerRecord<>("TEST", key, value),
                        (rmd,e)->{
                            if(e!=null) {
                                System.out.println("PROBLEM : " + key);
                            } else {
                                System.out.println("ACK : " + key + " : "+ rmd.offset());
                            }
                        });
            }
        }

    }

}
