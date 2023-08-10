package ro.axon.dot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.*;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.RemainingDaysOff;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.ID;

@ExtendWith(MockitoExtension.class)
class EmployeeRemainingDaysOffServiceTest {

    EmployeeRemainingDaysOffService employeeRemainingDaysOffService;

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    LeaveRequestRepository leaveRequestRepository;

    @BeforeEach
    void setUp() {
        employeeRemainingDaysOffService = new EmployeeRemainingDaysOffService(employeeRepository, leaveRequestRepository);
    }

    @Test
    void getEmployeeRemainingDaysOffIdNotFound() {
        EmployeeEty employee = new EmployeeEty();
        EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();
        daysOff.setId(1L);
        daysOff.setTotalNoDays(20);
        daysOff.setYear(2023);
        employee.setId(ID);
        employee.setEmpYearlyDaysOff(Collections.singleton(daysOff));

        when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());

        try {
            RemainingDaysOff remainingDaysOff = employeeRemainingDaysOffService.getEmployeeRemainingDaysOff(ID);
        } catch (BusinessException businessException) {
            assertEquals("The employee with the given ID does not exist.", businessException.getError().getErrorDescription().getDevMsg());
            return;
        }
        fail();
    }

    @Test
    void getEmployeeRemainingDaysOffNotSet() {
        EmployeeEty employee = new EmployeeEty();
        employee.setId(ID);

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

        try {
            RemainingDaysOff remainingDaysOff = employeeRemainingDaysOffService.getEmployeeRemainingDaysOff(ID);
        } catch (BusinessException businessException) {
            assertEquals("The vacation days for this employee have not been set for this year.", businessException.getError().getErrorDescription().getDevMsg());
            return;
        }
        fail();
    }

    @Test
    void getEmployeeRemainingDaysOff() {
        EmployeeEty employee = new EmployeeEty();
        EmpYearlyDaysOffEty daysOff = new EmpYearlyDaysOffEty();
        daysOff.setId(1L);
        daysOff.setTotalNoDays(20);
        daysOff.setYear(2023);
        employee.setId(ID);
        employee.setEmpYearlyDaysOff(Collections.singleton(daysOff));

        when(employeeRepository.findById(anyString())).thenReturn(Optional.of(employee));

        QLeaveRequestEty root = QLeaveRequestEty.leaveRequestEty;
        LeaveRequestQuery query = new LeaveRequestQuery();
        when(leaveRequestRepository.findAll(root.employee.id.eq(employee.getId())
                .and(query.withStatus(LeaveRequestEtyStatusEnum.APPROVED).withType(LeaveRequestEtyTypeEnum.VACATION).build())
                .or(query.withStatus(LeaveRequestEtyStatusEnum.PENDING).withType(LeaveRequestEtyTypeEnum.VACATION).build()))).thenReturn(Collections.emptyList());

        RemainingDaysOff remainingDaysOff = employeeRemainingDaysOffService.getEmployeeRemainingDaysOff(ID);

        assertEquals(20, remainingDaysOff.getRemainingDays());
    }

}