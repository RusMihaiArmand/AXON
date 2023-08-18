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
import ro.axon.dot.security.JwtTokenUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

  private final TeamRepository teamRepository;
  private final JwtTokenUtil tokenUtil;

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
    teamEty.setCrtUsr(tokenUtil.getLoggedUserId());
    teamEty.setCrtTms(Instant.now());
    teamEty.setMdfTms(Instant.now());
    teamEty.setMdfUsr(tokenUtil.getLoggedUserId());
    teamEty.setStatus(TeamStatus.ACTIVE);
    teamEty.setName(teamDetails.getName());
    TeamMapper.INSTANCE.mapTeamEtyToTeamDto(teamRepository.save(teamEty));

  }

}
