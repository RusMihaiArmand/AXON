package ro.axon.dot.service;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.LegallyDaysOffRepository;
import ro.axon.dot.mapper.LegallyDaysOffMapper;
import ro.axon.dot.model.LegallyDaysOffList;
import ro.axon.dot.model.LegallyDaysOffItem;

@Service
@RequiredArgsConstructor
public class LegallyDaysOffService  {

    private final LegallyDaysOffRepository legallyDaysOffRepository;

    public LegallyDaysOffList GetAllOffDays()
    {
        var offDaysList = new LegallyDaysOffList();
        offDaysList.setDays( legallyDaysOffRepository.findAll().stream().map(LegallyDaysOffMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));

        return offDaysList;
    }


    private LegallyDaysOffList GetOffDaysFromMonths(List<String> periods)
    {
        LegallyDaysOffList dayListFinal = new LegallyDaysOffList();
        dayListFinal.setDays( new ArrayList<>());

        LegallyDaysOffList allOffDays = this.GetAllOffDays();

        for (LegallyDaysOffItem day: allOffDays.getDays()) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
            String date = simpleDateFormat.format(day.getDate());
            if(periods.contains(date))
            {
                dayListFinal.addDay(day);
            }
        }
        return dayListFinal;
    }

    private LegallyDaysOffList GetOffDaysFromYears(List<String> years)
    {
        LegallyDaysOffList dayListFinal = new LegallyDaysOffList();
        dayListFinal.setDays( new ArrayList<>());

        LegallyDaysOffList allOffDays = this.GetAllOffDays();

        for (LegallyDaysOffItem day: allOffDays.getDays()) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
            String date = simpleDateFormat.format(day.getDate());
            if(years.contains(date))
            {
                dayListFinal.addDay(day);
            }
        }
        return dayListFinal;
    }


    public LegallyDaysOffList getOffDays(List<String> periods, List<String> years) {

        if(periods!=null)
            if(periods.size()==0)
                periods = null;


        if(years!=null)
            if(years.size()==0)
                years = null;

        if(periods==null && years==null)
        {
            return this.GetAllOffDays();
        }
        else
        {
            if(years!=null)
            {
                return this.GetOffDaysFromYears(years);
            }
            else {
                return this.GetOffDaysFromMonths(periods);
            }
        }

    }

}
