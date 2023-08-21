package ro.axon.dot.api;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.CreateTeamDetails;
import ro.axon.dot.model.TeamDetailsList;
import ro.axon.dot.service.TeamService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamApi {

  private final TeamService teamService;
  @GetMapping
  public ResponseEntity<TeamDetailsList> getTeamDetailsList() {
    return ResponseEntity.ok(teamService.getActiveTeams());
  }
  @PostMapping
  public ResponseEntity<Void> createTeam(@Valid @RequestBody CreateTeamDetails teamDetails) {
    teamService.saveTeam(teamDetails);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

}

