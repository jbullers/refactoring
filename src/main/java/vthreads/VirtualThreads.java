package vthreads;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static vthreads.ContactBook.LAST_NAMES;
import static vthreads.ContactBook.randomElementOf;

public class VirtualThreads {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        var contactBook = new ContactBook();

        long startTime = System.currentTimeMillis();

        var contactFutures = new ArrayList<Future<List<Contact>>>();
        var results = new ArrayList<List<String>>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var completionService = new ExecutorCompletionService<List<String>>(executor);
            for (int i = 0; i < 100_000; i++) {
                // Read contacts and extract emails
                completionService.submit(() -> contactBook.findByLastName(randomElementOf(LAST_NAMES))
                                                          .stream()
                                                          .map(Contact::email)
                                                          .toList());
            }

            for (int i = 0; i < 100_000; i++) {
                results.add(completionService.take().resultNow());
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println(results.size() + " requests handled");
        System.out.println("Time: " + Duration.ofMillis(endTime - startTime));
    }
}
