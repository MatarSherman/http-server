package dev.matar.httpserver.server;

import static dev.matar.httpserver.server.HttpRequestDeserializer.*;
import static org.junit.jupiter.api.Assertions.*;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.HttpRequestSizeLimitException;
import dev.matar.httpserver.exception.InvalidHttpRequestException;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;
import dev.matar.httpserver.model.http.HttpRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HttpRequestDeserializerTest {
  private final String[] VALID_REQ_FIRST_LINE = new String[] {"POST", "/test", "HTTP/1.1"};
  private final HttpHeader[] VALID_HEADERS =
      new HttpHeader[] {new HttpHeader("Host", "localhost"), new HttpHeader("Accept", "*/*")};

  private static InputStream getReqInputStream(String requestString) {
    return new ByteArrayInputStream(requestString.getBytes(Constants.DEFAULT_CHARSET));
  }

  @Test
  void shouldParseValidReqNoBody()
      throws IOException, InvalidHttpRequestException, HttpRequestSizeLimitException {
    String requestString =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + VALID_HEADERS[0].toRaw()
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF;

    InputStream inputStream = getReqInputStream(requestString);
    Optional<HttpRequest> optionalReq = deserialize(inputStream);

    assertTrue(optionalReq.isPresent());
    HttpRequest request = optionalReq.get();
    assertAll(
        () ->
            assertEquals(
                VALID_REQ_FIRST_LINE[0],
                request.getMethod().name(),
                "Should parse valid request method"),
        () ->
            assertEquals(
                VALID_REQ_FIRST_LINE[1], request.getPath(), "Should parse valid request path"),
        () ->
            assertEquals(
                VALID_REQ_FIRST_LINE[2],
                request.getVersion(),
                "Should parse valid request version"),
        () ->
            assertEquals(
                VALID_HEADERS[0].value(),
                request.getHeaders().getFirst(VALID_HEADERS[0].key()).orElse(null),
                "Should parse valid header"),
        () -> assertNull(request.getBody(), "Body should be null"));
  }

  @Test
  void shouldParseValidContinuousBody()
      throws IOException, InvalidHttpRequestException, HttpRequestSizeLimitException {
    String bodyString = "Hello World!";
    byte[] bodyBytes = bodyString.getBytes(Constants.DEFAULT_CHARSET);
    HttpHeader contentLength = HttpHeader.contentLength(bodyBytes.length);

    String requestString =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + contentLength.toRaw()
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF
            + bodyString;

    InputStream inputStream = getReqInputStream(requestString);
    Optional<HttpRequest> optionalReq = deserialize(inputStream);

    assertTrue(optionalReq.isPresent());
    HttpRequest request = optionalReq.get();
    assertAll(
        () ->
            assertEquals(
                contentLength.value(),
                request.getHeaders().getFirst(contentLength.key()).orElse(null),
                "Should parse valid Content-Length header"),
        () ->
            assertArrayEquals(bodyBytes, request.getBody(), "Should parse valid continuous body"));
  }

  @Test
  void shouldParseValidChunkedBody()
      throws IOException, InvalidHttpRequestException, HttpRequestSizeLimitException {
    String[] bodyWords = new String[] {"Matar", "Sherman"};
    byte[][] bodyBytes = new byte[][] {bodyWords[0].getBytes(), bodyWords[1].getBytes()};

    HttpHeader trailingHeader = new HttpHeader("Server-Timing", "custom-metric");
    String req =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + "Transfer-Encoding: chunked\r\n"
            + new HttpHeader("Trailer", "Server-Timing").toRaw()
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF
            + bodyBytes[0].length
            + Constants.HTTP_CRLF
            + bodyWords[0]
            + Constants.HTTP_CRLF
            + bodyBytes[1].length
            + Constants.HTTP_CRLF
            + bodyWords[1]
            + Constants.HTTP_CRLF
            + "0\r\n"
            + trailingHeader.toRaw()
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF;

    InputStream inputStream = getReqInputStream(req);
    Optional<HttpRequest> optionalReq = deserialize(inputStream);
    assertTrue(optionalReq.isPresent());
    HttpRequest request = optionalReq.get();

    assertArrayEquals(
        String.join("", bodyWords).getBytes(), request.getBody(), "Should parse chunked body");
    assertEquals(
        trailingHeader.value(), request.getHeaders().getFirst(trailingHeader.key()).orElse(null));
  }

  @Test
  void shouldHandleEmptyRequest()
      throws IOException, InvalidHttpRequestException, HttpRequestSizeLimitException {
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);

    Optional<HttpRequest> request = deserialize(inputStream);

    assertTrue(request.isEmpty());
  }

  @Test
  void shouldPropagateIOExceptions() {
    InputStream failingStream =
        new InputStream() {
          @Override
          public int read() throws IOException {
            throw new IOException("Simulated I/O failure");
          }
        };

    assertThrows(
        IOException.class,
        () -> deserialize(failingStream),
        "Should propagate IO Exceptions");
  }

  @Test
  void shouldThrowOnInvalidFirstLine() {
    String tooShort = "a";
    String tooLong = "a b c d";
    String invalidMethod = "GERT / HTTP/1.1";

    assertAll(
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(tooShort)),
                "Should throw if first line too short"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(tooLong)),
                "Should throw if first line too long"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(invalidMethod)),
                "Should throw if method is invalidZ"));
  }

  @Test
  void shouldThrowOnInvalidHeaders() {
    String missingEndOfHeaders =
        String.join(" ", VALID_REQ_FIRST_LINE) + Constants.HTTP_CRLF + VALID_HEADERS[0].toRaw();

    String invalidHeaderFormat =
        String.join(" ", VALID_REQ_FIRST_LINE) + Constants.HTTP_CRLF + "invalid-header";

    assertAll(
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(missingEndOfHeaders)),
                "Should throw when end of headers is missing"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(invalidHeaderFormat)),
                "Should throw when a header has an invalid format"));
  }

  @Test
  void shouldThrowOnInvalidBodySize() {
    String BodyString = "Hello World!";
    HttpHeader contentLength = HttpHeader.contentLength(999);

    String contentLengthBiggerThanBody =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + contentLength.toRaw()
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF
            + BodyString;

    String contentLengthNotInteger =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + new HttpHeader("Content-Length", "non-numerical-value").toRaw();

    assertAll(
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(contentLengthBiggerThanBody)),
                "Should throw when Content-Length is bigger than the size of the body"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(contentLengthNotInteger)),
                "Should throw when Content-Length is not an integer"));
  }

  @Test
  void shouldThrowOnInvalidChunkedBodyFormat() {
    String bodyString = "Matar Sherman";
    // Notice, whitespace will be missing from the actual body
    String[] bodyWords = bodyString.split(" ");
    byte[][] bodyBytes = new byte[][] {bodyWords[0].getBytes(), bodyWords[1].getBytes()};

    String reqStart =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + "Transfer-Encoding: chunked\r\n";

    String unexpectedEOF =
        reqStart
            + Constants.HTTP_CRLF
            + bodyBytes[0].length
            + Constants.HTTP_CRLF
            + bodyWords[0]
            + Constants.HTTP_CRLF;

    String unexpectedEndOfBody = unexpectedEOF + "\r\n";

    String illegalChunkSize =
        reqStart
            + Constants.HTTP_CRLF
            + "non-hexadecimal"
            + Constants.HTTP_CRLF
            + "0\r\n"
            + Constants.HTTP_CRLF;

    String chunkSizeTooBig =
        reqStart
            + Constants.HTTP_CRLF
            + bodyBytes[0].length * 2
            + Constants.HTTP_CRLF
            + bodyWords[0]
            + Constants.HTTP_CRLF;

    String chunkSizeTooSmall =
        reqStart
            + Constants.HTTP_CRLF
            + bodyBytes[0].length / 2
            + Constants.HTTP_CRLF
            + bodyWords[0]
            + Constants.HTTP_CRLF;

    assertAll(
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(unexpectedEOF)),
                "Should throw on unexpected end of stream"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(unexpectedEndOfBody)),
                "Should throw on unexpected end of body"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(illegalChunkSize)),
                "should throw on illegal (non hexadecimal) chunk size"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(chunkSizeTooBig)),
                "Should throw when chunk size is bigger than actual chunk"),
        () ->
            assertThrows(
                InvalidHttpRequestException.class,
                () -> deserialize(getReqInputStream(chunkSizeTooSmall)),
                "Should throw when chunk size is smaller than actual chunk"));
  }

  @Test
  void shouldThrowOnRequestTooLarge() {
    String firstLineTooLarge = "A".repeat(MAX_FIRST_LINE_LENGTH + 10);

    String headersSectionTooManyBytes =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + firstLineTooLarge
            + Constants.HTTP_CRLF;

    String amountOfHeadersTooLarge =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + "Host: a\r\n".repeat(HttpRequestDeserializer.MAX_HEADERS_AMOUNT + 10);

    String contentLengthTooLarge =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + HttpHeader.contentLength(MAX_BODY_SIZE + 10).toRaw()
            + Constants.HTTP_CRLF;

    int firstLineBytesAmount = firstLineTooLarge.getBytes().length;
    String chunkedBodyTooLarge =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + new HttpHeader(HttpHeaderKey.TRANSFER_ENCODING.value(), "chunked").toRaw()
            + Constants.HTTP_CRLF
            + firstLineTooLarge.repeat(1000);

    String chunkSizeTooLarge =
        String.join(" ", VALID_REQ_FIRST_LINE)
            + Constants.HTTP_CRLF
            + new HttpHeader(HttpHeaderKey.TRANSFER_ENCODING.value(), "chunked").toRaw()
            + Constants.HTTP_CRLF
            + Constants.HTTP_CRLF
            + Integer.toHexString(firstLineBytesAmount * 200)
            + Constants.HTTP_CRLF;

    assertAll(
        () ->
            assertThrows(
                HttpRequestSizeLimitException.class,
                () -> deserialize(getReqInputStream(firstLineTooLarge)),
                "Should throw first line is too large"),
        () ->
            assertThrows(
                HttpRequestSizeLimitException.class,
                () -> deserialize(getReqInputStream(headersSectionTooManyBytes)),
                "Should throw when headers section has too many bytes"),
        () ->
            assertThrows(
                HttpRequestSizeLimitException.class,
                () -> deserialize(getReqInputStream(amountOfHeadersTooLarge)),
                "Should throw when headers section has too many headers"),
        () ->
            assertThrows(
                HttpRequestSizeLimitException.class,
                () -> deserialize(getReqInputStream(contentLengthTooLarge)),
                "Should throw when Content-Length headers specifies body is too large"),
        () ->
            assertThrows(
                HttpRequestSizeLimitException.class,
                () -> deserialize(getReqInputStream(chunkedBodyTooLarge)),
                "Should throw when Chunked body is too large"),
        () ->
            assertThrows(
                HttpRequestSizeLimitException.class,
                () -> deserialize(getReqInputStream(chunkSizeTooLarge)),
                "Should throw when chunk size specification exceeds body capacity"));
  }
}
