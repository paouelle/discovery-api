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
package com.connexta.spring.interceptor;

import com.connexta.spring.error.DetailedResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

/**
 * Exception thrown by the {@link VersionInterceptor} when the server is unable to accept a message
 * from a client because the client is using an older version of the API.
 */
public class UnsupportedVersionException extends DetailedResponseStatusException {
  private final String clientVersion;
  private final String serverVersion;

  /**
   * Creates a new exception.
   *
   * @param code a specific code for the error
   * @param clientVersion the version advertised by the client
   * @param serverVersion the version of the API supported by the server
   * @param cause the cause for this exception
   */
  public UnsupportedVersionException(
      int code, String clientVersion, String serverVersion, @Nullable Throwable cause) {
    super(HttpStatus.NOT_IMPLEMENTED, code, "Unsupported version: " + clientVersion, cause);
    this.clientVersion = clientVersion;
    this.serverVersion = serverVersion;
    addDetail("client version: " + clientVersion);
    addDetail("server version: " + serverVersion);
  }

  /**
   * Creates a new exception.
   *
   * @param code a specific code for the error
   * @param clientVersion the version advertised by the client
   * @param serverVersion the version of the API supported by the server
   */
  public UnsupportedVersionException(int code, String clientVersion, String serverVersion) {
    this(code, clientVersion, serverVersion, null);
  }

  public String getClientVersion() {
    return clientVersion;
  }

  public String getServerVersion() {
    return serverVersion;
  }
}
