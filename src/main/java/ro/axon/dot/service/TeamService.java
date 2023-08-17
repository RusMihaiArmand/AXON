package ro.axon.dot.service;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.domain.TeamStatus;
import ro.axon.dot.mapper.TeamMapper;
import ro.axon.dot.model.CreateTeamDetails;
import ro.axon.dot.model.TeamDetailsList;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

  private final TeamRepository teamRepository;

  @Transactional(readOnly = true)
  public TeamDetailsList getActiveTeams() {
    var teamDetailsList = new TeamDetailsList();
    teamDetailsList
        .setItems(teamRepository.findByStatus(TeamStatus.ACTIVE)
            .stream()
            .map(TeamMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));
    return teamDetailsList;
  }

  public void saveTeam(CreateTeamDetails teamDetails) {

    TeamEty teamEty = new TeamEty();
    teamEty.setCrtUsr("User");        //todo to be modified when login endpoint is available
    teamEty.setCrtTms(Instant.now());
    teamEty.setMdfTms(Instant.now());
    teamEty.setMdfUsr("User");
    teamEty.setStatus(TeamStatus.ACTIVE);
    teamEty.setName(teamDetails.getName());
    TeamMapper.INSTANCE.mapTeamEtyToTeamDto(teamRepository.save(teamEty));

  }

}
