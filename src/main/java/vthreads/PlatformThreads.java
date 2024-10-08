package vthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static vthreads.ContactBook.LAST_NAMES;
import static vthreads.ContactBook.randomElementOf;

public class PlatformThreads {

    public static void main(String[] args) throws InterruptedException {
        var contactBook = new ContactBook();

        var startTime = Instant.now();

        var threads = new ArrayList<Thread>();
        var results = new ConcurrentLinkedDeque<List<String>>();
        for (int i = 0; i < 100_000; i++) {
            // Read contacts and extract their emails
            var thread = Thread.ofPlatform().start(() -> {
                var emails = contactBook.findByLastName(randomElementOf(LAST_NAMES))
                                        .stream()
                                        .map(Contact::email)
                                        .toList();
                results.add(emails);
            });
            threads.add(thread);
        }

        for (var t : threads) {
            t.join();
        }

        var endTime = Instant.now();

        System.out.println(results.size() + " requests handled");
        System.out.println("Time: " + Duration.between(startTime, endTime));
    }
}
