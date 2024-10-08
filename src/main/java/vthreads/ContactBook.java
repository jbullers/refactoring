package vthreads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class ContactBook {

    static final String EMAIL_FORMAT = "%s.%s@%s";

    static final List<String> FIRST_NAMES = List.of("Albert", "Bob", "Chris", "Dave", "Fred", "George",
                                                    "Henry", "Jason", "Joe", "Moe", "Nate", "Peter", "Stan", "William");
    static final List<String> LAST_NAMES = List.of("Black", "Bullers", "Doe", "Hamilton", "Johnson", "Peterson",
                                                   "Richards", "Ryker", "Smith", "Thomson", "Wilson", "White");
    static final List<String> EMAIL_DOMAINS = List.of("gmail.com", "hotmail.com", "ec.gc.ca");

    static String randomElementOf(List<String> elements) {
        int index = ThreadLocalRandom.current().nextInt(elements.size());
        return elements.get(index);
    }

    static Contact randomContact() {
        var firstName = randomElementOf(FIRST_NAMES);
        var lastName = randomElementOf(LAST_NAMES);
        var emailDomain = randomElementOf(EMAIL_DOMAINS);
        return new Contact(firstName, lastName, EMAIL_FORMAT.formatted(firstName, lastName, emailDomain));
    }

    private final List<Contact> contacts = new ArrayList<>();

    ContactBook() {
        // Generate some data
        for (int i = 0; i < 100; i++) {
            contacts.add(randomContact());
        }
    }

    List<Contact> findByLastName(String lastName) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException _) {}

        return contacts.stream()
                       .filter(contact -> contact.lastName().equals(lastName))
                       .toList();
    }
}
