package sparkswood.kafka;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import scala.Tuple2;

public class KafkaStreaming {

    public static void main(final String[] args) throws Exception {
        final SparkConf sparkConf = new SparkConf()
                .setAppName("KafkaStreaming")
                .set("spark.ui.enabled", "false");
        try( final JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(10)) ) {

            final Map<String, Object> kafkaParams = new HashMap<>();
            kafkaParams.put("bootstrap.servers", "127.0.0.1:9092");
            kafkaParams.put("key.deserializer", StringDeserializer.class);
            kafkaParams.put("value.deserializer", StringDeserializer.class);
            kafkaParams.put("group.id", "my_kafka_streaming_id_1");
            kafkaParams.put("auto.offset.reset", "latest");
            kafkaParams.put("enable.auto.commit", false);

            final Collection<String> topics = Arrays.asList("TEST");

            final JavaInputDStream<ConsumerRecord<String, String>> stream =
                    KafkaUtils.createDirectStream(
                            streamingContext,
                            LocationStrategies.PreferConsistent(),
                            ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                            );

            final JavaPairDStream<String, String> data = stream.mapToPair( record->new Tuple2<>(record.key(), record.value()) );
            data.print();

            final ObjectMapper mapper = new ObjectMapper();
            data.foreachRDD( p->p.collect().forEach(t-> {
                final Map<String, Integer> map = new HashMap<>();
                try {
                    map.putAll( mapper.readValue(t._2, new TypeReference<Map<String,Integer>>() {}) );
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                map.forEach((k,v)->System.out.println(String.format("%s:%s=%s",t._1, k, v)));

                // System.out.println(String.format("%s=%s",t._1, t._2));
            }) );

            streamingContext.start();
            streamingContext.awaitTermination();
        }
    }

}
