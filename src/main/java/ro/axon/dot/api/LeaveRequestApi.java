package ro.axon.dot.api;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestQuery;
import ro.axon.dot.model.EditLeaveRequestDetails;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.service.LeaveRequestService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LeaveRequestApi {

    private final LeaveRequestService leaveRequestService;


    @GetMapping("requests")
    public ResponseEntity<LeaveRequestDetailsList> getLeaveRequestDetailsList(@RequestParam(name="status") Optional<String> statusParam,
                                                                              @RequestParam(name="search") Optional<String> searchParam,
                                                                              @RequestParam(name="type") Optional<String> typeParam,
                                                                              @RequestParam(name="startDate") Optional<String> startDateParam,
                                                                              @RequestParam(name="endDate") Optional<String> endDateParam) throws Exception {
        LeaveRequestEtyStatusEnum status;
        try {
            status = LeaveRequestEtyStatusEnum.valueOf(statusParam.orElseGet(() -> "n/a").toUpperCase());
        }
        catch (Exception e) {
            status = null;
        }
        String search = searchParam.orElseGet(() -> null);
        LeaveRequestEtyTypeEnum type;
        try {
            type = LeaveRequestEtyTypeEnum.valueOf(typeParam.orElseGet(() -> "n/a").toUpperCase());
        }
        catch (Exception e) {
            type = null;
        }
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateParam.orElseGet(() -> "n/a"));
        }
        catch (Exception e) {
            startDate = null;
        }
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(endDateParam.orElseGet(() -> "n/a"));
        }
        catch (Exception e) {
            endDate = null;
        }
        //Any invalid/inexistent parameter takes on a null value
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withStatus(status)
                .withEmployeeName(search).withType(type).withStartDate(startDate).withEndDate(endDate).build()));
    }

    @PutMapping("employees/{employeeId}/requests/{requestId}")
    public ResponseEntity<Void> editLeaveRequest(@PathVariable String employeeId,
                                                                    @PathVariable Long requestId,
                                                                    @Valid @RequestBody EditLeaveRequestDetails leaveRequestDetails){

        leaveRequestService.editLeaveRequest(employeeId, requestId, leaveRequestDetails);

        return ResponseEntity.noContent().build();
    }
}
