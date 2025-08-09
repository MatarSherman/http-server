package dev.matar.httpserver.server;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.HttpSerializationException;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.server.serializer.BodySerializerRegistry;
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
      throws IOException, HttpSerializationException {
    writeFirstLineAndHeaders(response, outputStream);

    BodySerializerRegistry bodySerializer = BodySerializerRegistry.get();
    bodySerializer.serialize(response, outputStream);
  }

  private static void writeFirstLineAndHeaders(HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    StringBuilder stringBuilder = new StringBuilder();

    writeFirstLine(stringBuilder, response);
    writeHeaders(stringBuilder, response);

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

  private static void writeHttpCRLF(OutputStream outputStream) throws IOException {
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

  private static void writeHeaders(StringBuilder builder, HttpResponse<?> response) {
    response
        .getHeaders()
        .forEach(entry -> builder.append(serializeHeader(entry.getKey(), entry.getValue())));
  }
}
