package dev.matar.httpserver.server.serializer;

import dev.matar.httpserver.exception.HttpSerializationException;
import dev.matar.httpserver.model.http.HttpResponse;
import dev.matar.httpserver.server.HttpResponseSerializer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BodySerializerRegistry {
  private final List<HttpBodySerializer<?>> serializers = new ArrayList<>();
  private final HttpBodySerializer<Object> fallbackSerializer = new JSONBodySerializer();

  private static final BodySerializerRegistry INSTANCE = new BodySerializerRegistry();

  private BodySerializerRegistry() {
    serializers.add(new StringBodySerializer());
    serializers.add(new BytesBodySerializer());
    serializers.add(new PrimitiveBodySerializer());
    serializers.add(new StreamBodySerializer());
  }

  public void serialize(HttpResponse<?> response, OutputStream outputStream)
      throws IOException, HttpSerializationException {
    Object body = response.getBody();
    if (body == null) {
      HttpResponseSerializer.writeEndOfHeaders(outputStream);
      return;
    }
    HttpBodySerializer<?> serializer =
        serializers.stream()
            .filter(curr -> curr.canSerialize(body, response))
            .findFirst()
            .orElse(fallbackSerializer);

    useSerializer(serializer, body, response, outputStream);
  }

  /**
   * The casting of body to < T > is logically safe, due to the "canSerialize" checks confirming the
   * runtime type of body.
   */
  @SuppressWarnings("unchecked")
  private <T> void useSerializer(
      HttpBodySerializer<T> serializer,
      Object body,
      HttpResponse<?> response,
      OutputStream outputStream)
      throws IOException, HttpSerializationException {
    serializer.serialize((T) body, response, outputStream);
  }

  public static BodySerializerRegistry get() {
    return INSTANCE;
  }
}
