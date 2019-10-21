package com.connexta.discovery.rest.springboot.mock.controller;

import com.connexta.discovery.rest.models.ErrorMessage;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiscoveryErrorController extends BasicErrorController {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryErrorController.class);

  public DiscoveryErrorController(ErrorAttributes errorAttributes) {
    super(errorAttributes, new ErrorProperties());
    LOGGER.info("in here: {}", errorAttributes);
  }

  @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErrorMessage> handleError(HttpServletRequest request) {
    LOGGER.error("ERROR ERROR ERROR ERROR ERROR ERROR");
    final Map<String, Object> body =
        getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
    final HttpStatus status = getStatus(request);
    final ErrorMessage msg =
        new ErrorMessage().message("ERROR BOB ERROR").path(request.getServletPath());

    body.forEach((n, v) -> LOGGER.error("body({}): {}", n, Objects.toString(v)));
    request
        .getParameterMap()
        .forEach((n, v) -> LOGGER.error("parameter({}): {}", n, Arrays.toString(v)));
    for (final Enumeration<String> e = request.getAttributeNames(); e.hasMoreElements(); ) {
      final String name = e.nextElement();
      final Object obj = request.getAttribute(name);

      LOGGER.error(
          "parameter({}): [{}] <{}>",
          name,
          ((obj != null) ? obj.getClass() : "null"),
          Objects.toString(obj));
    }
    return new ResponseEntity<>(msg, HttpStatus.CONFLICT);
  }
}
