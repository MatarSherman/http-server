package dev.matar.httpserver.server.serializer;

import dev.matar.httpserver.exception.HttpSerializationException;
import dev.matar.httpserver.model.http.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;

public interface HttpBodySerializer<T> {
  void serialize(T body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException, HttpSerializationException;

  boolean canSerialize(Object body, HttpResponse<?> response);
}
