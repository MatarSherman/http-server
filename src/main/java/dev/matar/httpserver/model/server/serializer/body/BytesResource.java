package dev.matar.httpserver.model.server.serializer.body;

import java.io.ByteArrayInputStream;

public class BytesResource extends ResourceBody {
  private String filename;

  public BytesResource(byte[] bytes) {
    super(new ByteArrayInputStream(bytes), bytes.length);
  }

  void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  public boolean exists() {
    return true;
  }
}
