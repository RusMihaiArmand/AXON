package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.model.EmployeeIdentificationDetailsListItem;

/**
 * Mapper used for converting LeaveRequestEty object to LeaveRequestDto object
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeeIdentificationMapper {

    EmployeeIdentificationMapper INSTANCE = Mappers.getMapper(EmployeeIdentificationMapper.class);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "id", target = "employeeId")
    EmployeeIdentificationDetailsListItem mapEmployeeEtyToEmployeeIdentificationDto(EmployeeEty employeeEty);

}