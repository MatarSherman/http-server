package dev.matar.httpserver.model.server.serializer.body;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link ResourceInputStream} implementation decorator that limits reads to a range of bytes
 * within the stream.
 *
 * <p>On instantiation, it skips the wrapped stream to the specified offset
 */
public class RangedResourceInputStream implements ResourceInputStream {
  public static final int BUFFER_SIZE = 8192;

  private final long maxBytesToRead;
  private final InputStream inputStream;

  private long totalBytesRead;

  public RangedResourceInputStream(InputStream inputStream, long start, long length)
      throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException(
          "ERROR: RangedResourceInputStream length must be positive");
    }
    this.inputStream = inputStream;
    this.inputStream.skipNBytes(start);
    this.maxBytesToRead = length;
    this.totalBytesRead = 0;
  }

  public long transferTo(OutputStream outputStream) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];

    int currentBytesRead = 0;
    while (currentBytesRead != -1 && totalBytesRead < maxBytesToRead) {
      int bytesToRead = (int) Math.min(BUFFER_SIZE, maxBytesToRead - totalBytesRead);

      currentBytesRead = inputStream.read(buffer, 0, bytesToRead);
      if (currentBytesRead != -1) {
        outputStream.write(buffer, 0, currentBytesRead);
        totalBytesRead += currentBytesRead;
      }
    }
    return totalBytesRead;
  }

  public int read(byte[] destination) throws IOException {
    return read(destination, 0, destination.length);
  }

  public int read(byte[] destination, int offset, int length) throws IOException {
    if (totalBytesRead >= maxBytesToRead) {
      return -1;
    }
    int bytesToRead = (int) Math.min(length, maxBytesToRead - totalBytesRead);

    int bytesRead = inputStream.read(destination, offset, bytesToRead);
    if (bytesRead != -1) {
      totalBytesRead += bytesRead;
    }
    return bytesRead;
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
