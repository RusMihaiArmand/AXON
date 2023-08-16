package ro.axon.dot.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@SequenceGenerator(name = "TEAM_ID_SQ", sequenceName = "TEAM_ID_SQ", allocationSize = 1)
@Getter
@Setter
@Table(name = "TEAM")
public class TeamEty extends SrgKeyEntityTml<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEAM_ID_SQ")
    private Long id;

    @Column(name = "NAME")
    private String name;
    @Column(name = "CRT_USR")
    private String crtUsr;
    @Column(name = "CRT_TMS")
    private Instant crtTms;
    @Column(name = "MDF_USR")
    private String mdfUsr;
    @Column(name = "MDF_TMS")
    private Instant mdfTms;
    @Column(name = "STATUS")
    @Enumerated(value = EnumType.STRING)
    private TeamStatus status;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private Set<EmployeeEty> employees = new HashSet<>();

    @Override
    protected Class<? extends SrgKeyEntityTml<Long>> entityRefClass() {
        return TeamEty.class;
    }
}