package ro.axon.dot.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axon.dot.model.TeamDetailsList;
import ro.axon.dot.service.TeamService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TeamApi {

    private final TeamService teamService;

    @GetMapping("teams")
    public ResponseEntity<TeamDetailsList> getTeamDetailsList() {
        return ResponseEntity.ok(teamService.getTeamsDetails());
    }
}
