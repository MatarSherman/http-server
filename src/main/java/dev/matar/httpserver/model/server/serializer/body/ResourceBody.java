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

  protected InputStream inputStream;

  protected ResourceBody(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public InputStream getInputStream() {
    return this.inputStream;
  }

  public abstract long getContentLength();

  public abstract String getFileName();

  public abstract boolean exists();
}
