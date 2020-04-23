package org.mkm.crawler.crawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import javax.ws.rs.client.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.mkm.crawler.model.PageMap;

@Slf4j
@RequiredArgsConstructor
public final class CrawlerOrchestrator {

  public static int NUMBER_OF_CRAWLERS = 4;
  public static int IDLE_TIME = 4;

  private final Client client = JerseyClientBuilder
      .createClient(new ClientConfig().property(ClientProperties.FOLLOW_REDIRECTS, true));
  private final BlockingQueue<URL> urlsToVisit = new LinkedBlockingQueue<>();
  private final Set<URL> visitedURLs = Collections.synchronizedSet(new HashSet<>());
  private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CRAWLERS);
  private final PageMap pageMap = new PageMap();

  private final URL rootUrl;

  public Future<PageMap> startCrawling() {

    return CompletableFuture.supplyAsync(() -> {

      ArrayList<Future> futures = new ArrayList<>();
      urlsToVisit.add(rootUrl);
      for (int i = 0; i < NUMBER_OF_CRAWLERS; i++) {
        futures.add(
            executorService.submit(
                Crawler.builder()
                    .client(client)
                    .urlsToVisit(urlsToVisit)
                    .synchronizedVisitedURLs(visitedURLs)
                    .pageMap(pageMap)
                    .rootURL(rootUrl)
                    .idleTime(IDLE_TIME)
                    .build()
            )
        );
      }

      futures.forEach(f -> {
        try {
          f.get();
        } catch (InterruptedException | ExecutionException e) {
          log.error("Executor tasks interrupted", e);
        }
      });

      return this.pageMap;
    });
  }


  public void shutdown() {
    executorService.shutdown();
  }
}
