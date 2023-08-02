package ro.axon.dot.api;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;
import ro.axon.dot.domain.QLeaveRequestEty;
import ro.axon.dot.model.LeaveRequestDetailsList;
import ro.axon.dot.service.LeaveRequestService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LeaveRequestApi {

    private final LeaveRequestService leaveRequestService;

    @GetMapping("requests")
    public ResponseEntity<LeaveRequestDetailsList> getLeaveRequestDetailsList(@RequestParam(required=false) Map<String,String> queryParameters) throws Exception {
        QLeaveRequestEty request = QLeaveRequestEty.leaveRequestEty;
        LeaveRequestEtyStatusEnum status = LeaveRequestEtyStatusEnum.findEnumValue(queryParameters.get("status"));
        String search = queryParameters.get("search");
        LeaveRequestEtyTypeEnum type = LeaveRequestEtyTypeEnum.findEnumValue(queryParameters.get("type"));
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(queryParameters.get("startDate"));
        }
        catch (Exception e) {
            startDate = null;
        }
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(queryParameters.get("endDate"));
        }
        catch (Exception e) {
            endDate = null;
        }
        //Any invalid/inexistent parameter takes on a null value
        BooleanExpression requestHasStatus = Expressions.TRUE.isTrue();
        BooleanExpression requestHasEmployeeId = Expressions.TRUE.isTrue();
        BooleanExpression requestHasType = Expressions.TRUE.isTrue();
        BooleanExpression requestOldestDate = Expressions.TRUE.isTrue();
        BooleanExpression requestNewestDate = Expressions.TRUE.isTrue();
        if (status != null) requestHasStatus = request.status.eq(status);
        if (search != null) requestHasEmployeeId = request.employeeId.like(search);
        if (type != null) requestHasType = request.type.eq(type);
        if (startDate != null) requestOldestDate = request.startDate.after(startDate);
        if (endDate != null) requestNewestDate = request.endDate.before(endDate);
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsDetailsSorted(requestHasStatus.and(requestHasEmployeeId.and(requestHasType.and(requestOldestDate.and(requestNewestDate))))));
    }
}
