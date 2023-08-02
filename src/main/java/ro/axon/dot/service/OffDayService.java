package ro.axon.dot.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.OffDayRepository;
import ro.axon.dot.mapper.OffDayMapper;
import ro.axon.dot.model.OffDayList;
import ro.axon.dot.model.OffDayListItem;

@Service
@RequiredArgsConstructor
public class OffDayService {

    private final OffDayRepository offDayRepository;



    public OffDayList getOffDays(List<String> periods, List<String> years) {

        var offDaysList = new OffDayList();
        offDaysList.setDays(offDayRepository.findAll().stream().map(OffDayMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));


        if(periods!=null)
            if(periods.size()!=2)
                periods = null;
        if(years!=null)
            if(years.size()!=2)
                years = null;


        if(periods==null && years==null)
            return offDaysList;


        Date date_start, date_end;

        if(years!=null)
        {
            date_start = Date.valueOf(years.get(0)+"-01-01");
            date_end = Date.valueOf(years.get(1)+"-12-31");
        }
        else
        {
            date_start = Date.valueOf(periods.get(0)+"-01");

            LocalDate end = LocalDate.parse(periods.get(1)+"-01");
            int day = end.getMonth().length(end.isLeapYear());
            date_end = Date.valueOf(periods.get(1)+"-"+day);
        }


        OffDayList dayListFinal = new OffDayList();

        for (OffDayListItem checkDay: offDaysList.getDays()) {

            if ((checkDay.getDate().equals(date_start) || checkDay.getDate().after(date_start)) &&
                (checkDay.getDate().equals(date_end) || checkDay.getDate().before(date_end))) {
                dayListFinal.addDay(checkDay);
            }
        }

        return dayListFinal;
    }

}
