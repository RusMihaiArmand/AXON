package ro.axon.dot.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private List<GrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
      var roles = jwt.getClaim("roles");
      return (roles == null) ? null
          : new HashSet<>(mapRolesToGrantedAuthorities((Collection<String>) roles));
    });
    return jwtAuthenticationConverter;
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {

    httpSecurity.csrf().disable()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/api/v1/login",
            "/api/v1/refresh", "/api/v1/logout").permitAll()
        .antMatchers(HttpMethod.POST,"/api/v1/employees/register", "/api/v1/teams").hasAnyRole("HR")
        .antMatchers(HttpMethod.PATCH, "/api/v1/employees/{employeeId}").hasAnyRole("HR")
        .antMatchers(HttpMethod.POST, "/api/v1/employees/days-off").hasRole("HR")
        .antMatchers(HttpMethod.PATCH,"/api/v1/employees/{employeeId}/inactivate").hasAnyRole("HR")
        .antMatchers(HttpMethod.PATCH,"/api/v1/employees/{employeeId}/requests/{requestId}").hasAnyRole("HR", "TEAM_LEAD")
        .antMatchers(HttpMethod.GET,"/api/v1/requests").hasAnyRole("HR", "TEAM_LEAD")

        .anyRequest().authenticated().and()

        .oauth2ResourceServer(
            oauth -> oauth.jwt(
                token -> token.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        )
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
            .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));
  }
}
