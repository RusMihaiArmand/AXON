package ro.axon.dot.model;

import lombok.Data;

@Data
public class EmployeeUpdateRequest {

  private String teamId;
  private String firstName;
  private String lastName;
  private String email;
  private String role;
  private Long v;


  public EmployeeUpdateRequest() {}

  public EmployeeUpdateRequest(String teamId, String firstName, String lastName, String email, String role, Long v) {
    this.teamId = teamId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.role = role;
    this.v = v;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
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

}
