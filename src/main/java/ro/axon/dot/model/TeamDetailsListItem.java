package ro.axon.dot.model;

import lombok.Data;

import java.time.Instant;
import ro.axon.dot.domain.TeamStatus;
import ro.axon.dot.service.TeamService;

@Data
public class TeamDetailsListItem {

    private Long id;
    private String name;
    private String crtUsr;
    private Instant crtTms;
    private String mdfUsr;
    private Instant mdfTms;
    private TeamStatus status;

}
