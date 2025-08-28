package dev.matar.httpserver.server;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.HttpSerializationException;
import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.model.server.serializer.body.BytesResource;
import dev.matar.httpserver.model.server.serializer.body.FileResource;
import dev.matar.httpserver.model.server.serializer.body.StreamResource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class HttpResponseSerializerTest {
  private HttpResponse<Object> VALID_RESPONSE;
  private final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();

  @BeforeEach
  void buildValidResponse() {
    VALID_RESPONSE = new HttpResponse<>(200, "OK");
    VALID_RESPONSE.getHeaders().set("Host", "localhost");
    VALID_RESPONSE.getHeaders().set("Accept", "*/*");
  }

  @BeforeEach
  void resetOutputStream() {
    OUTPUT_STREAM.reset();
  }

  @Test
  void shouldParseValidResponseNoBody() throws IOException, HttpSerializationException {
    HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);

    ParsedHttpResponse expected = ParsedHttpResponse.from(VALID_RESPONSE, null);
    ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());

    assertEquals(expected, actual);
  }

  static Stream<Object> primitiveBodyProvider() {
    return Stream.of(123, false, 'a');
  }

  @ParameterizedTest
  @MethodSource("primitiveBodyProvider")
  void shouldParsePrimitiveBody(Object body) throws IOException, HttpSerializationException {
    VALID_RESPONSE.setBody(body);

    HttpResponse<?> response = new HttpResponse<>(VALID_RESPONSE);
    HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);

    final byte[] bodyBytes = String.valueOf(body).getBytes(Constants.DEFAULT_CHARSET);
    response.getHeaders().set(HttpHeaderKey.CONTENT_LENGTH.value(), "" + bodyBytes.length);
    response
        .getHeaders()
        .set(
            HttpHeaderKey.CONTENT_TYPE.value(),
            MimeType.TEXT_PLAIN.value()
                + HttpHeader.SUB_PARAM_SEPARATOR
                + HttpHeader.CHARSET_PARAM_INDICATOR
                + Constants.DEFAULT_CHARSET);

    ParsedHttpResponse expected = ParsedHttpResponse.from(response, "" + body);
    ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());
    assertEquals(expected, actual);
  }

  @Test
  void shouldParseRequestWithStringBody() throws IOException, HttpSerializationException {
    VALID_RESPONSE
        .getHeaders()
        .set(
            HttpHeaderKey.CONTENT_TYPE.value(),
            MimeType.TEXT_CSS.value()
                + HttpHeader.SUB_PARAM_SEPARATOR
                + HttpHeader.CHARSET_PARAM_INDICATOR
                + Constants.DEFAULT_CHARSET);

    String body = "body {color: red;}";
    VALID_RESPONSE.setBody(body);

    HttpResponse<?> response = new HttpResponse<>(VALID_RESPONSE);
    HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);

    byte[] bodyBytes = body.getBytes(Constants.DEFAULT_CHARSET);
    response.getHeaders().set(HttpHeaderKey.CONTENT_LENGTH.value(), "" + bodyBytes.length);

    ParsedHttpResponse expected = ParsedHttpResponse.from(response, body);
    ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());
    assertEquals(expected, actual);
  }

  @Test
  void shouldParseRequestWithJSONBody() throws IOException, HttpSerializationException {
    record Person(String name, String lastName) {}
    Person body = new Person("Matar", "Sherman");
    VALID_RESPONSE.setBody(body);

    HttpResponse<?> response = new HttpResponse<>(VALID_RESPONSE);
    HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);
    response
        .getHeaders()
        .set(
            HttpHeaderKey.CONTENT_TYPE.value(),
            MimeType.JSON.value()
                + HttpHeader.SUB_PARAM_SEPARATOR
                + HttpHeader.CHARSET_PARAM_INDICATOR
                + Constants.DEFAULT_CHARSET.name());

    ObjectMapper mapper = new ObjectMapper();
    String bodyString = mapper.writeValueAsString(body);
    response
        .getHeaders()
        .set(
            HttpHeaderKey.CONTENT_LENGTH.value(),
            bodyString.getBytes(Constants.DEFAULT_CHARSET).length + "");

    ParsedHttpResponse expected = ParsedHttpResponse.from(response, bodyString);
    ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());

    assertEquals(expected, actual);
  }

  @Test
  void shouldParseRequestWithBytesBody() throws IOException, HttpSerializationException {
    byte[] body = "Hello World!".getBytes(Constants.DEFAULT_CHARSET);

    VALID_RESPONSE.setBody(new BytesResource(body));
    HttpResponse<?> response = new HttpResponse<>(VALID_RESPONSE);
    HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);

    response.getHeaders().set(HttpHeaderKey.CONTENT_TYPE.value(), MimeType.BINARY.value());
    response.getHeaders().set(HttpHeaderKey.CONTENT_LENGTH.value(), "" + body.length);

    ParsedHttpResponse expected =
        ParsedHttpResponse.from(response, new String(body, Constants.DEFAULT_CHARSET));
    ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());
    assertEquals(expected, actual);
  }

  @Test
  void shouldParseRequestWithStreamBody() throws IOException, HttpSerializationException {
    String body = "Hello World!";
    byte[] bodyBytes = body.getBytes(Constants.DEFAULT_CHARSET);
    InputStream inputStream = new ByteArrayInputStream(bodyBytes);
    StreamResource resourceBody = new StreamResource(inputStream);

    VALID_RESPONSE.setBody(resourceBody);

    HttpResponse<?> response = new HttpResponse<>(VALID_RESPONSE);
    HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);
    response.getHeaders().set(HttpHeaderKey.CONTENT_TYPE.value(), MimeType.BINARY.value());
    response
        .getHeaders()
        .set(HttpHeaderKey.TRANSFER_ENCODING.value(), HttpHeaderValue.TRANSFER_ENC_CHUNKED.value());

    String chunkedBody =
        Integer.toHexString(bodyBytes.length)
            + Constants.HTTP_CRLF
            + body
            + Constants.HTTP_CRLF
            + 0
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF;

    ParsedHttpResponse expected = ParsedHttpResponse.from(response, chunkedBody);
    ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());

    assertEquals(expected, actual);
  }

  @Test
  void shouldParseRequestWithFileBody() throws IOException, HttpSerializationException {
    String fileString = "Hello World!";
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile(null, "file-body-test.txt");
      byte[] fileBytes = fileString.getBytes(Constants.DEFAULT_CHARSET);
      Files.write(tempFile, fileBytes);

      int expectedContentLength = fileBytes.length;

      VALID_RESPONSE.setBody(new FileResource(tempFile));
      HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM);

      HttpResponse<?> response = new HttpResponse<>(VALID_RESPONSE);
      response.getHeaders().set(HttpHeaderKey.CONTENT_TYPE.value(), "text/plain");
      response.getHeaders().set(HttpHeaderKey.CONTENT_LENGTH.value(), expectedContentLength + "");

      ParsedHttpResponse expected = ParsedHttpResponse.from(response, fileString);
      System.out.println(Arrays.toString(OUTPUT_STREAM.toByteArray()));
      ParsedHttpResponse actual = ParsedHttpResponse.parse(OUTPUT_STREAM.toByteArray());

      assertEquals(expected, actual, "Should parse response with file body");
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Test
  void shouldPropagateIOExceptions() throws IOException {
    try (OutputStream failingStream =
        new OutputStream() {
          @Override
          public void write(int b) throws IOException {
            throw new IOException();
          }
        }) {
      assertThrows(
          IOException.class, () -> HttpResponseSerializer.serialize(VALID_RESPONSE, failingStream));
    }
  }

  /**
   * Serialization works for all bodies that can be serialized by one of the built-in serializers,
   * including the fallback (JSON) serializer. Or one of the custom body serializers added by the
   * end user.
   */
  @Test
  void shouldThrowOnUnsupportedBodyType() {
    Object unsupportedBody =
        new Object() {
          private int a;
        };
    VALID_RESPONSE.setBody(unsupportedBody);
    assertThrows(
        HttpSerializationException.class,
        () -> HttpResponseSerializer.serialize(VALID_RESPONSE, OUTPUT_STREAM));
  }
}
