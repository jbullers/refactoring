package vthreads;

import java.util.List;
import java.util.concurrent.Executors;

import static vthreads.ContactBook.randomElementOf;

public class ScopeValues {

    interface Context {

        List<Contact> contacts();
    }

    interface Handler {

        Object handle(Context context, String value);
    }

    static class RestrictedContactBook implements Context {

        private static final ScopedValue<String> LAST_NAME = ScopedValue.newInstance();

        private final ContactBook contactBook;
        private final Handler handler;

        RestrictedContactBook(ContactBook contactBook, Handler handler) {
            this.contactBook = contactBook;
            this.handler = handler;
        }

        Object handle(String lastName, String firstName) {
            return ScopedValue.where(LAST_NAME, lastName)
                              .call(() -> handler.handle(this, firstName));
        }

        @Override
        public List<Contact> contacts() {
            return contactBook.findByLastName(LAST_NAME.get());
        }
    }

    public static void main(String[] args) {
        var contactBook = new ContactBook();
        var app = new RestrictedContactBook(
              contactBook,
              (context, value) ->
                    context.contacts().stream()
                           .filter(contact -> contact.firstName().equals(value))
                           .count());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    var lastName = randomElementOf(ContactBook.LAST_NAMES);
                    var firstName = randomElementOf(ContactBook.FIRST_NAMES);
                    System.out.println(Thread.currentThread() + ": Finding " + firstName + " " + lastName);
                    var result = app.handle(lastName, firstName);
                    System.out.println(Thread.currentThread() + ": Found " + result);
                });
            }
        }
    }
}
