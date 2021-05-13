package bdtc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class publicationStatisticYear {

    /**
     * Year of publication activities
     * of students or employees
     */
    private int year;

    /**
     * Employee or Student identifier
     */
    private String personId;

    /**
     * Publication identifier
     */
    private String publicationId;
}

