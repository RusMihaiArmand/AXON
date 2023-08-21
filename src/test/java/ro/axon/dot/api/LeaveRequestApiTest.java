package ro.axon.dot.api;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;
import ro.axon.dot.domain.LeaveRequestQuery;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.service.LeaveRequestService;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class LeaveRequestApiTest {

    @Mock
    private LeaveRequestService leaveRequestService;
    @InjectMocks
    private LeaveRequestApi leaveRequestApi;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaveRequestApi)
                        .setControllerAdvice(new ApiExceptionHandler())
                        .build();
    }

    @Test
    void getLeaveRequestDetailsListNone() throws Exception {
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        LeaveRequestDetailsListItem leaveRequest1 = new LeaveRequestDetailsListItem();
        leaveRequest1.setId(1L);
        leaveRequest1.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest1.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest2.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest3.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest4.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsList requests = new LeaveRequestDetailsList();
        requests.setItems(Stream.of(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4).filter(r -> r.getStatus() == LeaveRequestStatus.REJECTED).collect(Collectors.toList()));

        when(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withStatus(
            LeaveRequestStatus.REJECTED).build())).thenReturn(requests);

        mockMvc.perform(get("/api/v1/requests?status=rejected")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void getLeaveRequestDetailsListAll() throws Exception {
        LeaveRequestDetailsListItem leaveRequest1 = new LeaveRequestDetailsListItem();
        leaveRequest1.setId(1L);
        leaveRequest1.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest1.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest2.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest3.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest4.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsList requests = new LeaveRequestDetailsList();
        requests.setItems(Arrays.asList(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4));

        when(leaveRequestService.getLeaveRequestsDetailsSorted(any(BooleanExpression.class))).thenReturn(requests);

        mockMvc.perform(get("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[1].id").value(2L))
                .andExpect(jsonPath("$.items[2].id").value(3L))
                .andExpect(jsonPath("$.items[3].id").value(4L));
    }

    @Test
    void getLeaveRequestDetailsListFiltered() throws Exception {
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        LeaveRequestDetailsListItem leaveRequest1 = new LeaveRequestDetailsListItem();
        leaveRequest1.setId(1L);
        leaveRequest1.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest1.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest2.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest3.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest4.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsList requests = new LeaveRequestDetailsList();
        requests.setItems(Stream.of(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4).filter(r -> r.getType() == LeaveRequestType.MEDICAL).collect(Collectors.toList()));

        when(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withType(
            LeaveRequestType.MEDICAL).build())).thenReturn(requests);

        mockMvc.perform(get("/api/v1/requests?type=medical")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[1].id").value(3L));
    }

    @Test
    void getLeaveRequestDetailsListWrongQuery() throws Exception {
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        LeaveRequestDetailsListItem leaveRequest1 = new LeaveRequestDetailsListItem();
        leaveRequest1.setId(1L);
        leaveRequest1.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest1.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestStatus.PENDING);
        leaveRequest2.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest3.setType(LeaveRequestType.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest4.setType(LeaveRequestType.VACATION);

        LeaveRequestDetailsList requests = new LeaveRequestDetailsList();
        requests.setItems(Arrays.asList(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4));

        when(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withType(null).build())).thenReturn(requests);

        mockMvc.perform(get("/api/v1/requests?type=medica")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[1].id").value(2L))
                .andExpect(jsonPath("$.items[2].id").value(3L))
                .andExpect(jsonPath("$.items[3].id").value(4L));
    }
}