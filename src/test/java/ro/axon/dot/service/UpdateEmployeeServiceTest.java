package ro.axon.dot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.model.EmployeeDto;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.domain.EmployeeRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UpdateEmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UpdateEmployeeService updateEmployeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateEmployeeDetails_ValidVersion() {
        EmployeeDto employeeDto = new EmployeeDto();
        EmployeeEty employeeEty = new EmployeeEty();
        employeeEty.setV(1L);

       
        when(employeeRepository.findById(anyString())).thenReturn(java.util.Optional.of(employeeEty));
        when(employeeRepository.save(any(EmployeeEty.class))).thenReturn(employeeEty);

        
        updateEmployeeService.updateEmployeeDetails("employeeId", employeeDto);

        
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateEmployeeDetails_InvalidVersion() {
        
        EmployeeDto employeeDto = new EmployeeDto();
        EmployeeEty employeeEty = new EmployeeEty();
        employeeEty.setV(2L);

        when(employeeRepository.findById(anyString())).thenReturn(java.util.Optional.of(employeeEty));

        assertThrows(BusinessException.class, () ->
            updateEmployeeService.updateEmployeeDetails("employeeId", employeeDto)
        );

        verify(employeeRepository, never()).save(any());
    }

}
