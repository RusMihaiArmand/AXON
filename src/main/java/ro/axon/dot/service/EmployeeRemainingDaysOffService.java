package ro.axon.dot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.*;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.RemainingDaysOff;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class EmployeeRemainingDaysOffService {

    private final EmployeeRepository employeeRepository;
//    private final LegallyDaysOffService legallyDaysOffService;
    private final LeaveRequestRepository leaveRequestRepository;

//    private boolean isWeekend(LocalDate date) {
//        return date.getDayOfWeek().toString().equals("SATURDAY") || date.getDayOfWeek().toString().equals("SUNDAY");
//    }
//
//    private Integer countDaysOffPerRequest(LeaveRequestEty leaveRequest) {
//        List<LocalDate> legalDaysOffInCurrentYear;
//        Integer spentDaysOffInRequest = 0;
//        try {
//            legalDaysOffInCurrentYear = legallyDaysOffService.getOffDays(null, List.of(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))).getDays().stream().map(offDay -> offDay.getDate().toLocalDate()).toList();
//        } catch (NullPointerException e) {
//            legalDaysOffInCurrentYear = new ArrayList<>();
//        }
//        for (LocalDate date = leaveRequest.getStartDate(); !date.isAfter(leaveRequest.getEndDate()); date = date.plusDays(1)) {
//            if (!(isWeekend(date))) {
//                if (!legalDaysOffInCurrentYear.contains(date)) {
//                    spentDaysOffInRequest++;
//                }
//            }
//        }
//        return spentDaysOffInRequest;
//    }

    public RemainingDaysOff getEmployeeRemainingDaysOff(String employeeId) {
        var remainingDaysOff = new RemainingDaysOff();
        Optional<EmployeeEty> employeeOptionalEty;

        employeeOptionalEty = employeeRepository.findById(employeeId);

        if (employeeOptionalEty.isEmpty())
            throw new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build());

        EmployeeEty employee = employeeOptionalEty.get();

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Optional<Integer> totalDaysOff = employee.getEmpYearlyDaysOff().stream().filter(daysOffEntry -> daysOffEntry.getYear().equals(currentYear)).findFirst().map(EmpYearlyDaysOffEty::getTotalNoDays);

        if (totalDaysOff.isEmpty())
            throw new BusinessException(BusinessException.BusinessExceptionElement.builder().errorDescription(BusinessErrorCode.YEARLY_DAYS_OFF_NOT_SET).build());

        remainingDaysOff.setRemainingDays(totalDaysOff.get());

        QLeaveRequestEty root = QLeaveRequestEty.leaveRequestEty;
        LeaveRequestQuery query = new LeaveRequestQuery();
        Iterable<LeaveRequestEty> approvedVacationLeaveRequests = leaveRequestRepository.findAll(root.employee.id.eq(employee.getId())
                .and(query.withStatus(LeaveRequestEtyStatusEnum.APPROVED).withType(LeaveRequestEtyTypeEnum.VACATION).build())
                .or(query.withStatus(LeaveRequestEtyStatusEnum.PENDING).withType(LeaveRequestEtyTypeEnum.VACATION).build()));

        Integer spentDaysOff;
        //  spentDaysOff = StreamSupport.stream(approvedVacationLeaveRequests.spliterator(), false).map(this::countDaysOffPerRequest).reduce(0, Integer::sum);

        spentDaysOff = StreamSupport.stream(approvedVacationLeaveRequests.spliterator(), false).map(request -> ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate())).reduce(0L, Long::sum).intValue();
        remainingDaysOff.setRemainingDays(remainingDaysOff.getRemainingDays() - spentDaysOff);
        return remainingDaysOff;
    }
}
