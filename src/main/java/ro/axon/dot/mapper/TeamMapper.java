package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.model.TeamDetailsListItem;

/**
 * Mapper used for converting TeamEty object to TeamDto object
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TeamMapper {

    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    TeamDetailsListItem mapTeamEtyToTeamDto(TeamEty teamEty);
}
