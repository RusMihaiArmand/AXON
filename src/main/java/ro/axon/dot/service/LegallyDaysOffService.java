package ro.axon.dot.service;


import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axon.dot.config.LegallyDaysOffPersistenceManager;
import ro.axon.dot.mapper.LegallyDaysOffMapper;
import ro.axon.dot.model.LegallyDaysOffList;

@Service
public class LegallyDaysOffService  {

    private final LegallyDaysOffPersistenceManager legallyDaysOffPersistenceManager;


    public LegallyDaysOffService(LegallyDaysOffPersistenceManager legallyDaysOffPersistenceManager) {
        this.legallyDaysOffPersistenceManager = legallyDaysOffPersistenceManager;
    }

    public LegallyDaysOffList getAllLegallyOffDays()
    {
        var offDaysList = new LegallyDaysOffList();

        offDaysList.setDays( legallyDaysOffPersistenceManager.getAllLegallyDaysOffFromDb().stream().map(LegallyDaysOffMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));

        return offDaysList;
    }

    private String convertToYearMonth(LocalDate date){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return date.format(dateTimeFormatter);
    }

    private String convertToYear(LocalDate date){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(dateTimeFormatter);
    }

    private LegallyDaysOffList GetOffDaysFromMonths(List<String> periods)
    {
        LegallyDaysOffList dayListFinal = new LegallyDaysOffList();


        dayListFinal.setDays( legallyDaysOffPersistenceManager.getAllLegallyDaysOffFromDb().stream().
            filter(
                legallyDaysOff ->
                    periods.contains(convertToYearMonth(legallyDaysOff.getDate()))
            ).map(LegallyDaysOffMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));


        return dayListFinal;
    }

    private LegallyDaysOffList GetOffDaysFromYears(List<String> years)
    {
        LegallyDaysOffList dayListFinal = new LegallyDaysOffList();

        dayListFinal.setDays( legallyDaysOffPersistenceManager.getAllLegallyDaysOffFromDb().stream().
            filter(
                legallyDaysOff ->
                    years.contains(convertToYear(legallyDaysOff.getDate()))
            ).map(LegallyDaysOffMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));

        return dayListFinal;
    }


    @Transactional(readOnly = true)
    public LegallyDaysOffList getOffDays(List<String> periods, List<String> years) {

        if(periods!=null)
            if(periods.size()==0)
                periods = null;


        if(years!=null)
            if(years.size()==0)
                years = null;

        if(periods==null && years==null)
        {
            return this.getAllLegallyOffDays();
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
