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
package com.connexta.discovery.rest.springboot.mock.controller;

import com.connexta.discovery.rest.models.ContactInfo;
import com.connexta.discovery.rest.models.ResponseMessage;
import com.connexta.discovery.rest.models.SystemInfo;
import com.connexta.discovery.rest.spring.DiscoveryApi;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

/** Dummy controller implementation to provide a simple test stub for the discovery API. */
@RestController
@CrossOrigin(origins = "*")
public class DiscoveryController implements DiscoveryApi {
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryController.class);

  @Resource private NativeWebRequest request;

  @Override
  public Optional<NativeWebRequest> getRequest() {
    return Optional.of(request);
  }

  @Override
  public ResponseEntity<ResponseMessage> heartbeat(
      String acceptVersion, @Valid SystemInfo system, Optional<Boolean> echo) {
    LOGGER.info("Context Path: {}", request.getContextPath());
    for (final Iterator<String> i = request.getHeaderNames(); i.hasNext(); ) {
      final String name = i.next();

      LOGGER.info("{}: {}", name, request.getHeader(name));
    }
    LOGGER.info("Accept-Version: {}", acceptVersion);
    LOGGER.info("Echo: {}", echo);
    LOGGER.info("Heartbeat: {}", system);
    SystemInfo echoedSystem = null;
    final HttpHeaders headers = new HttpHeaders();
    HttpStatus status = HttpStatus.NO_CONTENT;

    if (echo.orElse(false)) {
      status = HttpStatus.OK;
      echoedSystem =
          new SystemInfo()
              .id(system.getId())
              .name(system.getName())
              .organization(system.getOrganization())
              .contact(
                  new ContactInfo()
                      .email(system.getContact().getEmail())
                      .name(system.getContact().getName()))
              .product(system.getProduct())
              .version(system.getVersion())
              .url(system.getUrl());
    }
    if (system.getProduct().equals("ion-308")) {
      status = HttpStatus.PERMANENT_REDIRECT;
    } else if (system.getProduct().equals("ion-307")) {
      status = HttpStatus.TEMPORARY_REDIRECT;
    }
    if ((status == HttpStatus.PERMANENT_REDIRECT) || (status == HttpStatus.TEMPORARY_REDIRECT)) {
      try {
        headers.setLocation(
            new URI(
                request.getNativeRequest(HttpServletRequest.class).getRequestURL().toString()
                    + "/ion-internal-id"));
        if (echoedSystem != null) {
          return new ResponseEntity<>(
              new ResponseMessage().echoedParameters(echoedSystem), headers, status);
        }
        return new ResponseEntity<>(headers, status);
      } catch (URISyntaxException e) {
      }
    }
    if (echoedSystem != null) {
      return new ResponseEntity<>(
          new ResponseMessage().echoedParameters(echoedSystem), headers, status);
    }
    return new ResponseEntity<>(headers, status);
  }
}
