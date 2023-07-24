package ro.axon.dot.model;

import lombok.Data;

import java.time.Instant;

@Data
public class TeamDetailsListItem {

    private Long id;
    private String name;
    private String crtUsr;
    private Instant crtTms;
    private String mdfUsr;
    private Instant mdfTms;

}
