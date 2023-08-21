package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.entity.LeaveRequestEty;
import ro.axon.dot.model.LeaveRequestDetailsListItem;

/**
 * Mapper used for converting LeaveRequestEty object to LeaveRequestDto object
 */
@Mapper(uses = EmployeeIdentificationMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LeaveRequestMapper {

    LeaveRequestMapper INSTANCE = Mappers.getMapper(LeaveRequestMapper.class);

    @Mapping(source = "employee", target = "employeeDetails")
    LeaveRequestDetailsListItem mapLeaveRequestEtyToLeaveRequestDto(LeaveRequestEty leaveRequestEty);

}
