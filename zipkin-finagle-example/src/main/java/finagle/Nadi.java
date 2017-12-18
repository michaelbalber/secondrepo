package finagle;

import com.twitter.app.Flags;
import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.zipkin.core.SamplingTracer;
import com.twitter.util.Future;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import zipkin.finagle.http.HttpZipkinTracer;

public class Nadi extends Service<Request, Response> {

  @Override
  public Future<Response> apply(Request request) {
    Response response = Response.apply();
    if (request.path().equals("/api")) {
      response.write(new Date().toString());
    } else {
      response.setStatusCode(404);
    }
    return Future.value(response);
  }

  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      args = new String[] {
          // All servers need to point to the same zipkin transport (note this is default)
              "-zipkin.initialSampleRate", "1.0",

    		  "-zipkin.http.host", "localhost:9411"
      };
    }

    // parse any commandline arguments
    new Flags("nadi", true, true).parseOrExit1(args, false);

    // It is unreliable to rely on implicit tracer config (Ex sometimes NullTracer is used).
    // Always set the tracer explicitly. The default constructor reads from system properties.
    SamplingTracer tracer = new HttpZipkinTracer();

    ServerBuilder.safeBuild(
        new Nadi(),
        ServerBuilder.get()
            .tracer(tracer)
            .codec(Http.get().enableTracing(true))
            .bindTo(new InetSocketAddress(InetAddress.getLoopbackAddress(), 10000))
            .name("nadi")); // this assigns the local service name
  }
}
