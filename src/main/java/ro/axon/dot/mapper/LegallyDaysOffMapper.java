package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.LegallyDaysOffEty;
import ro.axon.dot.model.LegallyDaysOffItem;


@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LegallyDaysOffMapper {

    LegallyDaysOffMapper INSTANCE = Mappers.getMapper(LegallyDaysOffMapper.class);

    LegallyDaysOffItem mapTeamEtyToTeamDto(LegallyDaysOffEty legallyDaysOffEty);

}
