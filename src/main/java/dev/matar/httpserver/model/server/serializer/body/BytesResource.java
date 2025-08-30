package dev.matar.httpserver.model.server.serializer.body;

import java.io.ByteArrayInputStream;

public class BytesResource extends ResourceBody {
  private final byte[] bytes;
  private String filename;

  public BytesResource(byte[] bytes) {
    super(new ByteArrayInputStream(bytes));
    this.bytes = bytes;
  }

  void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public long getContentLength() {
    return bytes.length;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  public boolean exists() {
    return true;
  }
}
