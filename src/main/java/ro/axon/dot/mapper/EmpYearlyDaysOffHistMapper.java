package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.EmpYearlyDaysOffHistEty;
import ro.axon.dot.model.EmpYearlyDaysOffHistDetailsListItem;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmpYearlyDaysOffHistMapper {

  EmpYearlyDaysOffHistMapper INSTANCE = Mappers.getMapper(EmpYearlyDaysOffHistMapper.class);

  EmpYearlyDaysOffHistDetailsListItem mapEmpYearlyDaysOffHistEtyToEmpYearlyDaysOffHistDto(
      EmpYearlyDaysOffHistEty empYearlyDaysOffHistEty);

}
