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
package com.connexta.discovery.rest.cxf.client.mock;

import com.connexta.discovery.rest.cxf.client.DiscoveryApi;
import com.connexta.discovery.rest.models.ContactInfo;
import com.connexta.discovery.rest.models.ErrorMessage;
import com.connexta.discovery.rest.models.ResponseMessage;
import com.connexta.discovery.rest.models.SystemInfo;
import com.connexta.discovery.rest.springboot.mock.Application;
import com.connexta.spring.interceptor.VersionInterceptor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codice.junit.MethodRuleAnnotationRunner;
import org.codice.junit.TestDelimiter;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.embedded.tomcat.ConnectorStartFailedException;
import org.springframework.context.ConfigurableApplicationContext;

@TestDelimiter
@RunWith(MethodRuleAnnotationRunner.class)
public class TestClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestClient.class);
  private static final String CLIENT_VERSION = VersionInterceptor.getVersion(DiscoveryApi.class);
  private static final int MAJOR_VERSION;
  private static final int MINOR_VERSION;

  private static int port = -1;
  private static ConfigurableApplicationContext context = null;

  static {
    final String[] parts = TestClient.CLIENT_VERSION.split("\\.");

    MAJOR_VERSION = Integer.parseInt(parts[0]);
    MINOR_VERSION = Integer.parseInt(parts[1]);
  }

  @BeforeClass
  public static void setupClass() throws Exception {
    TestClient.port = 4000;
    while (true) {
      System.setProperty("server.port", String.valueOf(++TestClient.port));
      LOGGER.info("-----------------------------");
      LOGGER.info("Starting server on port: {}", TestClient.port);
      LOGGER.info("-----------------------------");
      try {
        TestClient.context = SpringApplication.run(Application.class, new String[0]);
        return;
      } catch (ConnectorStartFailedException e) { // continue with next port
      } catch (Exception e) {
        System.err.print("Server failed on port: " + TestClient.port + "; ");
        throw e;
      }
    }
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    TestClient.context.close();
  }

  private final String baseUrl;
  private final String heartbeatUrl;
  private final URI redirectUri;
  private final DiscoveryApi api;
  private final SystemInfo system =
      new SystemInfo()
          .id("my.id")
          .name("my system")
          .organization("my organization")
          .contact(new ContactInfo().email("myemail@myorg.com").name("my name"))
          .product("my product")
          .version("my version")
          .url("my url");

  public TestClient() {
    this.baseUrl = "http://localhost:" + TestClient.port + "/";
    LOGGER.info("----------------------------------");
    LOGGER.info("Server url: {}", baseUrl);
    LOGGER.info("----------------------------------");
    this.heartbeatUrl = baseUrl + "heartbeat";
    try {
      this.redirectUri = new URI(heartbeatUrl + "/ion-internal-id");
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
    final ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.findAndRegisterModules();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.api =
        JAXRSClientFactory.create(
            baseUrl,
            DiscoveryApi.class,
            Collections.singletonList(
                new JacksonJaxbJsonProvider(
                    objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS)),
            true);
  }

  @Test
  public void testSuccessfulHeartbeat() throws Exception {
    final ResponseMessage msg = api.heartbeat(TestClient.CLIENT_VERSION, system, false);

    Assert.assertThat(msg, Matchers.nullValue());
  }

  @Test
  public void testSuccessfulHeartbeatWithEcho() throws Exception {
    final ResponseMessage msg = api.heartbeat(TestClient.CLIENT_VERSION, system, true);

    Assert.assertThat(msg, Matchers.not(Matchers.nullValue()));
    assertEchoedParameters(msg);
  }

  @Test(expected = BadRequestException.class)
  public void testInvalidHeartbeat() throws Exception {
    try {
      system.setProduct("");
      api.heartbeat(TestClient.CLIENT_VERSION, system, false);
    } catch (BadRequestException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(400));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ErrorMessage msg = resp.readEntity(ErrorMessage.class);

      Assert.assertThat(msg.getStatus(), Matchers.equalTo(400));
      Assert.assertThat(msg.getCode(), Matchers.nullValue());
      Assert.assertThat(msg.getMessage(), Matchers.not(Matchers.isEmptyOrNullString()));
      Assert.assertThat(msg.getPath(), Matchers.equalTo("/heartbeat"));
      throw e;
    }
  }

  @Test(expected = ServerErrorException.class)
  public void testTooOldMajorClientVersionHeartbeat() throws Exception {
    try {
      api.heartbeat((TestClient.MAJOR_VERSION - 1) + "." + TestClient.MINOR_VERSION, system, false);
    } catch (ServerErrorException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(501));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ErrorMessage msg = resp.readEntity(ErrorMessage.class);

      Assert.assertThat(msg.getStatus(), Matchers.equalTo(501));
      Assert.assertThat(msg.getCode(), Matchers.equalTo(501002));
      Assert.assertThat(msg.getMessage(), Matchers.not(Matchers.isEmptyOrNullString()));
      Assert.assertThat(msg.getPath(), Matchers.equalTo("/heartbeat"));
      throw e;
    }
  }

  @Test(expected = ServerErrorException.class)
  public void testTooRecentMajorClientVersionHeartbeat() throws Exception {
    try {
      api.heartbeat((TestClient.MAJOR_VERSION + 1) + "." + TestClient.MINOR_VERSION, system, false);
    } catch (ServerErrorException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(501));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ErrorMessage msg = resp.readEntity(ErrorMessage.class);

      Assert.assertThat(msg.getStatus(), Matchers.equalTo(501));
      Assert.assertThat(msg.getCode(), Matchers.equalTo(501003));
      Assert.assertThat(msg.getMessage(), Matchers.not(Matchers.isEmptyOrNullString()));
      Assert.assertThat(msg.getPath(), Matchers.equalTo("/heartbeat"));
      throw e;
    }
  }

  @Test(expected = ServerErrorException.class)
  public void testTooRecentMinorClientVersionHeartbeat() throws Exception {
    try {
      api.heartbeat(TestClient.MAJOR_VERSION + "." + (TestClient.MINOR_VERSION + 1), system, false);
    } catch (ServerErrorException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(501));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ErrorMessage msg = resp.readEntity(ErrorMessage.class);

      Assert.assertThat(msg.getStatus(), Matchers.equalTo(501));
      Assert.assertThat(msg.getCode(), Matchers.equalTo(501004));
      Assert.assertThat(msg.getMessage(), Matchers.not(Matchers.isEmptyOrNullString()));
      Assert.assertThat(msg.getPath(), Matchers.equalTo("/heartbeat"));
      throw e;
    }
  }

  @Test(expected = ServerErrorException.class)
  public void testUnparseableClientVersionHeartbeat() throws Exception {
    try {
      api.heartbeat("a.23", system, false);
    } catch (ServerErrorException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(501));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ErrorMessage msg = resp.readEntity(ErrorMessage.class);

      Assert.assertThat(msg.getStatus(), Matchers.equalTo(501));
      Assert.assertThat(msg.getCode(), Matchers.equalTo(501001));
      Assert.assertThat(msg.getMessage(), Matchers.not(Matchers.isEmptyOrNullString()));
      Assert.assertThat(msg.getPath(), Matchers.equalTo("/heartbeat"));
      throw e;
    }
  }

  @Test(expected = RedirectionException.class)
  public void testTemporaryRedirectedHeartbeat() throws Exception {
    try {
      system.setProduct("ion-307");
      api.heartbeat(TestClient.CLIENT_VERSION, system, false);
    } catch (RedirectionException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(307));
      Assert.assertThat(resp.getLocation(), Matchers.equalTo(redirectUri));
      Assert.assertThat(resp.getMediaType(), Matchers.nullValue());
      throw e;
    }
  }

  @Test(expected = RedirectionException.class)
  public void testTemporaryRedirectedHeartbeatWithEcho() throws Exception {
    try {
      system.setProduct("ion-307");
      api.heartbeat(TestClient.CLIENT_VERSION, system, true);
    } catch (RedirectionException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(307));
      Assert.assertThat(resp.getLocation(), Matchers.equalTo(redirectUri));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ResponseMessage msg = resp.readEntity(ResponseMessage.class);

      Assert.assertThat(msg, Matchers.not(Matchers.nullValue()));
      assertEchoedParameters(msg);
      throw e;
    }
  }

  @Test(expected = RedirectionException.class)
  public void testPermanentRedirectedHeartbeat() throws Exception {
    try {
      system.setProduct("ion-308");
      api.heartbeat(TestClient.CLIENT_VERSION, system, false);
    } catch (RedirectionException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(308));
      Assert.assertThat(resp.getLocation(), Matchers.equalTo(redirectUri));
      Assert.assertThat(resp.getMediaType(), Matchers.nullValue());
      throw e;
    }
  }

  @Test(expected = RedirectionException.class)
  public void testPermanentRedirectedHeartbeatWithEcho() throws Exception {
    try {
      system.setProduct("ion-308");
      api.heartbeat(TestClient.CLIENT_VERSION, system, true);
    } catch (RedirectionException e) {
      final Response resp = e.getResponse();

      Assert.assertThat(resp.getStatus(), Matchers.equalTo(308));
      Assert.assertThat(resp.getLocation(), Matchers.equalTo(redirectUri));
      Assert.assertTrue(resp.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
      final ResponseMessage msg = resp.readEntity(ResponseMessage.class);

      Assert.assertThat(msg, Matchers.not(Matchers.nullValue()));
      assertEchoedParameters(msg);
      throw e;
    }
  }

  private void assertEchoedParameters(ResponseMessage resp) {
    Assert.assertThat(resp.getEchoedParameters().getId(), Matchers.equalTo(system.getId()));
    Assert.assertThat(resp.getEchoedParameters().getName(), Matchers.equalTo(system.getName()));
    Assert.assertThat(
        resp.getEchoedParameters().getOrganization(), Matchers.equalTo(system.getOrganization()));
    Assert.assertThat(
        resp.getEchoedParameters().getContact().getEmail(),
        Matchers.equalTo(system.getContact().getEmail()));
    Assert.assertThat(
        resp.getEchoedParameters().getContact().getName(),
        Matchers.equalTo(system.getContact().getName()));
    Assert.assertThat(
        resp.getEchoedParameters().getProduct(), Matchers.equalTo(system.getProduct()));
    Assert.assertThat(
        resp.getEchoedParameters().getVersion(), Matchers.equalTo(system.getVersion()));
    Assert.assertThat(resp.getEchoedParameters().getUrl(), Matchers.equalTo(system.getUrl()));
  }

  private static ServerSocket findServerSocket(int portToTry) {
    try {
      final ServerSocket socket = new ServerSocket(portToTry);

      socket.setReuseAddress(true);
      return socket;
    } catch (Exception e) {
      return TestClient.findServerSocket(portToTry + 1);
    }
  }
}
