package ro.axon.dot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_END_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CONTRACT_START_DATE;
import static ro.axon.dot.EmployeeTestAttributes.CRT_TMS;
import static ro.axon.dot.EmployeeTestAttributes.CRT_USR;
import static ro.axon.dot.EmployeeTestAttributes.EMAIL;
import static ro.axon.dot.EmployeeTestAttributes.FIRST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.ID;
import static ro.axon.dot.EmployeeTestAttributes.LAST_NAME;
import static ro.axon.dot.EmployeeTestAttributes.MDF_TMS;
import static ro.axon.dot.EmployeeTestAttributes.MDF_USR;
import static ro.axon.dot.EmployeeTestAttributes.ROLE;
import static ro.axon.dot.EmployeeTestAttributes.STATUS;
import static ro.axon.dot.EmployeeTestAttributes.TEAM_ETY;
import static ro.axon.dot.EmployeeTestAttributes.USERNAME;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.axon.dot.domain.entity.EmployeeEty;
import ro.axon.dot.domain.entity.RefreshTokenEty;
import ro.axon.dot.domain.repositories.RefreshTokenRepository;
import ro.axon.dot.domain.enums.TokenStatus;

class RefreshTokenServiceTest {
  @Mock
  RefreshTokenRepository repository;
  RefreshTokenService service;
  private EmployeeEty employee;

  private static EmployeeEty setupEmployee() {
    EmployeeEty employee = new EmployeeEty();

    employee.setId(ID);
    employee.setFirstName(FIRST_NAME);
    employee.setLastName(LAST_NAME);
    employee.setEmail(EMAIL);
    employee.setCrtUsr(CRT_USR);
    employee.setCrtTms(CRT_TMS);
    employee.setMdfUsr(MDF_USR);
    employee.setMdfTms(MDF_TMS);
    employee.setRole(ROLE);
    employee.setStatus(STATUS);
    employee.setContractStartDate(CONTRACT_START_DATE);
    employee.setContractEndDate(CONTRACT_END_DATE);
    employee.setUsername(USERNAME);
    employee.setTeam(TEAM_ETY);
    return employee;
  }

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    service = new RefreshTokenService(repository);

    employee = setupEmployee();
  }

  @Test
  void saveRefreshToken() {

    Instant now = new Date().toInstant();

    RefreshTokenEty refreshToken = new RefreshTokenEty(
        String.valueOf(UUID.randomUUID()),
        TokenStatus.ACTIVE,
        employee,
        now,
        now,
        now.plusMillis(120000)
    );

    when(repository.save(any())).thenReturn(refreshToken);

    RefreshTokenEty returnedToken = service.saveRefreshToken(refreshToken);

    assertNotNull(returnedToken);
    assertEquals(refreshToken.getId(), returnedToken.getId());
    assertEquals(refreshToken.getStatus(), returnedToken.getStatus());
    assertEquals(refreshToken.getEmployee(), returnedToken.getEmployee());
    assertEquals(refreshToken.getCrtTms(), returnedToken.getCrtTms());
    assertEquals(refreshToken.getMdfTms(), returnedToken.getMdfTms());
    assertEquals(refreshToken.getExpTms(), returnedToken.getExpTms());

  }

  @Test
  void findTokenByKeyId() {
    Instant now = new Date().toInstant();

    String uuid = String.valueOf(UUID.randomUUID());

    RefreshTokenEty refreshToken = new RefreshTokenEty(
        uuid,
        TokenStatus.ACTIVE,
        employee,
        now,
        now,
        now.plusMillis(120000)
    );

    when(repository.findById(uuid)).thenReturn(Optional.of(refreshToken));

    RefreshTokenEty returnedToken = service.findTokenByKeyId(uuid);

    assertNotNull(returnedToken);

    assertEquals(refreshToken.getId(), returnedToken.getId());
    assertEquals(refreshToken.getStatus(), returnedToken.getStatus());
    assertEquals(refreshToken.getEmployee(), returnedToken.getEmployee());
    assertEquals(refreshToken.getCrtTms(), returnedToken.getCrtTms());
    assertEquals(refreshToken.getMdfTms(), returnedToken.getMdfTms());
    assertEquals(refreshToken.getExpTms(), returnedToken.getExpTms());
  }

}