package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;
import ro.axon.dot.domain.LeaveRequestQuery;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.model.LeaveRequestReport;
import ro.axon.dot.service.LeaveRequestService;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/requests")
public class LeaveRequestApi {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
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
        LeaveRequestStatus status;
        try {
            status = LeaveRequestStatus.valueOf(statusParamOpt.orElse("n/a").toUpperCase());
        }
        catch (Exception e) {
            status = null;
        }
        String search = searchParamOpt.orElse(null);
        LeaveRequestType type;
        try {
            type = LeaveRequestType.valueOf(typeParamOpt.orElse("n/a").toUpperCase());
        }
        catch (Exception e) {
            type = null;
        }
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateParamOpt.orElse("n/a"));
        }
        catch (Exception e) {
            startDate = null;
        }
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(endDateParamOpt.orElse("n/a"));
        }
        catch (Exception e) {
            endDate = null;
        }
        //Any invalid/inexistent parameter takes on a null value
        LeaveRequestQuery leaveRequestQuery = new LeaveRequestQuery();
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsDetailsSorted(leaveRequestQuery.withStatus(status)
                .withEmployeeName(search).withType(type).withStartDate(startDate).withEndDate(endDate).build()));
    }


    @GetMapping("/by-period")
    public ResponseEntity<LeaveRequestReport> getLeaveRequestsByPeriod(
            @RequestParam(required = false) String team,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {
        LeaveRequestReport report = leaveRequestService.generateLeaveRequestReport(team, startDate, endDate);
        return ResponseEntity.ok(report);
    }

}
