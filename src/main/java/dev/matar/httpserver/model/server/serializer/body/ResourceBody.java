package dev.matar.httpserver.model.server.serializer.body;

import java.io.InputStream;

/**
 * Represents HTTP response bodies that are more complex than primitives and POJO'S.
 *
 * <p>Implementations represent "file-like" resources of binary nature that require specific
 * serialization strategies involving streaming
 */
public abstract class ResourceBody {
  public static final int UNKNOWN_CONTENT_LENGTH = -1;

  private InputStream inputStream;
  private long contentLength;

  public ResourceBody(InputStream inputStream, long contentLength) {
    this.inputStream = inputStream;
    this.contentLength = contentLength;
  }

  public void setStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public InputStream getInputStream() {
    return this.inputStream;
  }

  public long getContentLength() {
    return this.contentLength;
  }

  public void setContentLength(long contentLength) {
    this.contentLength = contentLength;
  }

  public abstract String getFileName();

  public abstract boolean exists();
}
