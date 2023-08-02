package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.OffDayEty;
import ro.axon.dot.model.OffDayListItem;


@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OffDayMapper {

    OffDayMapper INSTANCE = Mappers.getMapper(OffDayMapper.class);

    OffDayListItem mapTeamEtyToTeamDto(OffDayEty offDayEty);

}
