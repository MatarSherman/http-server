package dev.matar.httpserver.model.server.serializer.body;

import java.io.IOException;
import java.io.InputStream;

public class StreamResource extends ResourceBody {
  private String filename;

  public StreamResource(InputStream inputStream) {
    super(new BasicResourceInputStream(inputStream));
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public ResourceInputStream getInputStream() throws IOException {
    return inputStream;
  }

  @Override
  public long getContentLength() {
    return ResourceBody.UNKNOWN_CONTENT_LENGTH;
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
