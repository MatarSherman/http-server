package dev.matar.httpserver.model.serializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StringBodySerializer implements HttpBodySerializer<String> {
  private static final List<MimeType> mimeTypes =
      Arrays.asList(
          MimeType.TEXT_PLAIN,
          MimeType.TEXT_HTML,
          MimeType.TEXT_CSS,
          MimeType.TEXT_JS,
          MimeType.TEXT_CSV,
          MimeType.TEXT_XML,
          MimeType.APP_JS,
          MimeType.APP_XML);

  public StringBodySerializer() {}

  @Override
  public void serialize(String body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException {
    Charset charset = Constants.DEFAULT_CHARSET;
    byte[] result;

    Optional<String> contentType =
        response.getHeaders().getFirst(HttpHeaderKey.CONTENT_TYPE.value());
    if (contentType.isPresent()) {
      Optional<String> charsetName = extractCharset(contentType.get());

      if (charsetName.isPresent() && Charset.isSupported(charsetName.get())) {
        charset = Charset.forName(charsetName.get());
      }
    }
    result = body.getBytes(charset);

    outputStream.write(
        HttpResponseSerializer.serializeHeader(HttpHeader.contentLength(result.length))
            .getBytes(Constants.DEFAULT_CHARSET));
    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    outputStream.write(result);
  }

  private Optional<String> extractCharset(String contentType) {
    final String CHARSET_STRING_PARAM = "charset=";
    String[] params = contentType.split(HttpHeader.SUB_PARAM_SEPARATOR);

    for (String param : params) {
      String normalized = param.trim().toLowerCase();
      if (normalized.startsWith(CHARSET_STRING_PARAM)) {
        return Optional.of(normalized.substring(CHARSET_STRING_PARAM.length()).replace("\"", ""));
      }
    }

    return Optional.empty();
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    Optional<String> contentType =
        response.getHeaders().getFirst(HttpHeaderKey.CONTENT_TYPE.value());

    if (contentType.isEmpty()) {
      return false;
    }
    String contentTypeMime = contentType.get().split(HttpHeader.SUB_PARAM_SEPARATOR)[0].trim();

    return body instanceof String
        && mimeTypes.stream()
            .anyMatch(mimeType -> contentTypeMime.equalsIgnoreCase(mimeType.value()));
  }
}
