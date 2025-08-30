package dev.matar.httpserver.server.metadata;

import dev.matar.httpserver.model.http.*;
import dev.matar.httpserver.model.server.serializer.body.FileResource;
import dev.matar.httpserver.model.server.serializer.body.ResourceBody;
import dev.matar.httpserver.util.RangedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class RangeMetadataProcessor implements HttpMetadataProcessor {
  @Override
  public void process(HttpRequest request, HttpResponse<?> response) {
    if (!(response.getBody() instanceof FileResource)) {
      return;
    }
    setAcceptRangesHeader(response);
    Optional<String> optionalRequestRange =
        request.getHeaders().getFirst(HttpHeaderKey.RANGE.value());

    if (optionalRequestRange.isPresent()) {
      ResourceBody body = (ResourceBody) response.getBody();
      configureResponseRange(response, body, optionalRequestRange.get());
    }
  }

  private void setAcceptRangesHeader(HttpResponse<?> response) {
    response
        .getHeaders()
        .set(HttpHeaderKey.ACCEPT_RANGES.value(), HttpHeaderValue.BYTES_RANGE.value());
  }

  private void configureResponseRange(
      HttpResponse<?> response, ResourceBody body, String rawRange) {
    String maxRangeSize = HttpHeader.CONTENT_RANGE_WILDCARD;
    try {
      validateRangeHeader(rawRange);
      long bodySize = body.getContentLength();
      maxRangeSize = bodySize + "";
      long[] bounds = getBoundsFromRangeHeader(rawRange, bodySize);

      body.setContentLength(getRangeSize(bounds));
      setContentRangeHeader(response, bounds, maxRangeSize);
      body.setStream(new RangedInputStream(body.getInputStream(), bounds[0], getRangeSize(bounds)));

      response.setStatus(HttpStatus.PARTIAL_CONTENT.code());
      return;
    } catch (IllegalArgumentException e) {
      System.out.println("INFO: found invalid range header in request, aborting configuration" + e);
    } catch (IOException e) {
      System.out.println("ERROR: could not apply range to body input stream");
    }
    configureUnsatisfiableRange(response, maxRangeSize);
  }

  private long[] getBoundsFromRangeHeader(String rawRange, long bodySize)
      throws IllegalArgumentException {
    int rangeBytesParamIndex = rawRange.indexOf(HttpHeader.RANGE_BYTES_PARAM);
    String rawBounds =
        rawRange.substring(rangeBytesParamIndex + HttpHeader.RANGE_BYTES_PARAM.length());

    long[] result = new long[2];
    long[] rangeHeaderBounds =
        Arrays.stream(rawBounds.split(HttpHeader.RANGE_BOUNDS_SEPARATOR))
            .filter(current -> !current.isEmpty())
            .mapToLong(Long::parseLong)
            .toArray();
    validateRangeHeaderBounds(rangeHeaderBounds, bodySize);

    int boundsSeparatorIndex = rawBounds.indexOf(HttpHeader.RANGE_BOUNDS_SEPARATOR);
    if (boundsSeparatorIndex == 0) {
      // Range: bytes=-<unitsFromEnd>
      result[0] = bodySize - rangeHeaderBounds[0];
      result[1] = bodySize - 1;
    } else if (boundsSeparatorIndex
        == rawBounds.length() - HttpHeader.RANGE_BOUNDS_SEPARATOR.length()) {
      // Range: bytes=<rangeStart>
      result[0] = rangeHeaderBounds[0];
      result[1] = bodySize - 1;
    } else {
      // Range: bytes=<rangeStart>-<rangeEnd>
      result[0] = rangeHeaderBounds[0];
      result[1] = rangeHeaderBounds[1];
    }
    return result;
  }

  private void validateRangeHeader(String rawRange) {
    if (!rawRange.startsWith(HttpHeader.RANGE_BYTES_PARAM)
        || !rawRange.contains(HttpHeader.RANGE_BOUNDS_SEPARATOR)) {
      throw new IllegalArgumentException("DEBUG: invalid format of range header");
    }
  }

  private void validateRangeHeaderBounds(long[] bounds, long bodySize)
      throws IllegalArgumentException {
    if (bounds.length > 2) {
      throw new IllegalArgumentException(
          "INFO: received multiple ranges. aborting due to unsupported feature");
    }
    if (bounds.length == 2 && bounds[1] <= bounds[0]) {
      throw new IllegalArgumentException(
          "INFO: invalid range header boundaries, end of range must be larger than start");
    }
    Arrays.stream(bounds).forEach(boundary -> validateRangeBoundary(boundary, bodySize));
  }

  private void validateRangeBoundary(long boundary, long bodySize) throws IllegalArgumentException {
    if (boundary < 0 || boundary > bodySize) {
      throw new IllegalArgumentException("INFO: invalid Range header boundary size: " + boundary);
    }
  }

  private void setContentRangeHeader(HttpResponse<?> response, long[] rangeBounds, String size) {
    String range;
    if (rangeBounds == null) {
      range = HttpHeader.CONTENT_RANGE_WILDCARD;
    } else {
      range = rangeBounds[0] + HttpHeader.RANGE_BOUNDS_SEPARATOR + rangeBounds[1];
    }
    String contentRange =
        HttpHeader.CONTENT_RANGE_BYTES_PARAM
            + range
            + HttpHeader.CONTENT_RANGE_TOTAL_SIZE_INDICATOR
            + size;

    response.getHeaders().set(HttpHeaderKey.CONTENT_RANGE.value(), contentRange);
  }

  private void configureUnsatisfiableRange(HttpResponse<?> response, String maxRangeSize) {
    response.setStatus(HttpStatus.RANGE_NOT_SATISFIABLE.code());
    response.setMessage("Range Not Satisfiable");
    response.setBody(null);
    setContentRangeHeader(response, null, maxRangeSize);
  }

  private long getRangeSize(long[] bounds) {
    return bounds[1] - bounds[0] + 1;
  }
}

//    if (response.getBody() instanceof Path bodyFile) {
//      Optional<String> ifModifiedSince =
//          request.getHeaders().getFirst(HttpHeaderKey.IF_MODIFIED_SINCE.value());
//
//      if (ifModifiedSince.isPresent()) {
//        try {
//          long lastModified = Files.getLastModifiedTime(bodyFile).toMillis();
//          long ifModifiedSinceTimestamp =
//              ZonedDateTime.parse(ifModifiedSince.get(), DateTimeFormatter.RFC_1123_DATE_TIME)
//                  .toInstant()
//                  .toEpochMilli();
//
//          if (lastModified < ifModifiedSinceTimestamp) {
//            response.setStatus(HttpStatus.NOT_MODIFIED.code());
//            response.setBody(null);
//            response
//                .getHeaders()
//                .set(HttpHeaderKey.LAST_MODIFIED.value(), String.valueOf(lastModified));
//            return;
//          }
//        } catch (DateTimeParseException _) {
//        }
//      }
//    }
