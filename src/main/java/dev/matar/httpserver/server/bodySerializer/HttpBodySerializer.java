package dev.matar.httpserver.server.bodySerializer;

import dev.matar.httpserver.exception.HttpBodySerializationException;
import dev.matar.httpserver.model.http.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;

public interface HttpBodySerializer<T> {
  void serialize(T body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException, HttpBodySerializationException;

  boolean canSerialize(Object body, HttpResponse<?> response);
}
