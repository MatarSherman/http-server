package dev.matar.httpserver.server;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.HttpBodySerializationException;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaders;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.server.bodySerializer.BodySerializerRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class HttpResponseSerializer {
  public static void serializeWithoutBody(HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    writeFirstLineAndHeaders(response, outputStream);
    writeEndOfHeaders(outputStream);
  }

  public static void serialize(HttpResponse<?> response, OutputStream outputStream)
      throws IOException, HttpBodySerializationException {
    writeFirstLineAndHeaders(response, outputStream);

    BodySerializerRegistry bodySerializer = BodySerializerRegistry.get();
    bodySerializer.serialize(response, outputStream);
  }

  private static void writeFirstLineAndHeaders(HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    StringBuilder stringBuilder = new StringBuilder();

    writeFirstLine(stringBuilder, response);
    writeHeaders(stringBuilder, response.getHeaders());

    outputStream.write(stringBuilder.toString().getBytes(Constants.DEFAULT_CHARSET));
  }

  private static void writeFirstLine(StringBuilder builder, HttpResponse<?> response) {
    builder
        .append(Constants.HTTP_VERSION + " ")
        .append(response.getStatus())
        .append(" ")
        .append(response.getMessage())
        .append("\r\n");
  }

  public static void writeHttpCRLF(OutputStream outputStream) throws IOException {
    outputStream.write(Constants.HTTP_CRLF.getBytes(Constants.DEFAULT_CHARSET));
  }

  public static void writeEndOfHeaders(OutputStream outputStream) throws IOException {
    writeHttpCRLF(outputStream);
  }

  public static String serializeHeader(String key, List<String> values) {
    String joinedValues = String.join(HttpHeader.MULTI_VALUE_SEPARATOR, values);
    return key + HttpHeader.KEY_VALUE_SEPARATOR + joinedValues + Constants.HTTP_CRLF;
  }

  public static String serializeHeader(String key, String value) {
    return key + HttpHeader.KEY_VALUE_SEPARATOR + value + Constants.HTTP_CRLF;
  }

  public static String serializeHeader(HttpHeader header) {
    return header.toRaw() + Constants.HTTP_CRLF;
  }

  public static void writeHeader(OutputStream outputStream, String key, String value)
      throws IOException {
    outputStream.write(serializeHeader(key, value).getBytes(Constants.DEFAULT_CHARSET));
  }

  private static void writeHeaders(StringBuilder builder, HttpHeaders headers) {
    headers.forEach(entry -> builder.append(serializeHeader(entry.getKey(), entry.getValue())));
  }

  public static void writeHeaders(HttpHeaders headers, OutputStream outputStream)
      throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    writeHeaders(stringBuilder, headers);

    outputStream.write(stringBuilder.toString().getBytes(Constants.DEFAULT_CHARSET));
  }

  public static void writeEndOfHeaders(HttpHeaders headers, OutputStream outputStream)
      throws IOException {
    writeHeaders(headers, outputStream);
    writeEndOfHeaders(outputStream);
  }
}
