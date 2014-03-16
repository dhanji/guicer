package com.wideplay.guicer.stat;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This module is used to periodically broadcast the current snapshot of stats
 * to a preconfigured HTTP URL somewhere on the web. This module should can used as
 * an alternative to StatServletModule to push stats to a remote collector rather
 * than have the collector pull it from us via http.
 *
 * For example, to regularly broadcast stats to the librato metric service:
 * <pre>
 *   install(new StatBroadcastModule(new URI("https://metrics-api.librato.com/v1/metrics"),
 *                                   StatBroadcastModule.basicAuthOf("&lt;username&gt;", "&lt;password&gt;",
 *                                   new LibratoJsonPublisher(),
 *                                   TimeUnit.SECONDS.toMillis(30));
 * </pre>
 *
 * This module will broadcast stats every 30 seconds to librato's HTTPS metrics URI using
 * basic-auth credentials. Note that the LibratoJsonPublisher is an example and you will
 * have to supply your own.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class StatBroadcastModule extends AbstractModule {
  private final URL endpoint;
  private final StatsPublisher publisher;
  private final Long rate;
  private final ImmutableMap<String, String> headers;

  public StatBroadcastModule(URI endpoint,
                             ImmutableMap<String, String> headers,
                             StatsPublisher publisher,
                             Long rateInMillis) {
    this.headers = headers;
    try {
      this.endpoint = endpoint.toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    this.publisher = publisher;
    this.rate = rateInMillis;
  }

  @Override
  protected void configure() {
    if (rate < TimeUnit.SECONDS.toMillis(20)) {
      addError("Specified rate of stats-broadcast is too fast: " + rate + "ms ");
      return;
    }

    String protocol = endpoint.getProtocol().toLowerCase();
    if (!"http".equals(protocol) && !"https".equals(protocol)) {
      addError("Only HTTP or HTTPS endpoints are supported: " + endpoint);
      return;
    }

    Broadcaster broadcaster = new Broadcaster(endpoint, headers, rate, publisher);
    bind(Broadcaster.class).toInstance(broadcaster);
    requestInjection(broadcaster);
  }

  public static ImmutableMap<String, String> basicAuthOf(String user, String password) {
    String authString = user + ":" + password;
    return ImmutableMap.of("Authorization", "Basic " + Base64.getEncoder().encodeToString(authString.getBytes()));
  }

  static class Broadcaster {
    private static final ScheduledExecutorService broadcaster = Executors.newSingleThreadScheduledExecutor();

    @Inject
    private Stats stats;

    Broadcaster(final URL endpoint, final ImmutableMap<String, String> headers, long delay, final StatsPublisher publisher) {
      broadcaster.scheduleWithFixedDelay(new Runnable() {
        @Override
        public void run() {
          try {
            // This cast is guaranteed by our protocol check.
            HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", publisher.getContentType());
            for (Map.Entry<String, String> header : headers.entrySet()) {
             connection.addRequestProperty(header.getKey(), header.getValue());
            }

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()))) {
              publisher.publish(stats.snapshot(), writer);
            }

            if (connection.getResponseCode() >= 400) {
              throw new IOException("Unable to broadcast stats to " + endpoint
                  + "\n => status: " + connection.getResponseCode()
                  + "\n => " + connection.getResponseMessage());
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }, delay * 3, delay, TimeUnit.MILLISECONDS);
    }
  }
}
