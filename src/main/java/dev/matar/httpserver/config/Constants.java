package dev.matar.httpserver.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {
  public static final char CARRIAGE_RETURN = '\r';
  public static final char NEW_LINE = '\n';
  public static final String HTTP_CRLF = "\r\n";
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  public static final String HTTP_VERSION = "HTTP/1.1";
}
