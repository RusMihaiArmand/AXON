package ro.axon.dot.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OffDayList {

    private List<OffDayListItem> days;

    public void addDay(OffDayListItem day)
    {
        if(days == null)
            days = new ArrayList<>();

        days.add(day);
    }
}
