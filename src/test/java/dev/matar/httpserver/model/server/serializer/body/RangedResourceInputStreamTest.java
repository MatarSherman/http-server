package dev.matar.httpserver.model.server.serializer.body;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RangedResourceInputStreamTest {
  private static byte[] testDataSource = new byte[0];
  private static ByteArrayInputStream inputStream;

  @BeforeAll
  static void generateData() {
    testDataSource = ("0123456789".repeat(10)).getBytes();
  }

  @BeforeEach
  void initInputStream() {
    inputStream = new ByteArrayInputStream(testDataSource);
  }

  @Test
  void testTransferWithRangeInMiddle() throws IOException {
    int RANGE_START = 5;
    int RANGE_LENGTH = 10;
    byte[] expected = new byte[RANGE_LENGTH];
    System.arraycopy(testDataSource, RANGE_START, expected, 0, RANGE_LENGTH);

    RangedResourceInputStream rangedResourceInputStream =
        new RangedResourceInputStream(inputStream, RANGE_START, RANGE_LENGTH);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    long bytesTransferred = rangedResourceInputStream.transferTo(outputStream);

    new String(expected);
    assertArrayEquals(
        expected, outputStream.toByteArray(), "Should transfer all bytes in the range");
    assertEquals(
        RANGE_LENGTH,
        bytesTransferred,
        "The return value should represent the amount of bytes read");
  }

  @Test
  void shouldTransferWithRangeExceedingBounds() throws IOException {
    int RANGE_START = -9999;
    int RANGE_LENGTH = testDataSource.length * 2;
    byte[] expected = testDataSource;

    RangedResourceInputStream rangedResourceInputStream =
        new RangedResourceInputStream(inputStream, RANGE_START, RANGE_LENGTH);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    rangedResourceInputStream.transferTo(outputStream);

    assertArrayEquals(
        expected,
        outputStream.toByteArray(),
        "Should transfer all bytes up to the ends of stream when the provided range exceeds them");
  }

  @Test
  void shouldThrowOnIllegalArgs() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new RangedResourceInputStream(inputStream, 0, -5),
        "Should throw on negative range length");
  }
}
