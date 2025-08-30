package dev.matar.httpserver.model.server.serializer.body;

import java.io.InputStream;

public class StreamResource extends ResourceBody {
  private String filename;

  public StreamResource(InputStream inputStream) {
    super(inputStream, ResourceBody.UNKNOWN_CONTENT_LENGTH);
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  @Override
  public boolean exists() {
    return true;
  }
}
