package ro.axon.dot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ro.axon.dot.security.FilterExceptionHandler;
import ro.axon.dot.security.JwtRequestFilter;
import ro.axon.dot.security.JwtTokenUtil;
import ro.axon.dot.service.EmployeeService;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  @Lazy
  private EmployeeService employeeService;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring().antMatchers("/api/v1/login").antMatchers("/api/v1/refresh");
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {

    httpSecurity.csrf().disable()
        .authorizeRequests().anyRequest().authenticated().and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    httpSecurity.addFilterBefore(
        new JwtRequestFilter(employeeService, jwtTokenUtil),
        UsernamePasswordAuthenticationFilter.class);

    httpSecurity.addFilterBefore(new FilterExceptionHandler(), JwtRequestFilter.class);
  }
}
