/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.spring.error;

import com.connexta.spring.annotation.DetailedResponseStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.WebRequest;

/**
 * Enhanced version of Spring's {@link DefaultErrorAttributes} capable of adding details information
 * from known exceptions.
 */
@Component
public class DetailedErrorAttributes extends DefaultErrorAttributes {
  @Override
  public Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
    final Map<String, Object> errorAttributes =
        super.getErrorAttributes(request, includeStackTrace);
    final Throwable error = getError(request);
    final int code = determineCode(error);
    final List<String> details = determineDetails(error);

    if (code != -1) {
      errorAttributes.put("code", code);
    }
    if (!CollectionUtils.isEmpty(details)) {
      errorAttributes.put("details", details);
    }
    return errorAttributes;
  }

  private int determineCode(Throwable error) {
    if (error instanceof Detailable) {
      return ((Detailable) error).getCode();
    }
    final DetailedResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(error.getClass(), DetailedResponseStatus.class);

    if (responseStatus != null) {
      return responseStatus.code();
    }
    return -1;
  }

  private List<String> determineDetails(Throwable error) {
    if (error instanceof Detailable) {
      return ((Detailable) error).getDetails();
    }
    final DetailedResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(error.getClass(), DetailedResponseStatus.class);

    if (responseStatus != null) {
      return Arrays.asList(responseStatus.details());
    }
    // for now, simply provide the exception message of the exception and its causes
    // re-think what we want to do here
    final List<String> details = new ArrayList<>();

    while (error != null) {
      details.add(error.getMessage());
      error = error.getCause();
    }
    return details;
  }
}
