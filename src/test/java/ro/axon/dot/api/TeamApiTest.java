package ro.axon.dot.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.axon.dot.model.TeamDetailsList;
import ro.axon.dot.model.TeamDetailsListItem;
import ro.axon.dot.service.TeamService;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
}