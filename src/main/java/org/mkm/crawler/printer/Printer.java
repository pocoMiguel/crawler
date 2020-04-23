package org.mkm.crawler.printer;

import java.io.FileWriter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Printer {
  private static FileWriter file;

  public void write(String content) {
    try {
      file = new FileWriter("output/output.json");
      file.write(content);
    } catch (IOException e) {
      log.error("Application error: failed to write result to a file");
    } finally {
      try {
        file.flush();
        file.close();
      } catch (IOException e) {
        log.error("Application error: failed to write result to a file: ", e);
      }
    }
  }
}
