package ro.axon.dot.api;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.sql.Date;
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
import ro.axon.dot.domain.OffDayEty;
import ro.axon.dot.domain.OffDayRepository;
import ro.axon.dot.model.OffDayList;
import ro.axon.dot.service.OffDayService;


@ExtendWith(MockitoExtension.class)
class OffDaysApiTest {


  @Mock
  OffDayRepository offDayRepository;
  OffDayService offDayService;

  OffDayList days;

  MockMvc mockMvc;

  @BeforeEach
  void setUp(){
    days = new OffDayList();
    days.setDays(new ArrayList<>());

    MockitoAnnotations.openMocks(this);
    offDayService = new OffDayService(offDayRepository);
  }



  @Test
  void getDaysOffTest() {

    OffDayEty[] offDayEty = new OffDayEty[15];

    offDayEty[0] = new OffDayEty();
    offDayEty[0].setDate(Date.valueOf("2023-01-01"));
    offDayEty[0].setDesc("An nou");

    offDayEty[1] = new OffDayEty();
    offDayEty[1].setDate(Date.valueOf("2023-01-02"));
    offDayEty[1].setDesc("An nou");

    offDayEty[2] = new OffDayEty();
    offDayEty[2].setDate(Date.valueOf("2023-01-24"));
    offDayEty[2].setDesc("Ziua Unirii");

    offDayEty[3] = new OffDayEty();
    offDayEty[3].setDate(Date.valueOf("2023-04-22"));
    offDayEty[3].setDesc("Pastele");

    offDayEty[4] = new OffDayEty();
    offDayEty[4].setDate(Date.valueOf("2023-04-24"));
    offDayEty[4].setDesc("Pastele");

    offDayEty[5] = new OffDayEty();
    offDayEty[5].setDate(Date.valueOf("2023-04-25"));
    offDayEty[5].setDesc("Pastele");

    offDayEty[6] = new OffDayEty();
    offDayEty[6].setDate(Date.valueOf("2023-05-01"));
    offDayEty[6].setDesc("Ziua Muncii");

    offDayEty[7] = new OffDayEty();
    offDayEty[7].setDate(Date.valueOf("2023-06-01"));
    offDayEty[7].setDesc("Ziua Copilului");

    offDayEty[8] = new OffDayEty();
    offDayEty[8].setDate(Date.valueOf("2023-06-13"));
    offDayEty[8].setDesc("Rusalii");

    offDayEty[9] = new OffDayEty();
    offDayEty[9].setDate(Date.valueOf("2023-08-15"));
    offDayEty[9].setDesc("Adormirea...");

    offDayEty[10] = new OffDayEty();
    offDayEty[10].setDate(Date.valueOf("2023-11-30"));
    offDayEty[10].setDesc("Sf Andrei");

    offDayEty[11] = new OffDayEty();
    offDayEty[11].setDate(Date.valueOf("2023-12-01"));
    offDayEty[11].setDesc("Romania");

    offDayEty[12] = new OffDayEty();
    offDayEty[12].setDate(Date.valueOf("2023-12-25"));
    offDayEty[12].setDesc("Craciunul");

    offDayEty[13] = new OffDayEty();
    offDayEty[13].setDate(Date.valueOf("2023-12-26"));
    offDayEty[13].setDesc("Craciunul");

    offDayEty[14] = new OffDayEty();
    offDayEty[14].setDate(Date.valueOf("2024-01-01"));
    offDayEty[14].setDesc("2024");


    when(offDayRepository.findAll()).thenReturn(Arrays.asList(offDayEty));


    days = offDayService.getOffDays(null,null);
    assertEquals(15,days.getDays().size());


    List<String> period = new ArrayList<>();
    period.add("2023-04");period.add("2023-05");
    List<String> years = new ArrayList<>();
    years.add("2022");

    days = offDayService.getOffDays(period,years);
    assertEquals(4,days.getDays().size());


    years.add("2023");
    days = offDayService.getOffDays(period,years);
    assertEquals(14,days.getDays().size());

  }

}