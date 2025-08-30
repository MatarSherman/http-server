package dev.matar.httpserver.model.server.serializer.body;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResource extends ResourceBody {
  private final Path file;

  public FileResource(Path file) throws IOException {
    super(Files.newInputStream(file), Files.size(file));
    this.file = file;
  }

  @Override
  public String getFileName() {
    return file.getFileName().toString();
  }

  public boolean exists() {
    return Files.exists(file);
  }
}
