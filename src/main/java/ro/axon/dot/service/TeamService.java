package ro.axon.dot.service;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.mapper.TeamMapper;
import ro.axon.dot.model.TeamDetailsList;

@Service
@RequiredArgsConstructor
public class TeamService {

  private final TeamRepository teamRepository;

  public TeamDetailsList getTeamsDetails() {
    var teamDetailsList = new TeamDetailsList();
    teamDetailsList.setItems(
        teamRepository.findAll().stream().map(TeamMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));
    return teamDetailsList;
  }

}
