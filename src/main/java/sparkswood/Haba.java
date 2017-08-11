package sparkswood;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class Haba {

    public static void main(final String[] args) throws IOException {
        // TODO Auto-generated method stub

        final SparkConf sparkConf = new SparkConf().setAppName("Haba");
        sparkConf.set("hbase.zookeeper.quorum", "guest136.hadoop.jac,guest142.hadoop.jac,guest148.hadoop.jac");
        final JavaSparkContext sc = new JavaSparkContext(sparkConf);

        final Configuration conf = HBaseConfiguration.create();
        final String tableName = "tkn_vault_DEV";
        conf.set("hbase.zookeeper.quorum", "guest136.hadoop.jac,guest142.hadoop.jac,guest148.hadoop.jac");
        conf.set("zookeeper.znode.parent", "/hbase-unsecure");
        conf.set("hbase.master", "guest138.hadoop.jac:16000");
        conf.setInt("timeout", 120000);
        conf.set("zookeeper.znode.parent", "/hbase-unsecure");
        conf.set(TableInputFormat.INPUT_TABLE, tableName);

        // conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        // conf.registerKryoClasses(org.apache.hadoop.hbase.client.Result);

        final JavaPairRDD<ImmutableBytesWritable, Result> rdd = sc.newAPIHadoopRDD(
                conf,
                TableInputFormat.class,
                org.apache.hadoop.hbase.io.ImmutableBytesWritable.class,
                org.apache.hadoop.hbase.client.Result.class
              );

        // System.err.println("count : " + rdd.count() );
        final Tuple2<ImmutableBytesWritable, Result> tuple = rdd.first();
        System.err.println(tuple._1 + "=" + tuple._2);
        sc.close();

    }

}
