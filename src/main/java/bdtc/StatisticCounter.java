package bdtc;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;

@AllArgsConstructor
@Slf4j
public class  StatisticCounter{

    private static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .toFormatter();

    /**
     * Function ounts the number of publications for the year;
     * the total time spent in the organization in hours for the year.
     * Parses publication and attendance statistic strings.
     * @param attendancesDataset - input DataSet with attendance statistics
     * @param publicationsDataset - input DataSet with publication statistics
     * @return the result of counting in format JavaRDD
     */
    public static JavaRDD<Row> countStatisticPerYear(Dataset<String> attendancesDataset,
                                                     Dataset<String> publicationsDataset) {

        //Parse publications statistic and drop duplicates
        Dataset<String> publicationsWords = publicationsDataset.map(s -> Arrays.toString(s.split("\n")), Encoders.STRING());
        Dataset<publicationStatisticYear> publicationsYearDataset = publicationsWords.map(s -> {
            s = s.split("\\[")[1];
            s = s.split("\\]")[0];
            String[] attendanceLog = s.split("\\|");
            LocalDateTime date = LocalDateTime.parse(attendanceLog[0], formatter);
            return new publicationStatisticYear(date.getYear(), attendanceLog[2], attendanceLog[3]);
        }, Encoders.bean(publicationStatisticYear.class))
                .coalesce(1)
                .dropDuplicates();

        //Group by year and person Id.
        Dataset<Row> p = publicationsYearDataset.groupBy("year", "personId")
                .count()
                .toDF("year", "personId", "countPub")
                .sort(functions.asc("year"));

        //Parse attendances statistic and drop duplicates
        Dataset<String> attendancesWords = attendancesDataset.map(s -> Arrays.toString(s.split("\n")), Encoders.STRING());
        Dataset<attendanceStatisticYear> attendancesYearDataset = attendancesWords.map(s -> {
            s = s.split("\\[")[1];
            s = s.split("\\]")[0];
            String[] attendanceLog = s.split("\\|");
            LocalDateTime date = LocalDateTime.parse(attendanceLog[0], formatter);

            return new attendanceStatisticYear(date.getYear(), Integer.parseInt(attendanceLog[2]), date.getHour()*Integer.parseInt(attendanceLog[3]), Integer.parseInt(attendanceLog[3]));
        }, Encoders.bean(attendanceStatisticYear.class))
                .coalesce(1)
                .dropDuplicates();

        //Group by year and person Id.
        Dataset<Row> a = attendancesYearDataset.groupBy("year", "personId")
                .sum("timestamp")
                .toDF("year", "personId", "hours")
                .sort(functions.asc("year"));

        //Join datasets and beautify output
        a.registerTempTable("attendances");
        p.registerTempTable("publications");

        Dataset<Row> t = a.sqlContext().sql("SELECT " +
                "CASE " +
                "WHEN a.year is null " +
                "THEN p.year " +
                "ELSE a.year " +
                "END AS year, " +
                "CASE " +
                "WHEN a.personId is null " +
                "THEN p.personId " +
                "ELSE a.personId " +
                "END AS personId, " +
                "CASE " +
                "WHEN p.countPub is null " +
                "THEN 0 " +
                "ELSE p.countPub " +
                "END AS countPub, " +
                "CASE " +
                "WHEN a.hours is null " +
                "THEN 0 " +
                "ELSE a.hours " +
                "END AS hours " +
                "FROM attendances a FULL JOIN publications p ON a.year = p.year and a.personId = p.personId");

        log.info("===========RESULT=========== ");
        t.show();
        return t.toJavaRDD();
    }

}
