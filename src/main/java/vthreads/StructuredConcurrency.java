package vthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

public class StructuredConcurrency {

    record Toast() {}

    record Coffee() {}

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException _) {
        }
    }

    static void shower() {
        sleep(6_000);
        System.out.println("Shower done");
    }

    static Toast prepareToast() {
        sleep(2_000);
        System.out.println("Toast ready");
        return new Toast();
    }

    static Coffee prepareCoffee() {
        sleep(3_000);
        System.out.println("Coffee ready");
        return new Coffee();
    }

    static Coffee failedCoffee() {
        sleep(1_000);
        throw new RuntimeException("Coffee maker exploded");
    }

    static void eatBreakfast(Toast toast, Coffee coffee) {
        System.out.println("Enjoying " + toast + " and " + coffee);
        sleep(4_000);
        System.out.println("Breakfast done");
    }

    static void headToWork_Sync() {
        shower();
        var toast = prepareToast();
        var coffee = prepareCoffee();
        eatBreakfast(toast, coffee);
    }

    static void headToWork_Executor() throws ExecutionException, InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var showerFuture = executor.submit(StructuredConcurrency::shower);
            var toastFuture = executor.submit(StructuredConcurrency::prepareToast);
            var coffeeFuture = executor.submit(StructuredConcurrency::prepareCoffee);
            //            var coffeeFuture = executor.submit(StructuredConcurrency::failedCoffee);

            showerFuture.get();
            eatBreakfast(toastFuture.get(), coffeeFuture.get());
        }
    }

    static void headToWork_Executor_ExitEarly() throws ExecutionException, InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var completionService = new ExecutorCompletionService<>(executor);
            var showerFuture = completionService.submit(Executors.callable(StructuredConcurrency::shower));
            var toastFuture = completionService.submit(StructuredConcurrency::prepareToast);
            var coffeeFuture = completionService.submit(StructuredConcurrency::prepareCoffee);
            //            var coffeeFuture = completionService.submit(StructuredConcurrency::failedCoffee);

            Toast toast = null;
            Coffee coffee = null;
            for (int i = 0; i < 3; i++) {
                try {
                    var result = completionService.take().get();
                    if (result instanceof Toast t) {
                        toast = t;
                    } else if (result instanceof Coffee c) {
                        coffee = c;
                    }
                } catch (Exception e) {
                    showerFuture.cancel(true);
                    toastFuture.cancel(true);
                    coffeeFuture.cancel(true);
                    throw e;
                }
            }

            eatBreakfast(toast, coffee);
        }
    }

    static void headToWork_Async_ExitEarly() {
        var showerFuture = CompletableFuture.runAsync(StructuredConcurrency::shower);
        var toastFuture = CompletableFuture.supplyAsync(StructuredConcurrency::prepareToast);
        //        var coffeeFuture = CompletableFuture.supplyAsync(StructuredConcurrency::prepareCoffee);
        var coffeeFuture = CompletableFuture.supplyAsync(StructuredConcurrency::failedCoffee);

        showerFuture.exceptionally(t -> {
            toastFuture.completeExceptionally(t);
            coffeeFuture.completeExceptionally(t);
            return null;
        });

        toastFuture.exceptionally(t -> {
            showerFuture.completeExceptionally(t);
            coffeeFuture.completeExceptionally(t);
            return null;
        });

        coffeeFuture.exceptionally(t -> {
            showerFuture.completeExceptionally(t);
            toastFuture.completeExceptionally(t);
            return null;
        });

        showerFuture.thenCompose(_ -> toastFuture.thenAcceptBoth(coffeeFuture, StructuredConcurrency::eatBreakfast))
                    .join();
    }

    static void headToWork_Structured() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var showerFuture = scope.fork(Executors.callable(StructuredConcurrency::shower));
            var toastFuture = scope.fork(StructuredConcurrency::prepareToast);
            //            var coffeeFuture = scope.fork(StructuredConcurrency::prepareCoffee);
            var coffeeFuture = scope.fork(StructuredConcurrency::failedCoffee);

            scope.join().throwIfFailed();

            eatBreakfast(toastFuture.get(), coffeeFuture.get());
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var startTime = Instant.now();

        try {
            //            headToWork_Sync();
            //            headToWork_Executor();
            //            headToWork_Executor_ExitEarly();
            headToWork_Async_ExitEarly();
            //            headToWork_Structured();
        } finally {
            var endTime = Instant.now();
            System.out.println("Total time: " + Duration.between(startTime, endTime));
        }
    }
}
