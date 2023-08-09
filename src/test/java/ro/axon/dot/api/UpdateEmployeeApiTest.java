package ro.axon.dot.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Optional;

import ro.axon.dot.api.UpdateEmployeeApi;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessExceptionElement;
import ro.axon.dot.model.EmployeeDto;
import ro.axon.dot.service.UpdateEmployeeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UpdateEmployeeApiTest {

    @Mock
    private UpdateEmployeeService updateEmployeeService;

    @InjectMocks
    private UpdateEmployeeApi updateEmployeeApi;

    @InjectMocks
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateEmployeeDetails_Success() {
        MockitoAnnotations.openMocks(this);

        UpdateEmployeeApi updateEmployeeApi = new UpdateEmployeeApi(updateEmployeeService);

        ResponseEntity<Void> response = updateEmployeeApi.updateEmployeeDetails("employeeId", new EmployeeDto());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(updateEmployeeService, times(1)).updateEmployeeDetails(any(), any());
    }

    @Test
    public void testUpdateEmployeeDetails_Conflict() {
    
    BusinessExceptionElement conflictElement = new BusinessExceptionElement(
        BusinessErrorCode.CONFLICT,
        null
    );

    BusinessException conflictException = new BusinessException(conflictElement);

    when(employeeRepository.findById(anyString()))
        .thenReturn(Optional.of(new EmployeeEty()));

    doThrow(conflictException).when(updateEmployeeService)
        .updateEmployeeDetails(anyString(), any(EmployeeDto.class));

    ResponseEntity<Void> response = updateEmployeeApi.updateEmployeeDetails("1", new EmployeeDto());
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    verify(updateEmployeeService).updateEmployeeDetails(eq("1"), any(EmployeeDto.class));
}

@Test
void updateEmployeeDetails_BadRequest() {
    BusinessExceptionElement badRequestElement = new BusinessExceptionElement(
        BusinessErrorCode.CONFLICT,
        null
    );

    BusinessException badRequestException = new BusinessException(badRequestElement);

    doThrow(badRequestException).when(updateEmployeeService)
            .updateEmployeeDetails(anyString(), any(EmployeeDto.class));

    ResponseEntity<Void> response = updateEmployeeApi.updateEmployeeDetails("1", new EmployeeDto());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    verify(updateEmployeeService).updateEmployeeDetails(eq("1"), any(EmployeeDto.class));
}
}