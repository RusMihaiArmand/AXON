package ro.axon.dot.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDate;

public class LeaveRequestQuery {

    private static final QLeaveRequestEty root = QLeaveRequestEty.leaveRequestEty;
    private BooleanExpression statusExp = Expressions.TRUE.isTrue();
    private BooleanExpression employeeIdExp = Expressions.TRUE.isTrue();
    private BooleanExpression typeExp = Expressions.TRUE.isTrue();
    private BooleanExpression startDateExp = Expressions.TRUE.isTrue();
    private BooleanExpression endDateExp = Expressions.TRUE.isTrue();

    public LeaveRequestQuery withStatus(LeaveRequestEtyStatusEnum status) {
        if (status != null) statusExp = root.status.eq(status);
        return this;
    }

    public LeaveRequestQuery withEmployeeId(String employeeId) {
        if (employeeId != null) employeeIdExp = root.employee.id.like(employeeId);
        return this;
    }

    public LeaveRequestQuery withType(LeaveRequestEtyTypeEnum type) {
        if (type != null) typeExp = root.type.eq(type);
        return this;
    }

    public LeaveRequestQuery withStartDate(LocalDate startDate) {
        if (startDate != null) startDateExp = root.startDate.after(startDate);
        return this;
    }

    public LeaveRequestQuery withEndDate(LocalDate endDate) {
        if (endDate != null) endDateExp = root.endDate.before(endDate);
        return this;
    }

    public BooleanExpression build() {
        return statusExp.and(employeeIdExp.and(typeExp.and(startDateExp.and(endDateExp))));
    }
}
