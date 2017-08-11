package sparkswood;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

/**
 * @author tpauvreau
 *
 */
public class Flint {

    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: JavaNetworkWordCount <hostname> <port>");
            System.exit(1);
        }

        //StreamingExamples.setStreamingLogLevels();

        // Create the context with a 1 second batch size
        final SparkConf sparkConf = new SparkConf().setAppName("JavaNetworkWordCount");
        try(final JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, Durations.seconds(1))) {

            // Create a JavaReceiverInputDStream on target ip:port and count the
            // words in input stream of \n delimited text (eg. generated by 'nc')
            // Note that no duplication in storage level only for running locally.
            // Replication necessary in distributed scenario for fault tolerance.
            final JavaReceiverInputDStream<String> lines = ssc.socketTextStream(args[0], Integer.parseInt(args[1]), StorageLevels.MEMORY_AND_DISK_SER);
            // final JavaDStream<String> words = lines.flatMap( x-> Arrays.asList(SPACE.split(x)) );
            final JavaDStream<String> words = lines.flatMap( x->Arrays.asList(SPACE.split(x)).iterator() );
            final JavaPairDStream<String, Integer> wordCounts = words
                    .mapToPair(s->new Tuple2<>(s, 1))
                    .reduceByKey((i1,i2)->i1 + i2);

            wordCounts.print();
            ssc.start();
            ssc.awaitTermination();
        }
    }
}
