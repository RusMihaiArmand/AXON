package ro.axon.dot.service;

import com.querydsl.core.types.dsl.Expressions;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.domain.LeaveRequestEty;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.*;

class LeaveRequestServiceTest {

    private LeaveRequestService leaveRequestService;
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        leaveRequestService = new LeaveRequestService(leaveRequestRepository, employeeRepository);
    }

    @Test
    void getLeaveRequestsDetailsEmptyList() {
        when(leaveRequestRepository.findAll(Expressions.TRUE.isTrue())).thenReturn(List.of());

        LeaveRequestDetailsList leaveRequestDetailsList = leaveRequestService.getLeaveRequestsDetailsSorted(Expressions.TRUE.isTrue());
        List<LeaveRequestDetailsListItem> leaveRequests = leaveRequestDetailsList.getItems();
        assertEquals(0, leaveRequests.size());
    }

    @Test
    void getLeaveRequestsDetailsSorted() {
        LeaveRequestEty leaveRequest1 = new LeaveRequestEty();
        Long leaveRequest1IdValue = 1L;
        leaveRequest1.setId(leaveRequest1IdValue);
        leaveRequest1.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest1.setCrtTms(Instant.ofEpochSecond(2));

        LeaveRequestEty leaveRequest2 = new LeaveRequestEty();
        Long leaveRequest2IdValue = 2L;
        leaveRequest2.setId(leaveRequest2IdValue);
        leaveRequest2.setStatus(LeaveRequestEtyStatusEnum.REJECTED);
        leaveRequest2.setCrtTms(Instant.ofEpochSecond(1));

        LeaveRequestEty leaveRequest3 = new LeaveRequestEty();
        Long leaveRequest3IdValue = 3L;
        leaveRequest3.setId(leaveRequest3IdValue);
        leaveRequest3.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest3.setCrtTms(Instant.ofEpochSecond(4));

        LeaveRequestEty leaveRequest4 = new LeaveRequestEty();
        Long leaveRequest4IdValue = 4L;
        leaveRequest4.setId(leaveRequest4IdValue);
        leaveRequest4.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest4.setCrtTms(Instant.ofEpochSecond(3));

        when(leaveRequestRepository.findAll(Expressions.TRUE.isTrue())).thenReturn(Arrays.asList(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4));

        LeaveRequestDetailsList leaveRequestDetailsList = leaveRequestService.getLeaveRequestsDetailsSorted(Expressions.TRUE.isTrue());
        List<LeaveRequestDetailsListItem> leaveRequests = leaveRequestDetailsList.getItems();
        // We want to confirm that the sorting order will be: oldest to newest pending request, oldest to newest
        // approved request, and oldest to newest rejected request.
        assertEquals(4, leaveRequests.size());
        assertEquals(leaveRequest4IdValue, leaveRequests.get(0).getId());
        assertEquals(leaveRequest3IdValue, leaveRequests.get(1).getId());
        assertEquals(leaveRequest1IdValue, leaveRequests.get(2).getId());
        assertEquals(leaveRequest2IdValue, leaveRequests.get(3).getId());
    }

    @Test
    void getLeaveRequestsDetailsSortedNullElems() {
        LeaveRequestEty leaveRequest1 = null;

        LeaveRequestEty leaveRequest2 = new LeaveRequestEty();
        Long leaveRequest2IdValue = 2L;
        leaveRequest2.setId(leaveRequest2IdValue);
        leaveRequest2.setStatus(LeaveRequestEtyStatusEnum.REJECTED);
        leaveRequest2.setCrtTms(Instant.ofEpochSecond(1));

        LeaveRequestEty leaveRequest3 = new LeaveRequestEty();
        Long leaveRequest3IdValue = 3L;
        leaveRequest3.setId(leaveRequest3IdValue);
        leaveRequest3.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest3.setCrtTms(Instant.ofEpochSecond(4));

        LeaveRequestEty leaveRequest4 = new LeaveRequestEty();
        Long leaveRequest4IdValue = 4L;
        leaveRequest4.setId(leaveRequest4IdValue);
        leaveRequest4.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest4.setCrtTms(Instant.ofEpochSecond(3));

        when(leaveRequestRepository.findAll(Expressions.TRUE.isTrue())).thenReturn(Arrays.asList(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4));

        LeaveRequestDetailsList leaveRequestDetailsList = leaveRequestService.getLeaveRequestsDetailsSorted(Expressions.TRUE.isTrue());
        List<LeaveRequestDetailsListItem> leaveRequests = leaveRequestDetailsList.getItems();
        // We want to confirm that the sorting order will be: oldest to newest pending request, oldest to newest
        // approved request, and oldest to newest rejected request, with possible nulls filtered out.
        assertEquals(3, leaveRequests.size());
        assertEquals(leaveRequest4IdValue, leaveRequests.get(0).getId());
        assertEquals(leaveRequest3IdValue, leaveRequests.get(1).getId());
        assertEquals(leaveRequest2IdValue, leaveRequests.get(2).getId());
    }

    @Test
    void editLeaveRequestNotFound(){

        EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
        leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
        leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
        leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
        leaveRequestEdit.setDescription("Vacation leave request");

        EmployeeEty employee = new EmployeeEty();
        employee.setId(ID);

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> leaveRequestService.editLeaveRequest(ID, 1L, leaveRequestEdit));

        assertEquals(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND, exception.getError().getErrorDescription());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void editLeaveRequestRejected(){

        LeaveRequestEty leaveRequest = new LeaveRequestEty();
        Long leaveRequestIdValue = 1L;
        leaveRequest.setId(leaveRequestIdValue);
        leaveRequest.setStatus(LeaveRequestEtyStatusEnum.REJECTED);

        EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
        leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
        leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
        leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
        leaveRequestEdit.setDescription("Vacation leave request");

        EmployeeEty employee = new EmployeeEty();
        employee.setId(ID);
        employee.getLeaveRequests().add(leaveRequest);

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> leaveRequestService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

        assertEquals(BusinessErrorCode.LEAVE_REQUEST_REJECTED, exception.getError().getErrorDescription());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void editLeaveRequestPastDate(){

        LeaveRequestEty leaveRequest = new LeaveRequestEty();
        Long leaveRequestIdValue = 1L;
        leaveRequest.setId(leaveRequestIdValue);
        leaveRequest.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest.setStartDate(LocalDate.parse("2023-07-23"));
        leaveRequest.setEndDate(LocalDate.parse("2023-07-27"));

        EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
        leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
        leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
        leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
        leaveRequestEdit.setDescription("Vacation leave request");

        EmployeeEty employee = new EmployeeEty();
        employee.setId(ID);
        employee.getLeaveRequests().add(leaveRequest);

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> leaveRequestService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

        assertEquals(BusinessErrorCode.LEAVE_REQUEST_PAST_DATE, exception.getError().getErrorDescription());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void editLeaveRequestPrecedingVersion(){

        LeaveRequestEty leaveRequest = new LeaveRequestEty();
        Long leaveRequestIdValue = 1L;
        leaveRequest.setId(leaveRequestIdValue);
        leaveRequest.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
        leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
        leaveRequest.setV(2L);

        EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
        leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
        leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
        leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
        leaveRequestEdit.setDescription("Vacation leave request");
        leaveRequestEdit.setV(1L);

        EmployeeEty employee = new EmployeeEty();
        employee.setId(ID);
        employee.getLeaveRequests().add(leaveRequest);

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> leaveRequestService.editLeaveRequest(ID, leaveRequestIdValue, leaveRequestEdit));

        assertEquals(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION, exception.getError().getErrorDescription());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void editLeaveRequestSuccess(){

        LeaveRequestEty leaveRequest = new LeaveRequestEty();
        Long leaveRequestIdValue = 1L;
        leaveRequest.setId(leaveRequestIdValue);
        leaveRequest.setType(LeaveRequestEtyTypeEnum.MEDICAL);
        leaveRequest.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest.setStartDate(LocalDate.parse("2023-08-23"));
        leaveRequest.setEndDate(LocalDate.parse("2023-08-27"));
        leaveRequest.setDescription("Medical leave request");
        leaveRequest.setV(1L);

        EditLeaveRequestDetails leaveRequestEdit = new EditLeaveRequestDetails();
        leaveRequestEdit.setStartDate(LocalDate.parse("2023-08-25"));
        leaveRequestEdit.setEndDate(LocalDate.parse("2023-08-28"));
        leaveRequestEdit.setType(LeaveRequestEtyTypeEnum.VACATION);
        leaveRequestEdit.setDescription("Vacation leave request");
        leaveRequestEdit.setV(1L);

        EmployeeEty employee = new EmployeeEty();
        employee.setId(ID);
        employee.getLeaveRequests().add(leaveRequest);

        LeaveRequestEty savedLeaveRequest = new LeaveRequestEty();
        savedLeaveRequest.setId(leaveRequestIdValue);
        savedLeaveRequest.setType(LeaveRequestEtyTypeEnum.VACATION);
        savedLeaveRequest.setStartDate(LocalDate.parse("2023-08-25"));
        savedLeaveRequest.setEndDate(LocalDate.parse("2023-08-28"));
        savedLeaveRequest.setDescription("Vacation leave request");
        savedLeaveRequest.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        savedLeaveRequest.setV(1L);

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any())).thenReturn(savedLeaveRequest);

        LeaveRequestDetailsListItem leaveRequestItem = leaveRequestService.editLeaveRequest(ID,
            leaveRequestIdValue,
            leaveRequestEdit);

        assertEquals(leaveRequestEdit.getStartDate(), leaveRequestItem.getStartDate());
        assertEquals(leaveRequestEdit.getEndDate(), leaveRequestItem.getEndDate());
        assertEquals(leaveRequestEdit.getType(), leaveRequestItem.getType());
        assertEquals(leaveRequestEdit.getDescription(), leaveRequestItem.getDescription());
        assertEquals(LeaveRequestEtyStatusEnum.PENDING, leaveRequestItem.getStatus());
    }



}