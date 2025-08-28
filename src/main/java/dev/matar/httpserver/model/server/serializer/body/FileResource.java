package dev.matar.httpserver.model.server.serializer.body;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResource extends ResourceBody {
  private final Path file;

  public FileResource(Path file) throws IOException {
    super(new BasicResourceInputStream(Files.newInputStream(file)));
    this.file = file;
  }

  @Override
  public ResourceInputStream getInputStream() throws IOException {
    return this.inputStream;
  }

  @Override
  public long getContentLength() {
    try {
      return Files.size(file);
    } catch (IOException e) {
      return -1;
    }
  }

  @Override
  public String getFileName() {
    return file.getFileName().toString();
  }

  public boolean exists() {
    return Files.exists(file);
  }
}
