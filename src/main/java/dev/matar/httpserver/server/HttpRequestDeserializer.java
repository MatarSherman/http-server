package dev.matar.httpserver.server;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.HttpDeserializationException;
import dev.matar.httpserver.exception.HttpRequestSizeLimitException;
import dev.matar.httpserver.exception.ReadLimitException;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;
import dev.matar.httpserver.model.http.HttpMethod;
import dev.matar.httpserver.model.http.HttpRequest;
import dev.matar.httpserver.util.HttpUtils;
import java.io.*;
import java.util.*;

public class HttpRequestDeserializer {
  public static final int MAX_FIRST_LINE_LENGTH = 8192;
  public static final int MAX_BYTES_HEADERS = 8192;
  public static final int MAX_HEADERS_AMOUNT = 100;
  public static final int MAX_BODY_SIZE = 1_048_576;

  public static Optional<HttpRequest> deserialize(HttpStreamReader httpStreamReader)
      throws IOException, HttpDeserializationException, HttpRequestSizeLimitException {
    HttpRequest.Builder builder = HttpRequest.builder();
    RequestMetadata metadata = new RequestMetadata(-1, false, false);

    try {
      String firstLine = httpStreamReader.readLine(MAX_FIRST_LINE_LENGTH);
      if (firstLine == null) {
        return Optional.empty();
      }
      deserializeFirstLine(firstLine, builder);
      deserializeContinuousHeaders(httpStreamReader, builder, metadata, MAX_BYTES_HEADERS, false);

      if (metadata.isChunkedBody) {
        byte[] body = deserializeBodyChunked(httpStreamReader);
        builder.body(body);
        // This ensures we read the last line while also checking for trailing headers
        deserializeContinuousHeaders(
            httpStreamReader, builder, metadata, MAX_BODY_SIZE - body.length, true);
      } else if (metadata.contentLength > 0) {
        builder.body(deserializeBodyContinuous(httpStreamReader, metadata.contentLength));
      }

      return Optional.of(builder.build());
    } catch (IOException e) {
      throw new IOException("ERROR: could not read HTTP request.", e);
    } catch (ReadLimitException e) {
      throw new HttpRequestSizeLimitException(
          "ERROR: first line exceeds length capacity",
          HttpRequestSizeLimitException.Section.FIRST_LINE);
    }
  }

  private static void deserializeFirstLine(String firstLine, HttpRequest.Builder builder)
      throws HttpDeserializationException {
    HTTPReqFirstLine requestDefinition = new HTTPReqFirstLine(firstLine);

    try {
      HttpMethod method = HttpMethod.valueOf(requestDefinition.method);
      builder.method(method);
    } catch (IllegalArgumentException e) {
      throw new HttpDeserializationException("ERROR: illegal HTTP request method", e);
    }

    builder.path(requestDefinition.path);
    builder.version(requestDefinition.version);
  }

  private static void deserializeContinuousHeaders(
      HttpStreamReader reader,
      HttpRequest.Builder requestBuilder,
      RequestMetadata metadata,
      int maxBytes,
      boolean isTrailing)
      throws IOException, HttpDeserializationException, HttpRequestSizeLimitException {
    try {
      Optional<HttpHeader> optionalHeader;
      int bytesReadCount = 0;
      int headersCount = 0;

      ByteArrayOutputStream line = reader.readLineRaw(maxBytes);
      while ((optionalHeader = deserializeHeaderLine(line)).isPresent()) {
        bytesReadCount += line.size();
        HttpHeader header = optionalHeader.get();
        if (isTrailing) {
          requestBuilder.addTrailingHeader(header.key(), header.value());
        } else {
          requestBuilder.addHeader(header.key(), header.value());
        }
        headersCount++;
        if (headersCount > MAX_HEADERS_AMOUNT) {
          throw new ReadLimitException("ERROR: headers count exceeds capacity");
        }
        saveRequestMetadata(metadata, header);
        line = reader.readLineRaw(maxBytes - bytesReadCount);
      }
    } catch (ReadLimitException e) {
      throw new HttpRequestSizeLimitException(
          "ERROR: request exceeds headers capacity", HttpRequestSizeLimitException.Section.HEADERS);
    }
  }

  private static Optional<HttpHeader> deserializeHeaderLine(ByteArrayOutputStream bytes)
      throws HttpDeserializationException {
    if (bytes == null) {
      throw new HttpDeserializationException("ERROR: unexpected end of stream in request headers");
    }
    if (bytes.size() == 0) {
      return Optional.empty();
    }
    final String HEADER_SPLIT_REGEX = ":\\s*";
    String[] splitHeader = bytes.toString(Constants.DEFAULT_CHARSET).split(HEADER_SPLIT_REGEX, 2);

    if (splitHeader.length != 2) {
      throw new HttpDeserializationException("ERROR: illegal header format: " + bytes);
    }
    return Optional.of(new HttpHeader(splitHeader[0], splitHeader[1]));
  }

