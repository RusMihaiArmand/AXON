package ro.axon.dot.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.domain.TeamStatus;
import ro.axon.dot.model.CreateTeamDetails;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.security.JwtTokenUtil;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock
  private Clock clock;
  @Mock
  private TeamRepository teamRepository;
  @Mock
  private JwtTokenUtil tokenUtil;
  @InjectMocks
  private TeamService teamService;

  @Test
  void getActiveTeams() {
    TeamEty team1 = new TeamEty();
    team1.setId(1L);

    TeamEty team2 = new TeamEty();
    team2.setId(2L);

    when(teamRepository.findByStatus(TeamStatus.ACTIVE)).thenReturn(Arrays.asList(team1, team2));

    List<TeamDetailsListItem> teams = teamService.getActiveTeams().getItems();
    assertEquals(teams.size(), 2);
    assertEquals(teams.get(0).getId(), 1L);
    assertEquals(teams.get(1).getId(), 2L);

  }

  @Test
  @DisplayName("When save team then verify is called successfully")
  void whenSaveTeamThenSuccessful() {

    CreateTeamDetails teamDetails = new CreateTeamDetails();
    teamDetails.setName("Test Team");

    TeamEty team1 = new TeamEty();
    team1.setId(1L);
    team1.setName("Test Team");
    team1.setCrtUsr("usr_hr");
    team1.setMdfUsr("usr_hr");

    when(tokenUtil.getLoggedUserId()).thenReturn("usr_hr");
    when(teamRepository.save(any())).thenReturn(team1);
    when(clock.instant()).thenReturn(Clock.systemDefaultZone().instant());

    teamService.saveTeam(teamDetails);

    verify(teamRepository).save(Mockito.argThat(saveTeamEty ->
        "Test Team".equals(saveTeamEty.getName()) &&
            "usr_hr".equals(saveTeamEty.getCrtUsr()) &&
            "usr_hr".equals(saveTeamEty.getMdfUsr()) &&
            saveTeamEty.getCrtTms() != null &&
            saveTeamEty.getMdfTms() != null &&
            TeamStatus.ACTIVE.equals(saveTeamEty.getStatus())
    ));
  }

  @Test
  @DisplayName("When save team then return error")
  void whenSaveTeamThenError() {

    CreateTeamDetails teamDetails = new CreateTeamDetails();
    teamDetails.setName("Test Team");

    when(teamRepository.save(Mockito.any(TeamEty.class))).thenThrow(RuntimeException.class);
    when(tokenUtil.getLoggedUserId()).thenReturn("mdf_usr");
    when(clock.instant()).thenReturn(Clock.systemDefaultZone().instant());

    assertThatCode(() -> teamService.saveTeam(teamDetails)).isInstanceOf(RuntimeException.class);

  }

}