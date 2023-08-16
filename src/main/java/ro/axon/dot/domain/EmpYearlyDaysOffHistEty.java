package ro.axon.dot.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "EMP_YEARLY_DAYS_OFF_HIST_ID_SQ", sequenceName = "EMP_YEARLY_DAYS_OFF_HIST_ID_SQ", allocationSize = 1)
@Getter
@Setter
@Table(name = "EMP_YEARLY_DAYS_OFF_HIST")
public class EmpYearlyDaysOffHistEty{

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMP_YEARLY_DAYS_OFF_HIST_ID_SQ")
  @Column(name = "ID")
  private Long id;

  @Column(name = "NO_DAYS")
  private Integer noDays;
  @Column(name = "DESCRIPTION")
  private String description;


  @Enumerated(EnumType.STRING)
  @Column(name = "TYPE")
  private VacationDaysChangeTypeEnum type;


  @Column(name = "CRT_USR")
  private String crtUsr;
  @Column(name = "CRT_TMS")
  private Instant crtTms;

  //@Column(name = "EMP_YEARLY_DAYS_OFF_ID")
 // private Long yearlyDaysOffId;

  @ManyToOne
 // @JoinColumn(name = "EMP_YEARLY_DAYS_OFF_ID")
  @JoinColumn(name = "EMP_YEARLY_DAYS_OFF_ID", referencedColumnName = "ID")
  private EmpYearlyDaysOffEty empYearlyDaysOffEty;



}
