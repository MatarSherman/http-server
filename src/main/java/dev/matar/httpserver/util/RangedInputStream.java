package dev.matar.httpserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link InputStream} implementation decorator that limits reads to a range of bytes within the
 * stream.
 *
 * <p>Should only be used with streams that support skipping!
 *
 * <p>On instantiation, it skips the wrapped stream to the specified offset
 */
public class RangedInputStream extends InputStream {
  public static final int BUFFER_SIZE = 8192;

  private final long maxBytesToRead;
  private final InputStream inputStream;

  private long totalBytesRead;

  public RangedInputStream(InputStream inputStream, long start, long length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("ERROR: RangedInputStream length must be positive");
    }
    this.inputStream = inputStream;
    this.inputStream.skipNBytes(start);
    this.maxBytesToRead = length;
    this.totalBytesRead = 0;
  }

  @Override
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

  @Override
  public int read(byte[] destination) throws IOException {
    return this.read(destination, 0, destination.length);
  }

  @Override
  public int read(byte[] destination, int offset, int length) throws IOException {
    if (totalBytesRead >= maxBytesToRead) {
      return -1;
    }
    int bytesToRead = (int) Math.min(length, maxBytesToRead - totalBytesRead);

    int bytesRead = inputStream.read(destination, offset, bytesToRead);
    totalBytesRead += Math.max(bytesRead, 0);
    return bytesRead;
  }

  @Override
  public int read() throws IOException {
    if (totalBytesRead >= maxBytesToRead) {
      return -1;
    }
    int bytesRead = inputStream.read();
    totalBytesRead += Math.max(bytesRead, 0);
    return bytesRead;
  }

  @Override
  public long skip(long n) throws IOException {
    long skipped = inputStream.skip(Math.min(n, maxBytesToRead - totalBytesRead));
    totalBytesRead += skipped;
    return skipped;
  }

  @Override
  public void skipNBytes(long n) throws IOException {
    long skipAmount = Math.min(n, maxBytesToRead - totalBytesRead);
    if (skipAmount == 0) {
      return;
    }
    inputStream.skipNBytes(skipAmount);
    totalBytesRead += skipAmount;
  }

  @Override
  public int available() throws IOException {
    return (int) Math.min(maxBytesToRead - totalBytesRead, inputStream.available());
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    if (totalBytesRead >= maxBytesToRead) {
      return new byte[0];
    }
    byte[] bytes = inputStream.readNBytes((int) Math.min(maxBytesToRead, Integer.MAX_VALUE));
    totalBytesRead += bytes.length;
    return bytes;
  }

  @Override
  public int readNBytes(byte[] b, int off, int len) throws IOException {
    if (totalBytesRead >= maxBytesToRead || b.length == 0) {
      return 0;
    }
    int bytesRead = super.readNBytes(b, off, (int) Math.min(len, maxBytesToRead - totalBytesRead));
    totalBytesRead += bytesRead;
    return bytesRead;
  }

  @Override
  public byte[] readNBytes(int len) throws IOException {
    if (totalBytesRead >= maxBytesToRead || len == 0) {
      return new byte[0];
    }
    byte[] bytes = super.readNBytes((int) Math.min(len, maxBytesToRead - totalBytesRead));
    totalBytesRead += bytes.length;
    return bytes;
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