  private static void saveRequestMetadata(RequestMetadata metadata, HttpHeader header)
      throws HttpDeserializationException, HttpRequestSizeLimitException {
    try {
      if (HttpUtils.isContentLengthHeader(header)) {
        int newContentLength = Integer.parseInt(header.value());
        if (newContentLength > MAX_BODY_SIZE) {
          throw new HttpRequestSizeLimitException(
              "ERROR: Content-Length exceeds max capacity of body",
              HttpRequestSizeLimitException.Section.BODY);
        }
        metadata.contentLength = newContentLength;
      } else if (HttpUtils.isTransferEncodingChunkedHeader(header)) {
        metadata.isChunkedBody = true;
      } else if (header.key().equalsIgnoreCase(HttpHeaderKey.TRAILER.value())) {
        metadata.hasTrailingHeaders = true;
      }
    } catch (NumberFormatException e) {
      throw new HttpDeserializationException(
          "ERROR: illegal Content-Length header value, received: " + header.value(), e);
    }
  }

  private static byte[] deserializeBodyContinuous(HttpStreamReader input, int bodyLength)
      throws IOException, HttpDeserializationException {
    byte[] body = new byte[bodyLength];
    int bytesRead = input.readBytes(body);

    if (bytesRead < bodyLength) {
      throw new HttpDeserializationException(
          "ERROR: request body is smaller than Content-Length specification, expected: "
              + bodyLength
              + " received: "
              + bytesRead);
    }
    return body;
  }

  private static byte[] deserializeBodyChunked(HttpStreamReader reader)
      throws IOException, HttpDeserializationException, HttpRequestSizeLimitException {
    final ByteArrayOutputStream bodyAccumulator = new ByteArrayOutputStream();
    ByteArrayOutputStream currLine = null;
    int chunkLength;
    int totalBytesReadCount = 0;

    try {
      do {
        currLine = reader.readLineRaw(MAX_BODY_SIZE - totalBytesReadCount);
        if (currLine == null || currLine.size() == 0) {
          throw new HttpDeserializationException("ERROR: malformed chunked body of HTTP request");
        }
        totalBytesReadCount += currLine.size();
        chunkLength = Integer.parseInt(currLine.toString(Constants.DEFAULT_CHARSET).trim(), 16);

        if (chunkLength + totalBytesReadCount > MAX_BODY_SIZE) {
          throw new ReadLimitException();
        }
        byte[] chunk = new byte[chunkLength];
        int bytesReadForChunk = reader.readBytes(chunk);
        totalBytesReadCount += bytesReadForChunk;

        if (bytesReadForChunk < chunkLength) {
          throw new HttpDeserializationException(
              "ERROR: chunk smaller than chunk size in chunked body, expected: "
                  + chunkLength
                  + " received: "
                  + bytesReadForChunk);
        }
        bodyAccumulator.write(chunk);
        if (chunkLength > 0) {
          String lineEnd = reader.readLine();
          if (!lineEnd.isEmpty()) {
            throw new HttpDeserializationException(
                "ERROR: chunk size smaller than actual chunk in chunked body, expected: "
                    + chunkLength
                    + " received: "
                    + (bytesReadForChunk + lineEnd.length()));
          }
        }
      } while (chunkLength > 0);

      return bodyAccumulator.toByteArray();
    } catch (NumberFormatException e) {
      throw new HttpDeserializationException(
          "ERROR: invalid chunk size in chunked body: "
              + (currLine != null ? currLine.toString().trim() : null),
          e);
    } catch (ReadLimitException e) {
      throw new HttpRequestSizeLimitException(
          "ERROR: body size exceeds capacity", HttpRequestSizeLimitException.Section.BODY);
    }
  }

  private static class RequestMetadata {
    public int contentLength;
    public boolean isChunkedBody;
    public boolean hasTrailingHeaders;

    public RequestMetadata(int contentLength, boolean isChunkedBody, boolean hasTrailingHeaders) {
      this.contentLength = contentLength;
      this.isChunkedBody = isChunkedBody;
      this.hasTrailingHeaders = hasTrailingHeaders;
    }
  }

  private static class HTTPReqFirstLine {
    public final String method;
    public final String path;
    public final String version;

    public HTTPReqFirstLine(String rawFirstLine) throws HttpDeserializationException {
      final String HTTP_FIRST_LINE_SEPARATOR = " ";
      String[] data = rawFirstLine.split(HTTP_FIRST_LINE_SEPARATOR);
      if (data.length != 3) {
        throw new HttpDeserializationException(
            "ERROR: Malformed HTTP request first line. Expected: \"METHOD path version\" found: "
                + rawFirstLine);
      }
      this.method = data[0];
      this.path = data[1];
      this.version = data[2];
    }
  }
}
