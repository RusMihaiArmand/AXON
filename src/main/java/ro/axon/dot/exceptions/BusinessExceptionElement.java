package ro.axon.dot.exceptions;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessExceptionElement {

  private final BusinessErrorCode errorDescription;
  private final Map<String, Object> contextVariables;
}
