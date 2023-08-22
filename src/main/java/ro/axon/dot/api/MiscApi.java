package ro.axon.dot.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.EmployeeRolesList;
import ro.axon.dot.model.LegallyDaysOffList;
import ro.axon.dot.service.LegallyDaysOffService;
import ro.axon.dot.service.RolesService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/misc")
public class MiscApi {

  private final RolesService rolesService;
  private final LegallyDaysOffService legallyDaysOffService;

  @GetMapping("roles")
  public ResponseEntity<EmployeeRolesList> getEmployeeRolesList() {

    return ResponseEntity.ok(rolesService.getEmployeeRoles());
  }

  @GetMapping("legally-days-off")
  public ResponseEntity<LegallyDaysOffList> getDaysOff(@RequestParam("periods") List<String> periods,
      @RequestParam("years") List<String> years) {

    return ResponseEntity.ok(legallyDaysOffService.getOffDays(periods, years));
  }

}
