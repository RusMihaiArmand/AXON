package ro.axon.dot.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LeaveRequestEtyTest {

    LeaveRequestEty leaveRequestEty;

    @BeforeEach
    void setUp() {
        leaveRequestEty = new LeaveRequestEty();
    }

    @Test
    void getId() {
        Long idValue = 2L;
        leaveRequestEty.setId(idValue);
        assertEquals(idValue, leaveRequestEty.getId());
    }

    @Test
    void getEmployeeId() {
        String employeeIdValue = "AA4456";
        leaveRequestEty.setEmployeeId(employeeIdValue);
        assertEquals(employeeIdValue, leaveRequestEty.getEmployeeId());
    }

    @Test
    void getCrtUsr() {
        String crtUsrValue = "initial.load";
        leaveRequestEty.setCrtUsr(crtUsrValue);
        assertEquals(crtUsrValue, leaveRequestEty.getCrtUsr());
    }

    @Test
    void getCrtTms() {
        Instant crtTmsValue = Instant.EPOCH;
        leaveRequestEty.setCrtTms(crtTmsValue);
        assertEquals(crtTmsValue, leaveRequestEty.getCrtTms());
    }

    @Test
    void getMdfUsr() {
        String mdfUsrValue = "initial.load";
        leaveRequestEty.setMdfUsr(mdfUsrValue);
        assertEquals(mdfUsrValue, leaveRequestEty.getMdfUsr());
    }

    @Test
    void getMdfTms() {
        Instant mdfTmsValue = Instant.EPOCH;
        leaveRequestEty.setMdfTms(mdfTmsValue);
        assertEquals(mdfTmsValue, leaveRequestEty.getMdfTms());
    }

    @Test
    void getStartDate() {
        LocalDate startDateValue = LocalDate.of(2000, 1, 1);
        leaveRequestEty.setStartDate(startDateValue);
        assertEquals(startDateValue, leaveRequestEty.getStartDate());
    }

    @Test
    void getEndDate() {
        LocalDate endDateValue = LocalDate.of(2000, 1, 2);
        leaveRequestEty.setEndDate(endDateValue);
        assertEquals(endDateValue, leaveRequestEty.getEndDate());
    }

    @Test
    void getNoDays() {
        Integer noDaysValue = 3;
        leaveRequestEty.setNoDays(noDaysValue);
        assertEquals(noDaysValue, leaveRequestEty.getNoDays());
    }

    @Test
    void getType() {
        LeaveRequestEtyTypeEnum typeValue = LeaveRequestEtyTypeEnum.VACATION;
        leaveRequestEty.setType(typeValue);
        assertEquals(typeValue, leaveRequestEty.getType());
    }

    @Test
    void getStatus() {
        LeaveRequestEtyStatusEnum statusValue = LeaveRequestEtyStatusEnum.PENDING;
        leaveRequestEty.setStatus(statusValue);
        assertEquals(statusValue, leaveRequestEty.getStatus());
    }

    @Test
    void getDescription() {
        String descriptionValue = "Lorem ipsum";
        leaveRequestEty.setDescription(descriptionValue);
        assertEquals(descriptionValue, leaveRequestEty.getDescription());
    }

    @Test
    void getRejectReason() {
        String rejectReasonValue = "Not viable";
        leaveRequestEty.setRejectReason(rejectReasonValue);
        assertEquals(rejectReasonValue, leaveRequestEty.getRejectReason());
    }
}