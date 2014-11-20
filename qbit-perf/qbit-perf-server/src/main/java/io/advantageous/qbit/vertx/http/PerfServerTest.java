package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.HttpServerBuilder;
import org.boon.core.Sys;

import static org.boon.Boon.puts;

/**
 * Created by Richard on 11/12/14.
 */
public class PerfServerTest {





    public static void main(String... args) {



        puts("SERVER Arguments", args);


        String argHost = "localhost";
        int argPort = 8080;
        int argBatchSize = 10;
        int argFlushRate = 100;
        int argPollTime = 10;


        if (args.length > 0) {

            argHost = args[0];
        }


        if (args.length > 1) {

            argPort = Integer.parseInt(args[1]);
        }


        if (args.length > 2) {

            argBatchSize = Integer.parseInt(args[2]);
        }

        if (args.length > 3) {

            argFlushRate = Integer.parseInt(args[3]);
        }

        if (args.length > 4) {

            argPollTime = Integer.parseInt(args[4]);
        }


        final String host = argHost;
        final int port = argPort;
        final int batchSize = argBatchSize;
        final int flushRate = argFlushRate;
        final int pollTime = argPollTime;


        puts("Params for SERVER: host", host, "port", port);

        puts("\nParams for client batchSize", batchSize, "flushRate", flushRate);

        puts("Params for client pollTime", pollTime);




        final HttpServer server = new HttpServerBuilder()
                                    .setPort(port)
                                    .setHost(host)
                                    .setPollTime(pollTime)
                                    .setRequestBatchSize(batchSize)
                                    .setFlushInterval(flushRate)

                                    .setHttpRequestConsumer(request -> {

                                        if (request.getUri().equals("/perf/")) {
                                            request.getResponse().response(200, "application/json", "\"ok\"");
                                        }
                                    }).build();


        server.start();


        Sys.sleep(1000);



        Sys.sleep(1_000 * 1_000);
    }
}