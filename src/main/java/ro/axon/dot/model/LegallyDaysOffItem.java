package ro.axon.dot.model;

import java.sql.Date;
import lombok.Data;

@Data
public class LegallyDaysOffItem {

    private Date date;
    private String desc;

}
