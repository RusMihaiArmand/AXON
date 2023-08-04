package ro.axon.dot.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "EMPLOYEE")
public class EmployeeEty extends SrgKeyEntityTml<String>{

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
  private TeamEty team;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "EMPLOYEE_ID")
  private Set<EmpYearlyDaysOffEty> empYearlyDaysOff = new HashSet<>();

  @Override
  protected Class<? extends SrgKeyEntityTml<String>> entityRefClass() {
    return EmployeeEty.class;
  }


  public EmployeeEty(String id, String firstName, String lastName, String email, String crtUsr,
      Instant crtTms, String mdfUsr, Instant mdfTms, String role, String status,
      LocalDate contractStartDate, LocalDate contractEndDate, String username, TeamEty team ) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.crtUsr = crtUsr;
    this.crtTms = crtTms;
    this.mdfUsr = mdfUsr;
    this.mdfTms = mdfTms;
    this.role = role;
    this.status = status;
    this.contractStartDate = contractStartDate;
    this.contractEndDate = contractEndDate;
    this.username = username;
    this.team = team;
  }
}
