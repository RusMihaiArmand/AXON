package ro.axon.dot.security;

import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.exceptions.BusinessErrorCode;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.BusinessException.BusinessExceptionElement;
import ro.axon.dot.service.EmployeeService;

@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final EmployeeService employeeService;
	private final JwtTokenUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final String requestTokenHeader = request.getHeader("Authorization");

		verifyRequestHeader(requestTokenHeader);

		SignedJWT token = jwtTokenUtil.parseToken(requestTokenHeader.substring(7));

		jwtTokenUtil.verifyToken(token);
		jwtTokenUtil.validateClaimSet(token);

		String username = jwtTokenUtil.getUsernameFromToken(token);

		SecurityContext securityContext = SecurityContextHolder.getContext();

		if (securityContext.getAuthentication() == null) {
			EmployeeEty employee = employeeService.loadEmployeeByUsername(username);
			validateToken(token, employee);

			UsernamePasswordAuthenticationToken usernamePasswordAuthToken = new UsernamePasswordAuthenticationToken(employee, null, null);
			usernamePasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			securityContext.setAuthentication(usernamePasswordAuthToken);
		}

		chain.doFilter(request, response);
	}

	private void verifyRequestHeader(String requestTokenHeader){
		if (requestTokenHeader == null || (!requestTokenHeader.startsWith("Bearer "))) {
			Map<String, Object> variables = new HashMap<>();
			variables.put("requestHeader", requestTokenHeader);

			throw new BusinessException(BusinessExceptionElement
					.builder()
					.errorDescription(BusinessErrorCode.REQUEST_HEADER_INVALID)
					.contextVariables(variables).build());

		}
	}

	private void validateToken(SignedJWT token, EmployeeEty employee){
		jwtTokenUtil.isTokenExpired(token);
		jwtTokenUtil.validateToken(token, employee);


	}

}
