package ro.axon.dot.domain.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import ro.axon.dot.domain.SrgKeyEntityTml;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "EMPLOYEE")
public class EmployeeEty extends SrgKeyEntityTml<String> {

  @Id
  @GeneratedValue(generator = "employee-uuid")
  @GenericGenerator(name = "employee-uuid", strategy = "uuid2")
  @Column(name = "EMPLOYEE_ID")
  private String id;

  @Column(name = "FIRST_NAME")
  private String firstName;
  @Column(name = "LAST_NAME")
  private String lastName;
  @Column(name = "EMAIL")
  private String email;
  @Column(name = "CRT_USR")
  private String crtUsr;
  @Column(name = "CRT_TMS")
  private Instant crtTms;
  @Column(name = "MDF_USR")
  private String mdfUsr;
  @Column(name = "MDF_TMS")
  private Instant mdfTms;
  @Column(name = "ROLE")
  private String role;
  @Column(name = "STATUS")
  private String status;
  @Column(name = "CONTRACT_START_DATE")
  private LocalDate contractStartDate;
  @Column(name = "CONTRACT_END_DATE")
  private LocalDate contractEndDate;
  @Column(name = "USERNAME")
  private String username;
  @Column(name = "PASSWORD")
  private String password;
  @ManyToOne
  @JoinColumn(name = "TEAM_ID")
  private TeamEty team;

  @OneToMany(
      mappedBy = "employee",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private Set<LeaveRequestEty> leaveRequests = new HashSet<>();

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
  private Set<EmpYearlyDaysOffEty> empYearlyDaysOff = new HashSet<>();

  public void addLeaveRequest(LeaveRequestEty leaveRequestEty) {
    leaveRequests.add(leaveRequestEty);
    leaveRequestEty.setEmployee(this);
  }

  public void removeLeaveRequests(LeaveRequestEty leaveRequestEty){
    leaveRequests.remove(leaveRequestEty);
    leaveRequestEty.setEmployee(null);
  }

  @Override
  protected Class<? extends SrgKeyEntityTml<String>> entityRefClass() {
    return EmployeeEty.class;
  }

}
