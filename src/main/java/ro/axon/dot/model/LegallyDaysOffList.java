package ro.axon.dot.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LegallyDaysOffList {

    private List<LegallyDaysOffItem> days;

    public void addDay(LegallyDaysOffItem day)
    {
        if(days == null)
            days = new ArrayList<>();

        days.add(day);
    }
}
