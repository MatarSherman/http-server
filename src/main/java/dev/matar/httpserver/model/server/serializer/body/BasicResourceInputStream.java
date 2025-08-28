package dev.matar.httpserver.model.server.serializer.body;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of {@link ResourceInputStream}, to wrap a standard {@link InputStream} without
 * added logic
 */
public class BasicResourceInputStream implements ResourceInputStream {
  protected final InputStream inputStream;

  public BasicResourceInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public long transferTo(OutputStream outputStream) throws IOException {
    return inputStream.transferTo(outputStream);
  }

  @Override
  public int read(byte[] destination) throws IOException {
    return inputStream.read(destination);
  }

  @Override
  public int read(byte[] destination, int offset, int length) throws IOException {
    return inputStream.read(destination, offset, length);
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
