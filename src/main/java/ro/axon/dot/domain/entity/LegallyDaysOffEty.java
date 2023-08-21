package ro.axon.dot.domain.entity;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "LEGALLY_DAYS_OFF")
public class LegallyDaysOffEty  {

    @Id
    @Column(name = "DATE")
    private LocalDate date;
    @Column(name = "DESCRIPTION")
    private String desc;

    public LocalDate getId() {
        return date;
    }

}
