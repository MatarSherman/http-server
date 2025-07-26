package dev.matar.httpserver.util;

import dev.matar.httpserver.model.http.HttpHeader;
import dev.matar.httpserver.model.http.HttpHeaderKey;

public class HttpUtils {
  public static boolean isContentLengthHeader(HttpHeader header) {
    return header.key().equalsIgnoreCase(HttpHeaderKey.CONTENT_LENGTH.value());
  }

  public static boolean isTransferEncodingChunkedHeader(HttpHeader header) {
    final String TRANSFER_ENCODING_CHUNKED = "chunked";
    return (header.key().equalsIgnoreCase(HttpHeaderKey.TRANSFER_ENCODING.value())
        && header.value().endsWith(TRANSFER_ENCODING_CHUNKED));
  }
}
