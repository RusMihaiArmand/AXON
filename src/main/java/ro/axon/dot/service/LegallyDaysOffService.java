package ro.axon.dot.service;

import static java.util.Arrays.asList;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.LegallyDaysOffRepository;
import ro.axon.dot.mapper.LegallyDaysOffMapper;
import ro.axon.dot.model.LegallyDaysOffList;
import ro.axon.dot.model.LegallyDaysOffItem;

@Service
@RequiredArgsConstructor
public class LegallyDaysOffService implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

    private final LegallyDaysOffRepository legallyDaysOffRepository;


    @Cacheable("daysOff")
    public LegallyDaysOffList GetAllOffDays()
    {
        return CacheAllOffDays();
    }

    @CachePut("daysOff")
    public LegallyDaysOffList CacheAllOffDays()
    {
        var offDaysList = new LegallyDaysOffList();
        offDaysList.setDays( legallyDaysOffRepository.findAll().stream().map(LegallyDaysOffMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));

        return offDaysList;
    }

    public LegallyDaysOffList getOffDays(List<String> periods, List<String> years) {

        var offDaysList =  this.GetAllOffDays() ;


        if(periods!=null)
            if(periods.size()!=2)
            {
                if(periods.size()==1)
                    periods.add(periods.get(0));
                else
                    periods = null;
            }


        if(years!=null)
            if(years.size()!=2)
            {
                if(years.size()==1)
                    years.add(years.get(0));
                else
                    years = null;
            }


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


        LegallyDaysOffList dayListFinal = new LegallyDaysOffList();
        dayListFinal.setDays( new ArrayList<LegallyDaysOffItem>());

        for (LegallyDaysOffItem checkDay: offDaysList.getDays()) {

            if ((checkDay.getDate().equals(date_start) || checkDay.getDate().after(date_start)) &&
                (checkDay.getDate().equals(date_end) || checkDay.getDate().before(date_end))) {
                dayListFinal.addDay(checkDay);
            }
        }

        return dayListFinal;
    }

    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        cacheManager.setCacheNames(asList("daysOff"));
    }
}
