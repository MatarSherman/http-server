package dev.matar.httpserver.server;

import dev.matar.httpserver.config.Constants;
import dev.matar.httpserver.exception.ReadLimitException;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class HttpStreamReader implements Closeable {
  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final int DEFAULT_MAX_LINE_SIZE = 8192;
  private static final int EOF = -1;

  private final InputStream inputStream;
  private final byte[] buffer;
  private int bufferIndex;
  private int bufferLimit;

  public HttpStreamReader(InputStream input, int bufferSize) {
    this.inputStream = input;
    this.buffer = new byte[bufferSize];
    this.bufferIndex = 0;
  }

  public HttpStreamReader(InputStream input) {
    this.inputStream = input;
    this.buffer = new byte[DEFAULT_BUFFER_SIZE];
    this.bufferIndex = 0;
  }

  private void fillBuffer() throws IOException {
    if (bufferIndex >= bufferLimit) {
      bufferIndex = 0;
      bufferLimit = inputStream.read(buffer);
    }
  }

  private int readByte() throws IOException {
    if (bufferIndex >= bufferLimit) {
      fillBuffer();
      if (bufferLimit == EOF) {
        return EOF;
      }
    }
    return Byte.toUnsignedInt(buffer[bufferIndex++]);
  }

  public ByteArrayOutputStream readLineRaw(int maxBytes) throws IOException, ReadLimitException {
    ByteArrayOutputStream line = new ByteArrayOutputStream();
    int bytesReadCount = 0;
    int currByte;
    boolean isPrevByteCR = false;

    while ((currByte = readByte()) != EOF) {
      bytesReadCount++;
      if (bytesReadCount > maxBytes) {
        throw new ReadLimitException("ERROR: reached maximum length for line");
      }
      if (currByte == Constants.NEW_LINE) {
        return line;
      }
      if (isPrevByteCR) {
        line.write(Constants.CARRIAGE_RETURN);
        isPrevByteCR = false;
      }
      if (currByte == Constants.CARRIAGE_RETURN) {
        isPrevByteCR = true;
      } else {
        line.write(currByte);
      }
    }
    if (isPrevByteCR) {
      line.write(currByte);
    }

    return line.size() > 0 ? line : null;
  }

  public String readLine(int maxBytes) throws IOException, ReadLimitException {
    ByteArrayOutputStream line = readLineRaw(maxBytes);
    return line != null ? line.toString(Constants.DEFAULT_CHARSET) : null;
  }

  public String readLine() throws IOException, ReadLimitException {
    return readLine(DEFAULT_MAX_LINE_SIZE);
  }

  public int readBytes(byte[] bytes) throws IOException {
    int amountToReadFromBuffer = Math.min(bufferLimit - bufferIndex, bytes.length);
    System.arraycopy(buffer, bufferIndex, bytes, 0, amountToReadFromBuffer);
    bufferIndex += amountToReadFromBuffer;

    if (amountToReadFromBuffer < bytes.length) {
      return inputStream.readNBytes(
          bytes, amountToReadFromBuffer, bytes.length - amountToReadFromBuffer);
    }

    return bytes.length;
  }

  @Override
  public void close() throws IOException {
    inputStream.close();
  }
}
