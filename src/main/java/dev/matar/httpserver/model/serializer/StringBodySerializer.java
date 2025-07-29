package dev.matar.httpserver.model.serializer;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.model.http.MimeType;
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
    final String CHARSET_STRING_PARAM = "charset=";
    Charset charset = Constants.DEFAULT_CHARSET;
    byte[] result;

    Optional<String> contentType =
        response.getHeaders().getFirst(HttpHeaderKey.CONTENT_TYPE.value());
    if (contentType.isPresent()) {
      String[] splitContentType = contentType.get().split(CHARSET_STRING_PARAM);

      if (splitContentType.length > 1 && Charset.isSupported(splitContentType[1])) {
        charset = Charset.forName(splitContentType[1]);
      }
    }
    result = body.getBytes(charset);

    outputStream.write(
        HttpResponseSerializer.serializeHeader(HttpHeader.contentLength(result.length))
            .getBytes(Constants.DEFAULT_CHARSET));
    HttpResponseSerializer.writeEndOfHeaders(outputStream);
    outputStream.write(result);
  }

  @Override
  public boolean canSerialize(Object body, HttpResponse<?> response) {
    Optional<String> contentType =
        response.getHeaders().getFirst(HttpHeaderKey.CONTENT_TYPE.value());
    return body instanceof String
        && contentType.isPresent()
        && mimeTypes.stream().anyMatch(mimeType -> mimeType.value().contains(contentType.get()));
  }
}
