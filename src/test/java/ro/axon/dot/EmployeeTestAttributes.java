package ro.axon.dot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import ro.axon.dot.domain.TeamEty;

public class EmployeeTestAttributes {
  public static final String ID = "000003e8-3121-21ee-8100-325096b39f47";
  public static final String FIRST_NAME = "Andrei";
  public static final String LAST_NAME = "Muresan";
  public static final String EMAIL = "someEmail@gmail.com";
  public static final String CRT_USR = "user1";
  public static final Instant CRT_TMS = LocalDateTime.now().toInstant(ZoneOffset.ofHours(0));
  public static final String MDF_USR = "user2";
  public static final Instant MDF_TMS = LocalDateTime.now().toInstant(ZoneOffset.ofHours(0));
  public static final String  ROLE = "HR";
  public static final String STATUS = "status";
  public static final Instant CONTRACT_START_DATE = Instant.now();
  public static final Instant CONTRACT_END_DATE = Instant.now();
  public static final Long V = 2L;
  public static final TeamEty TEAM_ETY = new TeamEty();
  public static final String USERNAME = "user.test";
}
