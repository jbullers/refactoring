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

        /// Here, `handle` takes two arguments: `lastName`, which is meant to be part of the "restricted view"
        /// imposed by `RestrictedContactBook`, and `firstName`, which is meant to be passed along to the
        /// downstream handler. `lastName` needs to be conveyed downstream view some mechanism that's separate
        /// from the invocation of the handler. Consider:
        ///
        /// restrictedContactBook.handle --calls-> handler.handle --calls-> restrictedContactBook.contact
        ///          |                                                               ^
        ///          |-----------------lastName needs to be conveyed across----------|
        ///
        /// Moreover, `lastName` needs to be conveyed in such a way that is thread safe: simply storing it as
        /// a field of a `RestrictedContactBook` instance isn't enough, because multiple threads would be
        /// reading/writing that same field concurrently.
        ///
        /// A typical solution here would be to capture the value of `lastName` in a `ThreadLocal` field:
        /// each thread then has its own copy, and they do not interfere with each other. There are a few
        /// downsides to this:
        /// 1. The scope in which `lastName` is set is not particularly well-defined (and it's up to us to
        /// remember to clear the value when we're done)
        /// 2. `ThreadLocal` doesn't restrict when a value can be set (i.e. anywhere the `ThreadLocal` is
        /// available, either `get` or `set` can be called). This makes it more difficult to reason about.
        /// 3. The implementation of `ThreadLocal` isn't particularly efficient in the face of many
        /// virtual threads.
        ///
        /// `ScopedValue` provides an alternative to `ThreadLocal` that conceptually operates in the same
        /// way, while avoiding the aforementioned issues. We first set up some bindings (similar to calling
        /// `set` on a `ThreadLocal`) and then execute some code. Any code executed downstream within the
        /// `ScopedValue.call` have the (read-only) bindings set.
        ///
        /// In the case of this code, if a handler calls `contacts` in its implementation, `LAST_NAME` will be
        /// bound appropriately if and only if the handler is invoked within a `ScopedValue` where a binding has
        /// been provided.
        ///
        /// As another example, consider a situation where we call a web server and ask to display a page.
        /// The page should only display if the user is authenticated, and perhaps displayed a greeting, like
        /// "Hello _username_". We wouldn't want to replicate the authentication logic everywhere, so our page
        /// handler would be downstream of some authentication handler that wraps it:
        ///
        /// AuthenticationHandler --ensures login and calls-> PageHandler
        ///
        /// PageHandler would want to call back to the AuthenticationHandler to get the username to display.
        /// Since we're likely dealing with fairly generic "handler" interfaces here, we can't really provide
        /// that information via the method call itself; it needs to be conveyed externally, and it needs to
        /// be thread safe (our server is processing multiple requests concurrently from different users).
        ///
        /// AuthenticationHandler could set up a `ScopedValue`, binding the logged-in user, thereby making
        /// it available to the downstream handler implementation:
        ///
        /// AuthenticationHandler.handle --calls-> PageHandler.handle --calls-> AuthenticationHandler.loggedInUser
        ///                   |                                                                ^
        ///                   |----------logged-in user is bound and available across----------|
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
