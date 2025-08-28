package dev.matar.httpserver.model.server.serializer.body;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines a stream contract for use within {@link ResourceBody}
 *
 * <p>This interface allows the creation of decorators for {@link java.io.InputStream} instances
 * with added logic (e.g., ranged stream) without requiring extending the entire {@link
 * java.io.InputStream} class
 */
public interface ResourceInputStream extends Closeable {
  long transferTo(OutputStream outputStream) throws IOException;

  int read(byte[] destination) throws IOException;

  int read(byte[] destination, int offset, int length) throws IOException;
}
