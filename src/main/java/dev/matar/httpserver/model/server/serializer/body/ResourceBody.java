package dev.matar.httpserver.model.server.serializer.body;

import java.io.IOException;
import java.util.function.Function;

/**
 * Represents HTTP response bodies that are more complex than primitives and POJO'S.
 *
 * <p>Implementations represent "file-like" resources of binary nature that require specific
 * serialization strategies involving streaming
 */
public abstract class ResourceBody {
  public static final int UNKNOWN_CONTENT_LENGTH = -1;

  protected ResourceInputStream inputStream;

  protected ResourceBody(ResourceInputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void wrapStream(Function<ResourceInputStream, RangedResourceInputStream> streamWrapper) {
    this.inputStream = streamWrapper.apply(inputStream);
  }

  public abstract ResourceInputStream getInputStream() throws IOException;

  public abstract long getContentLength();

  public abstract String getFileName();

  public abstract boolean exists();
}
