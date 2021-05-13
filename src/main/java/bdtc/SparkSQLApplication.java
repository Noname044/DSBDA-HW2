package bdtc;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Counts the number of publications for the year;
 * the total time spent in the organization in hours for the year.
 */
@Slf4j
public class SparkSQLApplication {

    /**
     * @param args - args[0]: input file attendances,
     *               args[1]: input file publications,
     *               args[2] - output directory
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            throw new RuntimeException("Usage: java -jar SparkSQLApplication.jar attendances.file " +
                    "publications.file outputDirectory");
        }

        log.info("Appliction started!");
        log.debug("Application started");
        SparkSession sc = SparkSession
                .builder()
                .master("local")
                .appName("SparkSQLApplication")
                .getOrCreate();

        Dataset<String> dfa = sc.read().text(args[0]).as(Encoders.STRING());
        Dataset<String> dfp = sc.read().text(args[1]).as(Encoders.STRING());
        log.info("===============COUNTING ATTENDANCES...================");
        JavaRDD<Row> result = StatisticCounter.countStatisticPerYear(dfa, dfp);
        log.info("============SAVING FILE TO " + args[2] + " directory============");
        result.saveAsTextFile(args[2]);
    }
}