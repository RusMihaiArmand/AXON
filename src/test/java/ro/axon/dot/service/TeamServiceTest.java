package ro.axon.dot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.axon.dot.domain.Status;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.model.TeamDetailsListItem;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository);
    }

    @Test
    void getActiveTeams() {
        TeamEty team1 = new TeamEty();
        team1.setId(1L);

        TeamEty team2 = new TeamEty();
        team2.setId(2L);

        when(teamRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(team1, team2));

        List<TeamDetailsListItem> teams = teamService.getActiveTeams().getItems();
        assertEquals(teams.size(), 2);
        assertEquals(teams.get(0).getId(), 1L);
        assertEquals(teams.get(1).getId(), 2L);

    }
}