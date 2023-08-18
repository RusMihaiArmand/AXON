package ro.axon.dot.service;

import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.axon.dot.domain.TeamEty;
import ro.axon.dot.domain.TeamRepository;
import ro.axon.dot.domain.TeamStatus;
import ro.axon.dot.mapper.TeamMapper;
import ro.axon.dot.model.CreateTeamDetails;
import ro.axon.dot.model.TeamDetailsList;
import ro.axon.dot.security.JwtTokenUtil;

@Service
@RequiredArgsConstructor
public class TeamService {

  private final TeamRepository teamRepository;
  private final JwtTokenUtil tokenUtil;
  private final Clock clock;

  public TeamDetailsList getActiveTeams() {
    var teamDetailsList = new TeamDetailsList();
    teamDetailsList
        .setItems(teamRepository.findByStatus(TeamStatus.ACTIVE)
            .stream()
            .map(TeamMapper.INSTANCE::mapTeamEtyToTeamDto)
            .collect(Collectors.toList()));
    return teamDetailsList;
  }

  @Transactional
  public void saveTeam(CreateTeamDetails teamDetails) {
    Instant now = clock.instant();

    TeamEty teamEty = new TeamEty();
    teamEty.setCrtUsr(tokenUtil.getLoggedUserId());
    teamEty.setCrtTms(now);
    teamEty.setMdfTms(now);
    teamEty.setMdfUsr(tokenUtil.getLoggedUserId());
    teamEty.setStatus(TeamStatus.ACTIVE);
    teamEty.setName(teamDetails.getName());
    TeamMapper.INSTANCE.mapTeamEtyToTeamDto(teamRepository.save(teamEty));

  }

}
