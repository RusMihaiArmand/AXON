package ro.axon.dot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class BusinessExceptionElement {
    private final BusinessErrorCode errorDescription;
    private final Map<String, Object> contextVariables;
}
