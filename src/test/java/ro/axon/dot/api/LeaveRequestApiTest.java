package ro.axon.dot.api;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.EmployeeTestAttributes;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestQuery;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestDetailsListItem;
import ro.axon.dot.service.LeaveRequestService;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LeaveRequestApiTest {

    private final String employeeId = EmployeeTestAttributes.ID;
    private final Long requestId = 1L;
    private final String editLeaveRequestContent = "{ \"startDate\": \"2023-08-25\", \"endDate\": \"2023-08-28\", \"type\": \"VACATION\", \"description\": \"Vacation leave request\", \"v\": 1 }";

    @Mock
    private LeaveRequestService leaveRequestService;
    @InjectMocks
    private LeaveRequestApi leaveRequestApi;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(leaveRequestApi)
            .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void getLeaveRequestDetailsListNone() throws Exception {
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        LeaveRequestDetailsListItem leaveRequest1 = new LeaveRequestDetailsListItem();
        leaveRequest1.setId(1L);
        leaveRequest1.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest1.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest2.setType(LeaveRequestEtyTypeEnum.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest3.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest4.setType(LeaveRequestEtyTypeEnum.VACATION);

        LeaveRequestDetailsList requests = new LeaveRequestDetailsList();
        requests.setItems(Stream.of(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4).filter(r -> r.getStatus() == LeaveRequestEtyStatusEnum.REJECTED).collect(Collectors.toList()));

        when(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withStatus(LeaveRequestEtyStatusEnum.REJECTED).build())).thenReturn(requests);

        mockMvc.perform(get("/api/v1/requests?status=rejected")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void getLeaveRequestDetailsListAll() throws Exception {
        LeaveRequestDetailsListItem leaveRequest1 = new LeaveRequestDetailsListItem();
        leaveRequest1.setId(1L);
        leaveRequest1.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest1.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest2.setType(LeaveRequestEtyTypeEnum.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest3.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest4.setType(LeaveRequestEtyTypeEnum.VACATION);

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
        leaveRequest1.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest1.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest2.setType(LeaveRequestEtyTypeEnum.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest3.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest4.setType(LeaveRequestEtyTypeEnum.VACATION);

        LeaveRequestDetailsList requests = new LeaveRequestDetailsList();
        requests.setItems(Stream.of(leaveRequest1, leaveRequest2, leaveRequest3, leaveRequest4).filter(r -> r.getType() == LeaveRequestEtyTypeEnum.MEDICAL).collect(Collectors.toList()));

        when(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withType(LeaveRequestEtyTypeEnum.MEDICAL).build())).thenReturn(requests);

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
        leaveRequest1.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest1.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest2 = new LeaveRequestDetailsListItem();
        leaveRequest2.setId(2L);
        leaveRequest2.setStatus(LeaveRequestEtyStatusEnum.PENDING);
        leaveRequest2.setType(LeaveRequestEtyTypeEnum.VACATION);

        LeaveRequestDetailsListItem leaveRequest3 = new LeaveRequestDetailsListItem();
        leaveRequest3.setId(3L);
        leaveRequest3.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest3.setType(LeaveRequestEtyTypeEnum.MEDICAL);

        LeaveRequestDetailsListItem leaveRequest4 = new LeaveRequestDetailsListItem();
        leaveRequest4.setId(4L);
        leaveRequest4.setStatus(LeaveRequestEtyStatusEnum.APPROVED);
        leaveRequest4.setType(LeaveRequestEtyTypeEnum.VACATION);

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

    @Test
    void editLeaveRequestSucces() throws Exception {

        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId, employeeId, requestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(editLeaveRequestContent))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testEditLeaveRequestEmployeeNotFound() throws Exception {

        when(leaveRequestService.editLeaveRequest(anyString(), anyLong(), any()))
            .thenThrow( new BusinessException(BusinessExceptionElement
                .builder().errorDescription(BusinessErrorCode.EMPLOYEE_NOT_FOUND).build()));

        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(editLeaveRequestContent))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testEditLeaveRequestNotFound() throws Exception {

        when(leaveRequestService.editLeaveRequest(anyString(), anyLong(), any()))
            .thenThrow( new BusinessException(BusinessExceptionElement
                .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_NOT_FOUND).build()));

        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(editLeaveRequestContent))
            .andExpect(status().isBadRequest());
    }
    @Test
    public void testEditLeaveRequestPastDate() throws Exception {

        when(leaveRequestService.editLeaveRequest(anyString(), anyLong(), any()))
            .thenThrow( new BusinessException(BusinessExceptionElement
                .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_PAST_DATE).build()));

        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(editLeaveRequestContent))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testEditLeaveRequestRejected() throws Exception {

        when(leaveRequestService.editLeaveRequest(anyString(), anyLong(), any()))
            .thenThrow( new BusinessException(BusinessExceptionElement
                .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_REJECTED).build()));

        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(editLeaveRequestContent))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testEditLeaveRequestPrecedingVersion() throws Exception {

        when(leaveRequestService.editLeaveRequest(anyString(), anyLong(), any()))
            .thenThrow( new BusinessException(BusinessExceptionElement
                .builder().errorDescription(BusinessErrorCode.LEAVE_REQUEST_PRECEDING_VERSION).build()));

        mockMvc.perform(put("/api/v1/employees/" + employeeId + "/requests/" + requestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(editLeaveRequestContent))
            .andExpect(status().isConflict());
    }


}