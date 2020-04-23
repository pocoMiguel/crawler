package org.mkm.crawler.crawler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mkm.crawler.util.Util.stubURIWithContent;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mkm.crawler.model.Page;
import org.mkm.crawler.model.PageMap;
import org.mockito.Mockito;

public class CrawlerTest {
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8080));

  private static String HOST_URL = "http://localhost:8080";

  private Crawler crawler;
  private BlockingQueue<URL> urlsToVisit;
  private PageMap pageMap;

  @Before
  public void setUp() throws Exception {
    urlsToVisit = new LinkedBlockingQueue<>();
    pageMap = new PageMap();
    crawler = Crawler.builder()
        .client(JerseyClientBuilder.createClient())
        .urlsToVisit(urlsToVisit)
        .synchronizedVisitedURLs(new HashSet<>())
        .pageMap(pageMap)
        .rootURL(new URL(HOST_URL))
        .build();
  }

  @Test
  public void visitsRootURL() throws Exception {
    // Given a root URL to crawl
    stubURIWithContent("/", "Something");
    urlsToVisit.add(buildURLForRelativePath("/", ""));

    // When crawlerOrchestrator is initiated
    crawler.run();

    // Then the root URL is visited
    verify(1, getRequestedFor(urlPathEqualTo("/")));
    assertThat(pageMap.getPages().size(), is(1));
    assertThat(pageMap.getPages().get("/"), notNullValue());
  }

  @Test
  public void doesNotRevisitVisitedPages() throws Exception {
    // Given that I have two pages linked to each other (thus creating a loop)
    stubURIWithContent("/", "<a href=\"/page\">Page</a>");
    stubURIWithContent("/page", "<a href=\"/\">Index</a>");
    urlsToVisit.add(buildURLForRelativePath("/", ""));

    // When I crawl from the root URL
    crawler.run();

    // Then I expect to not see the same URL crawled again
    verify(1, getRequestedFor(urlPathEqualTo("/")));
    verify(1, getRequestedFor(urlPathEqualTo("/page")));
    assertThat(urlsToVisit.size(), is(0));
    assertThat(pageMap.getPages().size(), is(2));

    Page rootPage = pageMap.getPages().get("/");
    assertThat(rootPage.getPageLinks().size(), is(1));
    assertThat(rootPage.getPageLinks().contains("/page"), is(true));

    Page pageLink = pageMap.getPages().get("/page");
    assertThat(pageLink.getPageLinks().size(), is(1));
    assertThat(pageLink.getPageLinks().contains("/"), is(true));
  }

  @Test
  public void ignoresNonReachableURLs() throws Exception {
    // Given a page with a non-reachable URL
    stubURIWithContent("/", "<a href=\"/page\">Not found</a>");
    stubFor(
        get(
            urlEqualTo("/page")
        ).willReturn(
            aResponse().withStatus(404)
        )
    );
    urlsToVisit.add(buildURLForRelativePath("/", ""));

    // When crawler crawls
    crawler.run();

    // Then I expect the crawler to ignore the error and continue on with other operations
    verify(1, getRequestedFor(urlPathEqualTo("/")));
    verify(1, getRequestedFor(urlPathEqualTo("/page")));
    assertThat(urlsToVisit.size(), is(0));
    assertThat(pageMap.getPages().size(), is(1));
  }

  @Test
  public void onlyVisitsSingleDomain() throws Exception {

    // Given a crawler with mock JAX-RS client
    final String baseURL = String.format("%s/", HOST_URL);
    final String baseURLContent = "<a href=\"http://google.com\">Google</a>";
    Client mockClient = Mockito.mock(Client.class);
    WebTarget mockWebTarget = Mockito.mock(WebTarget.class);
    Invocation.Builder mockBuilder = Mockito.mock(Invocation.Builder.class);

    when(mockClient.target(baseURL)).thenReturn(mockWebTarget);
    when(mockWebTarget.request(MediaType.TEXT_HTML)).thenReturn(mockBuilder);
    when(mockBuilder.get(String.class)).thenReturn(baseURLContent);
    crawler = Crawler.builder()
        .client(mockClient)
        .urlsToVisit(urlsToVisit)
        .synchronizedVisitedURLs(new HashSet<>())
        .pageMap(pageMap)
        .rootURL(new URL(HOST_URL))
        .build();

    // And that the crawler is to crawl a page with an external link
    urlsToVisit.add(buildURLForRelativePath("/", ""));

    // When crawler crawls
    crawler.run();

    // Then I expect the root URL and Page 1 to be crawled
    Mockito.verify(mockClient, times(1)).target(baseURL);
    Mockito.verify(mockClient, times(0)).target("http://google.com");
  }

  private static URL buildURLForRelativePath(String path, String title) throws MalformedURLException {
    String url = String.format("%s%s", HOST_URL, path);
    return new URL(url);
  }
}