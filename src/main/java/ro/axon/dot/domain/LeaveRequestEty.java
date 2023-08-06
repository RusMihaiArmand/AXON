package ro.axon.dot.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@SequenceGenerator(name = "LEAVE_REQUEST_ID_SQ", sequenceName = "LEAVE_REQUEST_ID_SQ", allocationSize = 1)
@Getter
@Setter
@Table(name = "LEAVE_REQUEST")
public class LeaveRequestEty extends SrgKeyEntityTml<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LEAVE_REQUEST_ID_SQ")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "EMPLOYEE_ID")
    private EmployeeEty employee;
    @Column(name = "CRT_USR")
    private String crtUsr;
    @Column(name = "CRT_TMS")
    private Instant crtTms;
    @Column(name = "MDF_USR")
    private String mdfUsr;
    @Column(name = "MDF_TMS")
    private Instant mdfTms;
    @Column(name = "START_DATE")
    private LocalDate startDate;
    @Column(name = "END_DATE")
    private LocalDate endDate;
    @Column(name = "NO_DAYS")
    private Integer noDays;
    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private LeaveRequestEtyTypeEnum type;
    @Column(name = "STATUS")
    @Enumerated(value = EnumType.STRING)
    private LeaveRequestEtyStatusEnum status;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "REJECT_REASON")
    private String rejectReason;

    @Override
    protected Class<? extends SrgKeyEntityTml<Long>> entityRefClass() {
        return TeamEty.class;
    }
}
