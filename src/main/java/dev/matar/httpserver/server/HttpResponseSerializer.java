package dev.matar.httpserver.server;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.InvalidHttpResponseException;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.serializer.BodySerializerRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class HttpResponseSerializer {
  public static void serialize(HttpResponse<?> response, OutputStream outputStream)
      throws IOException, InvalidHttpResponseException {
    StringBuilder builder =
        new StringBuilder()
            .append(Constants.HTTP_VERSION + " ")
            .append(response.getStatus())
            .append(" ")
            .append(response.getMessage())
            .append("\r\n");

    response
        .getHeaders()
        .forEach(entry -> builder.append(serializeHeader(entry.getKey(), entry.getValue())));
    outputStream.write(builder.toString().getBytes(Constants.DEFAULT_CHARSET));

    BodySerializerRegistry bodySerializer = BodySerializerRegistry.get();
    bodySerializer.serialize(response, outputStream);
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

  public static String serializeHeader(HttpHeader header) {
    return header.toRaw() + Constants.HTTP_CRLF;
  }
}
