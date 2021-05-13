package bdtc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class attendanceStatisticYear {

    /**
     * Year of statistic attendances
     * of students or employees
     */
    private int year;

    /**
     * Employee or Student identifier
     */
    private int personId;

    /**
     * Timestamp of entering or exiting
     */
    private int timestamp;

    /**
     * Flag of entering or exiting
     */
    private int enterExit;
}

