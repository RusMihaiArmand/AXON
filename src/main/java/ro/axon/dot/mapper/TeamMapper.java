package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.entity.TeamEty;
import ro.axon.dot.model.TeamDetailsListItem;

/**
 * Mapper used for converting TeamEty object to TeamDto object
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TeamMapper {

    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    TeamDetailsListItem mapTeamEtyToTeamDto(TeamEty teamEty);
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "employees", ignore = true)
    TeamEty mapTeamDtoToTeamEty(TeamDetailsListItem teamDto);
}
