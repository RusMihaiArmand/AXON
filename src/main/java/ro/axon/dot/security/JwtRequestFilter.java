package ro.axon.dot.security;

import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.text.ParseException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import ro.axon.dot.domain.EmployeeEty;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.service.EmployeeService;

@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final EmployeeService employeeService;
	private final JwtTokenUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final String requestTokenHeader = request.getHeader("Authorization");

		String username;
		SignedJWT jwtToken;

		//Verify if token starts with Bearer
		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {

			//try to parse the token
			try {
				jwtToken = jwtTokenUtil.parseToken(requestTokenHeader.substring(7));

				//Verify token
				try {
					jwtTokenUtil.verifyToken(jwtToken);
					username = jwtTokenUtil.getUsernameFromToken(jwtToken);

					//Token validation
					if (SecurityContextHolder.getContext().getAuthentication() == null) {

						EmployeeEty employee;
						try {
							employee = employeeService.loadEmployeeByUsername(username);

							//Validate and set authentication
							try {
								jwtTokenUtil.validateToken(jwtToken, employee);

								UsernamePasswordAuthenticationToken usernamePasswordAuthToken = new UsernamePasswordAuthenticationToken(employee, null, null);
								usernamePasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
								SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthToken);

								//if token invalid
							} catch (BusinessException e) {
								response.sendError(401, e.getMessage());
							}
							//if user not found
						} catch (Exception e) {
							response.sendError(401, e.getMessage());
						}
					}
					//if token can't be verified or can't get username from token
				} catch (BusinessException e) {
					response.sendError(401, e.getMessage());
				}
				//if token can't be parser
			} catch (BusinessException e) {
				response.sendError(401, e.getMessage());
      }
		}
		//if token doesn't start with Bearer
		else
			response.sendError(401, "Bad token format");

		chain.doFilter(request, response);
	}


}
