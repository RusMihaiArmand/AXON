package ro.axon.dot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import ro.axon.dot.exceptions.BusinessException;
import ro.axon.dot.exceptions.ErrorDetail;

@RequiredArgsConstructor
public class FilterExceptionHandler extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (BusinessException exception) {

      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorCode(exception.getError().getErrorDescription().getErrorCode());
      errorDetail.setMessage(exception.getError().getErrorDescription().getDevMsg());
      errorDetail.setContextVariables(exception.getError().getContextVariables());

      response.setStatus(exception.getError().getErrorDescription().getStatus().value());
      response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetail));
    }
  }
}
