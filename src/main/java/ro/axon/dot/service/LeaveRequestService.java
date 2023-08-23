package ro.axon.dot.service;

import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.ArrayList;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import ro.axon.dot.domain.repositories.LeaveRequestRepository;

import ro.axon.dot.model.LeaveRequestReport;
import ro.axon.dot.model.LeaveRequestReportItem;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;


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

    public LeaveRequestReport generateLeaveRequestReport(String teamName, LocalDate startDate,
                                                         LocalDate endDate) {

        List<LeaveRequestReportItem> reportItems = new ArrayList<>();
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
            reportItem.setLeaveRequests(new LeaveRequestDetailsList());

            reportItem.setFirstName(employee.getFirstName());
            reportItem.setLastName(employee.getLastName());

            List<LeaveRequestEty> employeeLeaveByPeriod = employeeService.getEmployeeLeaveRequestByPeriod(
                startDate, endDate, employee);

            int noOfVacationDays = calculateDaysForEmployee(
                employeeLeaveByPeriod
                    .stream()
                    .filter(leaveRequestEty -> leaveRequestEty.getType().equals(LeaveRequestType.VACATION))
                    .collect(Collectors.toList()), startDate, endDate);

            int noOfMedicalDays = calculateDaysForEmployee(employeeLeaveByPeriod
                .stream()
                .filter(leaveRequestEty -> leaveRequestEty.getType().equals(LeaveRequestType.MEDICAL))
                .collect(Collectors.toList()), startDate, endDate);

            reportItem.setNoOfMedicalDays(noOfMedicalDays);
            reportItem.setNoOfVacationDays(noOfVacationDays);
            reportItem.getLeaveRequests().setItems(employeeLeaveByPeriod.stream()
                .map(LeaveRequestMapper.INSTANCE::mapLeaveRequestEtyToLeaveRequestDto)
                .collect(Collectors.toList()));

            reportItems.add(reportItem);
        }

        LeaveRequestReport leaveRequestReport = new LeaveRequestReport();
        leaveRequestReport.setItems(reportItems);

        return leaveRequestReport;
    }

    private int calculateDaysForEmployee(List<LeaveRequestEty> leaveRequests, LocalDate startDate,
                                      LocalDate endDate) {
        int totalVacationDays = 0;
        for (LeaveRequestEty leaveRequest : leaveRequests) {
            if (leaveRequest.getStatus().equals(LeaveRequestStatus.APPROVED)) {
                if (leaveRequest.getStartDate().isAfter(startDate) && leaveRequest.getEndDate().isBefore(endDate)) {
                    totalVacationDays += leaveRequest.getNoDays();
                } else if (leaveRequest.getStartDate().isBefore(startDate)) {
                    totalVacationDays += (int) employeeService.calculateCountedDaysOff(startDate, leaveRequest.getEndDate());
                } else if (leaveRequest.getEndDate().isAfter(endDate) ) {
                    totalVacationDays += (int) employeeService.calculateCountedDaysOff(leaveRequest.getStartDate(), endDate);
                }
            }
        }
        return totalVacationDays;
    }

}

