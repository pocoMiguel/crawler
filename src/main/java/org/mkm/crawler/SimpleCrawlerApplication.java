package org.mkm.crawler;

import com.google.gson.Gson;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.mkm.crawler.crawler.CrawlerOrchestrator;
import org.mkm.crawler.model.PageMap;
import org.mkm.crawler.printer.Printer;

@Slf4j
public class SimpleCrawlerApplication {

  public static void main(String[] args) {

    if (args.length <= 0) {
      log.error("Need to provide URL for the crawler to work");
      System.exit(0);
    }

    String rootUrl = args[0];

    try {
      CrawlerOrchestrator crawlerOrchestrator = new CrawlerOrchestrator(new URL(rootUrl));
      log.info("Starting crawling URL {}", rootUrl);

      Future<PageMap> pageMapFuture = crawlerOrchestrator.startCrawling();
      PageMap pageMap = pageMapFuture.get();

      Printer printer = new Printer();
      printer.write(new Gson().toJson(pageMap.getPages()));

      crawlerOrchestrator.shutdown();
      log.info("Done.");

    } catch (MalformedURLException e) {
      log.error("URL given is malformed. Please do not include a trailing slash. Example:\nhttp://google.com");
    } catch (InterruptedException | ExecutionException e) {
      log.error("Crawling operation is interrupted");
    }
  }

}
