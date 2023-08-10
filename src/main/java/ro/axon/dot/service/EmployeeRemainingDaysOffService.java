package ro.axon.dot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.*;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.mapper.EmployeeMapper;
import ro.axon.dot.model.EmployeeDetailsListItem;
import ro.axon.dot.model.RemainingDaysOff;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeRemainingDaysOffService {

    private final EmployeeRepository employeeRepository;
    private final LegallyDaysOffService legallyDaysOffService;
    private final LeaveRequestRepository leaveRequestRepository;

    public RemainingDaysOff getEmployeeRemainingDaysOff(String employeeId) {
        var remainingDaysOff = new RemainingDaysOff();
        EmployeeEty employeeEty;

        try {
            employeeEty = employeeRepository.findAll().stream().filter(e -> e.getId().equals(employeeId)).toList().get(0);
        } catch (Exception e) {
            throw new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build());
        }

        EmployeeDetailsListItem employee = EmployeeMapper.INSTANCE.mapEmployeeEtyToEmployeeDto(employeeEty);

        if (employee.getTotalVacationDays() == 0)
            throw new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET).build());

        remainingDaysOff.setRemainingDays(employee.getTotalVacationDays());

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        List<LocalDate> legalDaysOffInCurrentYear;
        try {
            legalDaysOffInCurrentYear = legallyDaysOffService.getOffDays(null, List.of(currentYear)).getDays().stream().map(offDay -> offDay.getDate().toLocalDate()).toList();
        } catch (NullPointerException e) {
            legalDaysOffInCurrentYear = new ArrayList<>();
        }

        QLeaveRequestEty root = QLeaveRequestEty.leaveRequestEty;
        LeaveRequestQuery query = new LeaveRequestQuery();
        Iterable<LeaveRequestEty> approvedVacationLeaveRequests = leaveRequestRepository.findAll(root.employee.id.eq(employee.getId()).and(query.withStatus(LeaveRequestEtyStatusEnum.APPROVED).withType(LeaveRequestEtyTypeEnum.VACATION).build()));

        for (LeaveRequestEty leaveRequest : approvedVacationLeaveRequests) {
            for (LocalDate date = leaveRequest.getStartDate(); !date.isAfter(leaveRequest.getEndDate()); date = date.plusDays(1)) {
                if (!(date.getDayOfWeek().toString().equals("SATURDAY") || date.getDayOfWeek().toString().equals("SUNDAY"))) {
                    if (!legalDaysOffInCurrentYear.contains(date)) {
                        remainingDaysOff.setRemainingDays(remainingDaysOff.getRemainingDays() - 1);
                    }
                }
            }
        }

        return remainingDaysOff;
    }
}
