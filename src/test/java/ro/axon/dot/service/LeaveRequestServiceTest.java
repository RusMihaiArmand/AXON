package ro.axon.dot.service;

import com.querydsl.core.types.dsl.Expressions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.entity.LeaveRequestEty;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.repositories.LeaveRequestRepository;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import ro.axon.dot.domain.repositories.EmployeeRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @InjectMocks
    private LeaveRequestService leaveRequestService;
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;

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
        leaveRequest1.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest1.setCrtTms(Instant.ofEpochSecond(2));

        LeaveRequestEty leaveRequest2 = new LeaveRequestEty();
        Long leaveRequest2IdValue = 2L;
        leaveRequest2.setId(leaveRequest2IdValue);
        leaveRequest2.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest2.setCrtTms(Instant.ofEpochSecond(1));

        LeaveRequestEty leaveRequest3 = new LeaveRequestEty();
        Long leaveRequest3IdValue = 3L;
        leaveRequest3.setId(leaveRequest3IdValue);
        leaveRequest3.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest3.setCrtTms(Instant.ofEpochSecond(4));

        LeaveRequestEty leaveRequest4 = new LeaveRequestEty();
        Long leaveRequest4IdValue = 4L;
        leaveRequest4.setId(leaveRequest4IdValue);
        leaveRequest4.setStatus(LeaveRequestStatus.PENDING);
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
        leaveRequest2.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest2.setCrtTms(Instant.ofEpochSecond(1));

        LeaveRequestEty leaveRequest3 = new LeaveRequestEty();
        Long leaveRequest3IdValue = 3L;
        leaveRequest3.setId(leaveRequest3IdValue);
        leaveRequest3.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest3.setCrtTms(Instant.ofEpochSecond(4));

        LeaveRequestEty leaveRequest4 = new LeaveRequestEty();
        Long leaveRequest4IdValue = 4L;
        leaveRequest4.setId(leaveRequest4IdValue);
        leaveRequest4.setStatus(LeaveRequestStatus.PENDING);
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
}