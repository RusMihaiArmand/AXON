package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.EmployeeRolesList;
import ro.axon.dot.service.RolesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/misc")
public class MiscApi {

  private final RolesService rolesService;
  @GetMapping("roles")
  public ResponseEntity<EmployeeRolesList> getEmployeeRolesList() {

    return ResponseEntity.ok(rolesService.getEmployeeRoles());
  }

}
