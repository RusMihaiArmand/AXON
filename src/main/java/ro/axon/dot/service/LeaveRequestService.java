package ro.axon.dot.service;

import com.querydsl.core.types.dsl.BooleanExpression;
<<<<<<< Updated upstream
=======
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.domain.entity.LeaveRequestEty;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.LeaveRequestDetailsList;

import java.time.LocalDate;
>>>>>>> Stashed changes
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axon.dot.domain.entity.LeaveRequestEty;
import ro.axon.dot.domain.repositories.LeaveRequestRepository;
<<<<<<< Updated upstream
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.LeaveRequestDetailsList;
=======
import ro.axon.dot.model.LeaveRequestReport;
import ro.axon.dot.model.LeaveRequestReportItem;
>>>>>>> Stashed changes

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;

    @Transactional(readOnly = true)
    public LeaveRequestDetailsList getLeaveRequestsDetailsSorted(BooleanExpression query) {
        var leaveRequestDetailsList = new LeaveRequestDetailsList();
        Comparator<LeaveRequestEty> leaveRequestStatusComparator = Comparator.comparing(LeaveRequestEty::getStatus);
        Comparator<LeaveRequestEty> leaveRequestCrtTmsComparator = Comparator.comparing(LeaveRequestEty::getCrtTms);
        Iterable<LeaveRequestEty> filteredRepo = leaveRequestRepository.findAll(query);
        leaveRequestDetailsList.setItems(StreamSupport.stream(filteredRepo.spliterator(), false)
            .filter(Objects::nonNull)
            .sorted(leaveRequestStatusComparator.thenComparing(leaveRequestCrtTmsComparator))
            .map(LeaveRequestMapper.INSTANCE::mapLeaveRequestEtyToLeaveRequestDto)
            .collect(Collectors.toList()));
        return leaveRequestDetailsList;
    }

<<<<<<< Updated upstream
}
=======


    public LeaveRequestReport generateLeaveRequestReport(String teamName, LocalDate startDate,
                                                         LocalDate endDate) {
        LeaveRequestReport leaveRequestReport= new LeaveRequestReport();
        List<EmployeeEty> employees = employeeService.getEmployeeFromTeam(teamName);

        employees = employees.stream()
                .filter(employeeEty ->
                        employeeEty.getLeaveRequests().stream()
                                .anyMatch(leaveRequestEty ->
                                        (leaveRequestEty.getStartDate().isBefore(endDate) ||
                                                leaveRequestEty.getEndDate().isAfter(startDate))
                                                && (leaveRequestEty.getStatus().equals(LeaveRequestStatus.APPROVED))
                                )
                )
                .collect(Collectors.toList());



        for (EmployeeEty employee : employees){
            LeaveRequestReportItem reportItem = new LeaveRequestReportItem();
            reportItem.setFirstName(employee.getFirstName());
            reportItem.setLastName(employee.getLastName());

            List<LeaveRequestEty> employeeLeaveByPeriod = employeeService.getEmployeeLeaveRequestByPeriod(
                    startDate, endDate, employee);

            int noOfVacationDays = calculateVacationDays(employee.getLeaveRequests().stream().toList(), startDate, endDate);
            int noOfMedicalDays = calculateMedicalDays(employee.getLeaveRequests().stream().toList(), startDate, endDate);

            reportItem.setNoOfMedicalDays(noOfMedicalDays);
            reportItem.setNoOfVacationDays(noOfVacationDays);
            reportItem.getLeaveRequests().setItems(employeeLeaveByPeriod.stream().map(LeaveRequestMapper.INSTANCE::
                    mapLeaveRequestEtyToLeaveRequestDto).collect(Collectors.toList()));

            leaveRequestReport.getItems().add(reportItem);
        }
        return leaveRequestReport;
    }

    private int calculateVacationDays(List<LeaveRequestEty> leaveRequests, LocalDate startDate,
                                      LocalDate endDate) {
        int totalVacationDays = 0;
        for (LeaveRequestEty leaveRequest : leaveRequests) {
            if (leaveRequest.getType().equals(LeaveRequestType.VACATION) && (leaveRequest.getStatus().equals(LeaveRequestStatus.APPROVED))) {
                if (leaveRequest.getStartDate().isAfter(startDate) && leaveRequest.getEndDate().isBefore(endDate)) {
                    totalVacationDays += leaveRequest.getNoDays();
                } else if (leaveRequest.getStartDate().isBefore(startDate) && leaveRequest.getEndDate().isBefore(endDate)) {
                    totalVacationDays += (int) employeeService.calculateCountedDaysOff(startDate, leaveRequest.getEndDate());
                } else if (leaveRequest.getEndDate().isAfter(endDate) && leaveRequest.getStartDate().isBefore(endDate)) {
                    totalVacationDays += (int) employeeService.calculateCountedDaysOff(endDate, leaveRequest.getStartDate());
                }
            }
        }
        return totalVacationDays;
    }

    private int calculateMedicalDays(List<LeaveRequestEty> leaveRequests, LocalDate startDate,
                                     LocalDate endDate){
         int totalMedicalDays = 0;
         for(LeaveRequestEty leaveRequest : leaveRequests){
             if(leaveRequest.getType().equals(LeaveRequestType.MEDICAL) && (leaveRequest.getStatus().equals(LeaveRequestStatus.APPROVED))){
                 if(leaveRequest.getStartDate().isAfter(startDate) && leaveRequest.getEndDate().isBefore(endDate)){
                     totalMedicalDays += (int)employeeService.calculateCountedDaysOff(leaveRequest.getStartDate(), leaveRequest.getEndDate());
                 }else if(leaveRequest.getStartDate().isBefore(startDate) && leaveRequest.getEndDate().isBefore(endDate)){
                     totalMedicalDays += (int)employeeService.calculateCountedDaysOff(startDate, leaveRequest.getEndDate());
                 }else if(leaveRequest.getEndDate().isAfter(endDate) && leaveRequest.getStartDate().isBefore(endDate)){
                     totalMedicalDays += (int)employeeService.calculateCountedDaysOff(endDate, leaveRequest.getStartDate());
                 }
             }
         }
            return totalMedicalDays;
    }
}
>>>>>>> Stashed changes
