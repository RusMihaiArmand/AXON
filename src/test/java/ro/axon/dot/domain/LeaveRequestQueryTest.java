package ro.axon.dot.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import ro.axon.dot.domain.entity.QLeaveRequestEty;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;

import static org.junit.jupiter.api.Assertions.*;

class LeaveRequestQueryTest {
    private LeaveRequestQuery leaveRequestQuery;
    private QLeaveRequestEty root;
    static final BooleanExpression TRUE_EXP = Expressions.TRUE.isTrue();

    @BeforeEach
    void setUp() {
        leaveRequestQuery = new LeaveRequestQuery();
        root = QLeaveRequestEty.leaveRequestEty;
    }

    @Test
    void withStatusNull() {
        LeaveRequestStatus status = null;
        leaveRequestQuery.withStatus(status);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withStatus() {
        LeaveRequestStatus status = LeaveRequestStatus.PENDING;
        leaveRequestQuery.withStatus(status);
        BooleanExpression expectedExpression = root.status.eq(status).and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withEmployeeNameNull() {
        String employeeName = null;
        leaveRequestQuery.withEmployeeName(employeeName);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withEmployeeId() {
        String employeeName = "John";
        leaveRequestQuery.withEmployeeName(employeeName);
        BooleanExpression expectedExpression = TRUE_EXP.and(root.employee.firstName.containsIgnoreCase(employeeName).or(root.employee.lastName.containsIgnoreCase(employeeName)).and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withTypeNull() {
        LeaveRequestType type = null;
        leaveRequestQuery.withType(type);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withType() {
        LeaveRequestType type = LeaveRequestType.VACATION;
        leaveRequestQuery.withType(type);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(root.type.eq(type).and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withStartDateNull() {
        LocalDate startDate = null;
        leaveRequestQuery.withStartDate(startDate);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withStartDate() {
        LocalDate startDate = LocalDate.of(2000, 1, 1);
        leaveRequestQuery.withStartDate(startDate);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(root.startDate.after(startDate).and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withEndDateNull() {
        LocalDate endDate = null;
        leaveRequestQuery.withEndDate(endDate);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }

    @Test
    void withEndDate() {
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        leaveRequestQuery.withEndDate(endDate);
        BooleanExpression expectedExpression = TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(TRUE_EXP.and(root.endDate.before(endDate)))));
        assertEquals(expectedExpression, leaveRequestQuery.build());
    }
}