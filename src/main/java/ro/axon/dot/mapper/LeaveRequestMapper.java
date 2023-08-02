package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.model.LeaveRequestDetailsListItem;

/**
 * Mapper used for converting LeaveRequestEty object to LeaveRequestDto object
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LeaveRequestMapper {

    LeaveRequestMapper INSTANCE = Mappers.getMapper(LeaveRequestMapper.class);

    LeaveRequestDetailsListItem mapLeaveRequestEtyToLeaveRequestDto(LeaveRequestEty leaveRequestEty);

}
