package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.LeaveRequestQuery;
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
    public ResponseEntity<LeaveRequestDetailsList> getLeaveRequestDetailsList(@RequestParam(name="status", required = false) String statusParam,
                                                                              @RequestParam(name="search", required = false) String searchParam,
                                                                              @RequestParam(name="type", required = false) String typeParam,
                                                                              @RequestParam(name="startDate", required = false) String startDateParam,
                                                                              @RequestParam(name="endDate", required = false) String endDateParam) throws Exception {
        Optional<String> statusParamOpt = Optional.ofNullable(statusParam);
        Optional<String> searchParamOpt = Optional.ofNullable(searchParam);
        Optional<String> typeParamOpt = Optional.ofNullable(typeParam);
        Optional<String> startDateParamOpt = Optional.ofNullable(startDateParam);
        Optional<String> endDateParamOpt = Optional.ofNullable(endDateParam);
        LeaveRequestEtyStatusEnum status;
        try {
            status = LeaveRequestEtyStatusEnum.valueOf(statusParamOpt.orElseGet(() -> "n/a").toUpperCase());
        }
        catch (Exception e) {
            status = null;
        }
        String search = searchParamOpt.orElseGet(() -> null);
        LeaveRequestEtyTypeEnum type;
        try {
            type = LeaveRequestEtyTypeEnum.valueOf(typeParamOpt.orElseGet(() -> "n/a").toUpperCase());
        }
        catch (Exception e) {
            type = null;
        }
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateParamOpt.orElseGet(() -> "n/a"));
        }
        catch (Exception e) {
            startDate = null;
        }
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(endDateParamOpt.orElseGet(() -> "n/a"));
        }
        catch (Exception e) {
            endDate = null;
        }
        //Any invalid/inexistent parameter takes on a null value
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withStatus(status)
                .withEmployeeName(search).withType(type).withStartDate(startDate).withEndDate(endDate).build()));
    }
}
