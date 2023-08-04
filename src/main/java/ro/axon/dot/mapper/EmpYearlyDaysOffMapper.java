package ro.axon.dot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ro.axon.dot.domain.EmpYearlyDaysOffEty;
import ro.axon.dot.model.EmpYearlyDaysOffDetailsListItem;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmpYearlyDaysOffMapper {
  EmpYearlyDaysOffMapper INSTANCE = Mappers.getMapper(EmpYearlyDaysOffMapper.class);

  EmpYearlyDaysOffDetailsListItem mapEmpYearlyDaysOffToEmpYearlyDaysOffDto(EmpYearlyDaysOffEty empYearlyDaysOffEty);
}
