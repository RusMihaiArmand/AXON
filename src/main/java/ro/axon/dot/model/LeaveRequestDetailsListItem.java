package ro.axon.dot.model;

import lombok.Data;
import ro.axon.dot.domain.LeaveRequestEtyStatusEnum;
import ro.axon.dot.domain.LeaveRequestEtyTypeEnum;

import java.time.LocalDate;
import java.time.Instant;

@Data
public class LeaveRequestDetailsListItem {

    private Long id;
    private String employeeId;
    private String crtUsr;
    private Instant crtTms;
    private String mdfUsr;
    private Instant mdfTms;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer noDays;
    private LeaveRequestEtyTypeEnum type;
    private LeaveRequestEtyStatusEnum status;
    private String description;
    private String rejectReason;

}
