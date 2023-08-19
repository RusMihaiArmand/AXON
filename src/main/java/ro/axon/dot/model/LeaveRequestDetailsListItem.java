package ro.axon.dot.model;

import lombok.Data;
import ro.axon.dot.domain.enums.LeaveRequestStatus;
import ro.axon.dot.domain.enums.LeaveRequestType;

import java.time.LocalDate;
import java.time.Instant;

@Data
public class LeaveRequestDetailsListItem {

    private Long id;
    private EmployeeIdentificationDetailsListItem employeeDetails;
    private String crtUsr;
    private Instant crtTms;
    private String mdfUsr;
    private Instant mdfTms;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer noDays;
    private LeaveRequestType type;
    private LeaveRequestStatus status;
    private String description;
    private String rejectReason;

}
