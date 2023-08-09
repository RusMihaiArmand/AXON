package ro.axon.dot.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "EMP_YEARLY_DAYS_OFF_ID_SQ", sequenceName = "EMP_YEARLY_DAYS_OFF_ID_SQ", allocationSize = 1)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "EMP_YEARLY_DAYS_OFF")
public class EmpYearlyDaysOffEty{

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMP_YEARLY_DAYS_OFF_ID_SQ")
  @Column(name = "ID")
  private Long id;

  @Column(name = "TOTAL_NO_DAYS")
  private Integer totalNoDays;
  @Column(name = "YEAR")
  private Integer year;

  @OneToMany(mappedBy = "empYearlyDaysOffEty")
  private Set<EmpYearlyDaysOffHistEty> empYearlyDaysOffHistEtySet = new HashSet<>();

  @Column(name = "EMPLOYEE_ID")
  private String employeeId;



}
