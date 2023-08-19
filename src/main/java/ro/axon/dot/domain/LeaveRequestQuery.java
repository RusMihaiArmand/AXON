package ro.axon.dot.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDate;
import ro.axon.dot.domain.entity.QLeaveRequestEty;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;

public class LeaveRequestQuery {

    private static final QLeaveRequestEty root = QLeaveRequestEty.leaveRequestEty;
    private BooleanExpression statusExp = Expressions.TRUE.isTrue();
    private BooleanExpression employeeNameExp = Expressions.TRUE.isTrue();
    private BooleanExpression typeExp = Expressions.TRUE.isTrue();
    private BooleanExpression startDateExp = Expressions.TRUE.isTrue();
    private BooleanExpression endDateExp = Expressions.TRUE.isTrue();

    public LeaveRequestQuery withStatus(LeaveRequestStatus status) {
        if (status != null) statusExp = root.status.eq(status);
        return this;
    }

    public LeaveRequestQuery withEmployeeName(String nameSearch) {
        if (nameSearch != null) employeeNameExp = root.employee.firstName.containsIgnoreCase(nameSearch).or(root.employee.lastName.containsIgnoreCase(nameSearch));
        return this;
    }

    public LeaveRequestQuery withType(LeaveRequestType type) {
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
        return statusExp.and(employeeNameExp.and(typeExp.and(startDateExp.and(endDateExp))));
    }
}
