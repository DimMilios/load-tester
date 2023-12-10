package org.example;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

record Result(HttpResponse<String> response, Duration latency){}

public class Main {
    public static void main(String[] args) {
        CLIArgs cliArgs = null;
        try {
            cliArgs = new CLIParser(args).getArguments();
            System.out.println("Number of requests: " + cliArgs.number());
            System.out.println("Concurrency: " + cliArgs.concurrency());
            System.out.println("URL: " + cliArgs.uri());
        } catch (ParseException e) {
            System.err.println("Parsing failed. Reason: " + e.getMessage());
        }

        if (cliArgs == null) {
            System.err.println("Parsing failed.");
            System.exit(1);
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CLIArgs finalCliArgs = cliArgs;

            List<Future<Result>> results = new ArrayList<>();

            for (int i = 0; i < cliArgs.concurrency(); i++) {
                var f = executor.submit(() -> {
                    Instant start = Instant.now();
                    return new Result(sendRequest(finalCliArgs), Duration.between(start, Instant.now()));
                });
                results.add(f);
            }

            List<Long> latenciesMs = new ArrayList<>();
            for (Future<Result> result : results) {
                System.out.println("HTTP GET request. Status: " + result.get().response().statusCode() + " Body: " + result.get().response().body());
                System.out.println("Latency: " + result.get().latency().toMillis());
                latenciesMs.add(result.get().latency().toMillis());
            }

            latenciesMs.sort(Comparator.naturalOrder());

            long idx = Math.round(latenciesMs.size() * 0.8);
            assert idx < latenciesMs.size();
            System.out.println("80% percentile: " + latenciesMs.get(((int) idx)));
            System.out.println(latenciesMs);

            // Example: 10 requests, 80% percentile
            // latencies[40, 32, 31, 30, 25, 24, 23, 21, 20, 10]
            // index: 10 * 0.8 = 8
            // i.e. 80% of requests took less than 31ms


        } catch (ExecutionException | InterruptedException e) {
            e.getCause().printStackTrace();
        }
    }

    private static HttpResponse<String> sendRequest(CLIArgs cliArgs) throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(cliArgs.uri())
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .timeout(Duration.ofSeconds(15L))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }
}