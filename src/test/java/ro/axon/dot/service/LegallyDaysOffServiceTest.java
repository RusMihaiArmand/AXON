package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import ro.axon.dot.domain.persistence.LegallyDaysOffPersistenceManager;
import ro.axon.dot.domain.entity.LegallyDaysOffEty;
import ro.axon.dot.domain.repositories.LegallyDaysOffRepository;
import ro.axon.dot.model.LegallyDaysOffList;


@ExtendWith(MockitoExtension.class)
class LegallyDaysOffServiceTest {


  @Mock
  LegallyDaysOffRepository legallyDaysOffRepository;

  LegallyDaysOffPersistenceManager legallyDaysOffPersistenceManager;
  LegallyDaysOffService legallyDaysOffService;
  LegallyDaysOffList days;

  MockMvc mockMvc;

  @BeforeEach
  void setUp(){
    days = new LegallyDaysOffList();
    days.setDays(new ArrayList<>());

    MockitoAnnotations.openMocks(this);

    legallyDaysOffPersistenceManager = new LegallyDaysOffPersistenceManager(legallyDaysOffRepository);
    legallyDaysOffService = new LegallyDaysOffService(legallyDaysOffPersistenceManager);
  }



  @Test
  void getDaysOffTest() {

    LegallyDaysOffEty[] legallyDaysOffEty = new LegallyDaysOffEty[15];

    legallyDaysOffEty[0] = new LegallyDaysOffEty();
    legallyDaysOffEty[0].setDate( LocalDate.parse("2023-01-01") );
    legallyDaysOffEty[0].setDesc("An nou");

    legallyDaysOffEty[1] = new LegallyDaysOffEty();
    legallyDaysOffEty[1].setDate(LocalDate.parse("2023-01-02"));
    legallyDaysOffEty[1].setDesc("An nou");

    legallyDaysOffEty[2] = new LegallyDaysOffEty();
    legallyDaysOffEty[2].setDate(LocalDate.parse("2023-01-24"));
    legallyDaysOffEty[2].setDesc("Ziua Unirii");

    legallyDaysOffEty[3] = new LegallyDaysOffEty();
    legallyDaysOffEty[3].setDate(LocalDate.parse("2023-04-22"));
    legallyDaysOffEty[3].setDesc("Pastele");

    legallyDaysOffEty[4] = new LegallyDaysOffEty();
    legallyDaysOffEty[4].setDate(LocalDate.parse("2023-04-24"));
    legallyDaysOffEty[4].setDesc("Pastele");

    legallyDaysOffEty[5] = new LegallyDaysOffEty();
    legallyDaysOffEty[5].setDate(LocalDate.parse("2023-04-25"));
    legallyDaysOffEty[5].setDesc("Pastele");

    legallyDaysOffEty[6] = new LegallyDaysOffEty();
    legallyDaysOffEty[6].setDate(LocalDate.parse("2023-05-01"));
    legallyDaysOffEty[6].setDesc("Ziua Muncii");

    legallyDaysOffEty[7] = new LegallyDaysOffEty();
    legallyDaysOffEty[7].setDate(LocalDate.parse("2023-06-01"));
    legallyDaysOffEty[7].setDesc("Ziua Copilului");

    legallyDaysOffEty[8] = new LegallyDaysOffEty();
    legallyDaysOffEty[8].setDate(LocalDate.parse("2023-06-13"));
    legallyDaysOffEty[8].setDesc("Rusalii");

    legallyDaysOffEty[9] = new LegallyDaysOffEty();
    legallyDaysOffEty[9].setDate(LocalDate.parse("2023-08-15"));
    legallyDaysOffEty[9].setDesc("Adormirea...");

    legallyDaysOffEty[10] = new LegallyDaysOffEty();
    legallyDaysOffEty[10].setDate(LocalDate.parse("2023-11-30"));
    legallyDaysOffEty[10].setDesc("Sf Andrei");

    legallyDaysOffEty[11] = new LegallyDaysOffEty();
    legallyDaysOffEty[11].setDate(LocalDate.parse("2023-12-01"));
    legallyDaysOffEty[11].setDesc("Romania");

    legallyDaysOffEty[12] = new LegallyDaysOffEty();
    legallyDaysOffEty[12].setDate(LocalDate.parse("2023-12-25"));
    legallyDaysOffEty[12].setDesc("Craciunul");

    legallyDaysOffEty[13] = new LegallyDaysOffEty();
    legallyDaysOffEty[13].setDate(LocalDate.parse("2023-12-26"));
    legallyDaysOffEty[13].setDesc("Craciunul");

    legallyDaysOffEty[14] = new LegallyDaysOffEty();
    legallyDaysOffEty[14].setDate(LocalDate.parse("2024-01-01"));
    legallyDaysOffEty[14].setDesc("2024");


    when(legallyDaysOffRepository.findAll()).thenReturn(Arrays.asList(legallyDaysOffEty));

    days = legallyDaysOffService.getOffDays(null,null);
    assertEquals(15,days.getDays().size());


    List<String> period = new ArrayList<>();
    period.add("2023-04"); period.add("2023-06");
    List<String> years = new ArrayList<>();


    days = legallyDaysOffService.getOffDays(period,years);
    assertEquals(5,days.getDays().size());


    years.add("2022");
    days = legallyDaysOffService.getOffDays(period,years);
    assertEquals(0,days.getDays().size());

    years.add("2023");
    days = legallyDaysOffService.getOffDays(period,years);
    assertEquals(14,days.getDays().size());


    days = legallyDaysOffService.getOffDays(null,years);
    assertEquals(14,days.getDays().size());



  }

}