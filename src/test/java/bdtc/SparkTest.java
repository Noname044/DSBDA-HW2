package bdtc;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static bdtc.StatisticCounter.countStatisticPerYear;

public class SparkTest {

    final String testStringA1 = "2020-04-29 08:00:00|NRNU MEPhI|11111111|-1\n";
    final String testStringA2 = "2020-04-29 13:00:00|NRNU MEPhI|11111111|1\n";
    final String testStringA3 = "2021-04-29 00:00:00|NRNU MEPhI|11111111|-1\n";
    final String testStringA4 = "2021-04-29 08:00:00|NRNU MEPhI|11111111|1\n";
    final String testStringA5 = "2021-04-30 03:00:00|MSU|33355700|-1\n";
    final String testStringA6 = "2021-04-30 23:00:00|MSU|33355700|1\n";

    final String testStringP1 = "2020-04-30 14:01:40|NRNU MEPhI|22222222|3333333\n";
    final String testStringP2 = "2020-05-30 14:01:40|NRNU MEPhI|22222222|4444444\n";
    final String testStringP3 = "2021-05-30 14:02:40|NRNU MEPhI|11111111|5555555\n";
    final String testStringP4 = "2020-05-30 14:02:40|NRNU MEPhI|11111111|6666666\n";

    SparkSession ss = SparkSession
            .builder()
            .master("local")
            .appName("SparkSQLApplication")
            .getOrCreate();

    @Test
    public void testOneData() {

        JavaSparkContext sc = new JavaSparkContext(ss.sparkContext());
        JavaRDD<String> attendances = sc.parallelize(Arrays.asList(testStringA1, testStringA2));
        JavaRDD<String> publications = sc.parallelize(Arrays.asList(testStringP4));
        JavaRDD<Row> result = countStatisticPerYear(ss.createDataset(attendances.rdd(), Encoders.STRING()), ss.createDataset(publications.rdd(), Encoders.STRING()));
        List<Row> rowList = result.collect();

        assert rowList.get(0).toString().equals("[2020,11111111,1,5]");
    }

    @Test
    public void testSameData() {

        JavaSparkContext sc = new JavaSparkContext(ss.sparkContext());
        JavaRDD<String> attendances = sc.parallelize(Arrays.asList(testStringA1, testStringA2, testStringA1, testStringA2));
        JavaRDD<String> publications = sc.parallelize(Arrays.asList(testStringP4, testStringP4));
        JavaRDD<Row> result = countStatisticPerYear(ss.createDataset(attendances.rdd(), Encoders.STRING()), ss.createDataset(publications.rdd(), Encoders.STRING()));
        List<Row> rowList = result.collect();

        assert rowList.get(0).toString().equals("[2020,11111111,1,5]");
    }

    @Test
    public void testMegaBigData() {

        JavaSparkContext sc = new JavaSparkContext(ss.sparkContext());
        JavaRDD<String> attendances = sc.parallelize(Arrays.asList(testStringA1, testStringA2, testStringA3, testStringA4, testStringA5, testStringA6));
        JavaRDD<String> publications = sc.parallelize(Arrays.asList(testStringP1, testStringP2, testStringP3));
        JavaRDD<Row> result = countStatisticPerYear(ss.createDataset(attendances.rdd(), Encoders.STRING()), ss.createDataset(publications.rdd(), Encoders.STRING()));
        List<Row> rowList = result.collect();

        assert rowList.get(0).toString().equals("[2020,11111111,0,5]");
        assert rowList.get(1).toString().equals("[2020,22222222,2,0]");
        assert rowList.get(2).toString().equals("[2021,11111111,1,8]");
        assert rowList.get(3).toString().equals("[2021,33355700,0,20]");
    }

}
