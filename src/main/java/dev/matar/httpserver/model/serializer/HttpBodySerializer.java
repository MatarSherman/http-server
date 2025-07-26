package dev.matar.httpserver.model.serializer;

import dev.matar.httpserver.exception.InvalidHttpResponseException;
import dev.matar.httpserver.model.http.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;

public interface HttpBodySerializer<T> {
  void serialize(T body, HttpResponse<?> response, OutputStream outputStream)
      throws IOException, InvalidHttpResponseException;

  boolean canSerialize(Object body, HttpResponse<?> response);
}
