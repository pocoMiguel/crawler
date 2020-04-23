package org.mkm.crawler.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mkm.crawler.util.Util.stubURIWithFilename;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mkm.crawler.crawler.CrawlerOrchestrator;
import org.mkm.crawler.model.Page;
import org.mkm.crawler.model.PageMap;

public class CrawlerIntegrationTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8080));

  private CrawlerOrchestrator crawler;

  @Before
  public void setUp() throws Exception {
    crawler = new CrawlerOrchestrator(
        new URL("http://localhost:8080/")
    );
  }

  @Test
  public void crawlURL() throws Exception {
    stubURIWithFilename("/", "index.html");
    stubURIWithFilename("/page2", "page2.html");
    stubURIWithFilename("/page3", "page3.html");
    stubURIWithFilename("/page4", "page4.html");

    Future<PageMap> result = crawler.startCrawling();

    assertThat(result, notNullValue());
    PageMap pageMap = result.get();

    verify(1, getRequestedFor(urlEqualTo("/")));
    verify(1, getRequestedFor(urlEqualTo("/page2")));
    verify(1, getRequestedFor(urlEqualTo("/page3")));
    verify(1, getRequestedFor(urlEqualTo("/page4")));

    assertThat(pageMap.getPages().size(), is(4));

    verticesAdjacentToVertex(pageMap, "/", "/page2", "/page3");
    verticesAdjacentToVertex(pageMap, "/page2", "/page3", "/page4");
    verticesAdjacentToVertex(pageMap, "/page3", "/page4");
    verticesAdjacentToVertex(pageMap, "/page4", "/");
  }

  private static void verticesAdjacentToVertex(
      PageMap pageMap, String pageKey, String... adjacentVertices) {
    assertThat(pageMap.getPages().containsKey(pageKey), is(true));
    Page page = pageMap.getPages().get(pageKey);

    Arrays.stream(adjacentVertices)
        .forEach(link ->
            assertThat(page.getPageLinks().contains(link), is(true))
        );
  }
}
