package ro.axon.dot.domain;

import java.sql.Date;
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
public class LegallyDaysOffEty extends SrgKeyEntityTml<Date> {


    @Id
    @Column(name = "DATE")
    private Date date;

    @Column(name = "DESCRIPTION")
    private String desc;

    @Override
    public Date getId() {
        return date;
    }

    @Override
    protected Class<? extends SrgKeyEntityTml<Date>> entityRefClass() {

        return null;
    }

}
