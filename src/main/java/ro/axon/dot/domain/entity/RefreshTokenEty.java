package ro.axon.dot.domain.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ro.axon.dot.domain.enums.TokenStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "REFRESH_TOKEN")
public class RefreshTokenEty {

  @Id
  @Column(name = "ID", unique = true)
  private String id;

  @Column(name = "STATUS")
  @Enumerated(value = EnumType.STRING)
  private TokenStatus status;

  @OneToOne
  @JoinColumn(name = "AUDIENCE", referencedColumnName = "EMPLOYEE_ID")
  private EmployeeEty employee;

  @Column(name = "CRT_TMS")
  private Instant crtTms;
  @Column(name = "MDF_TMS")
  private Instant mdfTms;
  @Column(name = "EXP_TMS")
  private Instant expTms;

}
