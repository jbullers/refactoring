package vthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import static vthreads.ContactBook.LAST_NAMES;
import static vthreads.ContactBook.randomElementOf;

public class ExecutorPlatform {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var contactBook = new ContactBook();

        var startTime = Instant.now();

        var results = new ArrayList<List<String>>();
        try (var executor = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory())) {
            //                try (var executor = Executors.newCachedThreadPool()) {
            //        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 1_000)) {
            var completionService = new ExecutorCompletionService<List<String>>(executor);
            for (int i = 0; i < 100_000; i++) {
                // Read contacts and extract emails
                completionService.submit(() -> contactBook.findByLastName(randomElementOf(LAST_NAMES))
                                                          .stream()
                                                          .map(Contact::email)
                                                          .toList());
            }

            // Collect results
            for (int i = 0; i < 100_000; i++) {
                results.add(completionService.take().resultNow());
            }
        }

        var endTime = Instant.now();

        System.out.println(results.size() + " requests handled");
        System.out.println("Time: " + Duration.between(startTime, endTime));
    }
}
