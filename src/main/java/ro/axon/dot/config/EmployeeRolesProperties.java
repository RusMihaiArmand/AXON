package ro.axon.dot.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "ro.axon.core")
@ConstructorBinding
public record EmployeeRolesProperties(List<String> roles) {

}
