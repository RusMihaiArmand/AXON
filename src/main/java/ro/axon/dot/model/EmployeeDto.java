package ro.axon.dot.model;

import lombok.Data;

@Data
public class EmployeeDto {

  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String crtUsr;
  private String mdfUsr;
  private String role;
  private Long v;
  private Integer totalVacationDays;
  private TeamDetailsListItem teamDetails;
  private String username;

  public EmployeeDto() {
    
}

  public EmployeeDto(String id, String firstName, String lastName, String email, String crtUsr,
      String mdfUsr, String role, Long v, Integer totalVacationDays,
      TeamDetailsListItem teamDetails,
      String username) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.crtUsr = crtUsr;
    this.mdfUsr = mdfUsr;
    this.role = role;
    this.v = v;
    this.totalVacationDays = totalVacationDays;
    this.teamDetails = teamDetails;
    this.username = username;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCrtUsr() {
    return crtUsr;
  }

  public void setCrtUsr(String crtUsr) {
    this.crtUsr = crtUsr;
  }

  public String getMdfUsr() {
    return mdfUsr;
  }

  public void setMdfUsr(String mdfUsr) {
    this.mdfUsr = mdfUsr;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Long getV() {
    return v;
  }

  public void setV(Long v) {
    this.v = v;
  }

  public Integer getTotalVacationDays() {
    return totalVacationDays;
  }

  public void setTotalVacationDays(Integer totalVacationDays) {
    this.totalVacationDays = totalVacationDays;
  }

  public TeamDetailsListItem getTeamDetails() {
    return teamDetails;
  }

  public void setTeamDetails(TeamDetailsListItem teamDetails) {
    this.teamDetails = teamDetails;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
