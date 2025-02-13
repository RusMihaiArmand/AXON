package ro.axon.dot.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.axon.dot.model.CreateTeamDetails;
import ro.axon.dot.model.TeamDetailsList;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.service.TeamService;

@ExtendWith(MockitoExtension.class)
class TeamApiTest {
  @Mock
  private TeamService teamService;
  @InjectMocks
  private TeamApi teamApi;
  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(teamApi).build();
  }

  @Test
  void getTeamDetailsList() throws Exception {
    TeamDetailsListItem team1 = new TeamDetailsListItem();
    team1.setId(1L);

    TeamDetailsListItem team2 = new TeamDetailsListItem();
    team2.setId(2L);
    TeamDetailsList teamDetailsList = new TeamDetailsList();
    teamDetailsList.setItems(Arrays.asList(team1, team2));

    when(teamService.getActiveTeams()).thenReturn(teamDetailsList);

    mockMvc.perform(get("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.items[0].id").value(1L))
        .andExpect(jsonPath("$.items[1].id").value(2L));

    teamDetailsList.setItems(new ArrayList<>());
    when(teamService.getActiveTeams()).thenReturn(teamDetailsList);
    mockMvc.perform(get("/api/v1/teams")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(0)));
  }

  @Test
  @DisplayName("When create team then return status code")
  void whenCreateTeamThenReturnStatus() throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();
    CreateTeamDetails createTeamDetails = new CreateTeamDetails();
    createTeamDetails.setName("internship");

    mockMvc.perform(post("/api/v1/teams")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createTeamDetails)))
        .andExpect(status().isCreated());

    verify(teamService).saveTeam(Mockito.any(CreateTeamDetails.class));

  }
}