package org.mkm.crawler.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class Util {
  public static void stubURIWithFilename(String URI, String filename) {
    stubFor(
        get(
            urlEqualTo(URI)
        ).willReturn(
            aResponse().withBodyFile(filename)
        )
    );
  }

  public static void stubURIWithContent(String URI, String content) {
    stubFor(
        get(
            urlEqualTo(URI)
        ).willReturn(
            aResponse().withBody(content)
        )
    );
  }
}
