package ro.axon.dot.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.LocalDate;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.mapper.LeaveRequestMapper;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.LeaveRequestDetailsList;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import ro.axon.dot.model.LeaveRequestDetailsListItem;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestDetailsList getLeaveRequestsDetailsSorted(BooleanExpression query) {
        var leaveRequestDetailsList = new LeaveRequestDetailsList();
        Comparator<LeaveRequestEty> leaveRequestStatusComparator = Comparator.comparing(LeaveRequestEty::getStatus);
        Comparator<LeaveRequestEty> leaveRequestCrtTmsComparator = Comparator.comparing(LeaveRequestEty::getCrtTms);
        Iterable<LeaveRequestEty> filteredRepo = leaveRequestRepository.findAll(query);
        leaveRequestDetailsList.setItems(StreamSupport.stream(filteredRepo.spliterator(), false).filter(Objects::nonNull).sorted(leaveRequestStatusComparator.thenComparing(leaveRequestCrtTmsComparator))
                .map(LeaveRequestMapper.INSTANCE::mapLeaveRequestEtyToLeaveRequestDto).collect(Collectors.toList()));
        return leaveRequestDetailsList;
    }

    @Transactional
    public LeaveRequestDetailsListItem editLeaveRequest(String employeeId,
                                                        Long requestId,
                                                        EditLeaveRequestDetails editLeaveRequestDetails){

        EmployeeEty employee = employeeRepository.findById(employeeId).orElseThrow(
            () -> new BusinessException(BusinessExceptionElement.builder().errorDescription(
                BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()
            ));

        LeaveRequestEty leaveRequest = checkLeaveRequestExists(employee, requestId);

        validateLeaveRequest(leaveRequest, editLeaveRequestDetails);

        if(leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.APPROVED) ||
            leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.PENDING)){

            leaveRequest.setStartDate(editLeaveRequestDetails.getStartDate());
            leaveRequest.setEndDate(editLeaveRequestDetails.getEndDate());
            leaveRequest.setType(editLeaveRequestDetails.getType());
            leaveRequest.setDescription(editLeaveRequestDetails.getDescription());
            leaveRequest.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        }

        LeaveRequestDetailsListItem leaveRequestDetailsListItem = LeaveRequestMapper.INSTANCE
            .mapLeaveRequestEtyToLeaveRequestDto(leaveRequestRepository.save(leaveRequest));

        return leaveRequestDetailsListItem;
    }

    private LeaveRequestEty checkLeaveRequestExists(EmployeeEty employee, Long requestId){

        return employee.getLeaveRequests().stream()
            .filter(leaveRequestEty -> leaveRequestEty.getId().equals(requestId)).findFirst()
            .orElseThrow(() -> new BusinessException(BusinessExceptionElement.builder()
                .errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND).build()));

    }
    private void validateLeaveRequest(LeaveRequestEty leaveRequest, EditLeaveRequestDetails editLeaveRequestDetails){

        if(editLeaveRequestDetails.getV() < leaveRequest.getV()){
            throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
                BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION).build());
        }
        if(leaveRequest.getStatus().equals(LeaveRequestEtyStatusEnum.REJECTED)){
            throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
                BusinessErrorCode.LEAVE_REQUEST_REJECTED).build());
        }
        if(editLeaveRequestDetails.getStartDate().isBefore(LocalDate.now().withDayOfMonth(1))){
            throw new BusinessException(BusinessExceptionElement.builder().errorDescription(
                BusinessErrorCode.LEAVE_REQUEST_PAST_DATE).build());
        }
    }

}