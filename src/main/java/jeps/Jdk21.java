package jeps;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import javax.annotation.Nullable;

// https://openjdk.org/projects/jdk/21/jeps-since-jdk-17
public class Jdk21 {

    // Record Patterns
    // https://openjdk.java.net/jeps/440

    // Extends pattern matching beyond simple type patterns as introduced with JEP 394 (instanceof)
    // Record patterns match and destructure
    static class RecordPatterns {

        record PhoneNumber(String phoneNumber) {}
        record Email(String email) {}
        record ContactInfo(@Nullable PhoneNumber phoneNumber, @Nullable Email email) {}
        record Customer(int id, String name, @Nullable ContactInfo contactInfo) {}

        static void sendEmailPromo(Customer customer) {
            // Before
            var contactInfo = customer.contactInfo();
            if (contactInfo != null) {
                var email = contactInfo.email();
                if (email != null) {
                    System.out.println("Sending promo to " + email.email());
                }
            }

            // or, to remove the verbose null checks and intermediate variables
            Optional.ofNullable(customer.contactInfo())
                    .map(ContactInfo::email)
                    .ifPresent(email -> System.out.println("Sending promo to " + email.email()));

            // After
            if (customer instanceof Customer(var _id, var _name, ContactInfo(var _number, Email(String email)))) {
                System.out.println("Sending promo to " + email);
            }
        }

        public static void main(String[] args) {
            sendEmailPromo(new Customer(0,
                                        "Jason",
                                        new ContactInfo(
                                              new PhoneNumber("416-123-4567"),
                                              new Email("jason.bullers@gmail.com"))));

            sendEmailPromo(new Customer(1,
                                        "Fred",
                                        new ContactInfo(
                                              new PhoneNumber("416-123-4567"),
                                              null)));

            sendEmailPromo(new Customer(0, "George", null));
        }
    }

    // Pattern Matching for switch
    // https://openjdk.java.net/jeps/441

    // Now we start to see some payoff from records, sealed classes, and switch expressions
    // These JEPs were designed to work together and enable "data-oriented" programming,
    // which is basically a more functional paradigm in which data is treated as data
    // and functions are written against a particular data model.
    // Think DMS decoders or PGs, where the name of the game is data transformation.
    static class PatternMatching {

        sealed interface Geometry {}
        record Point(int x, int y) implements Geometry {}
        record LineSegment(Point start, Point end) implements Geometry {}
        record Polygon(List<LineSegment> segments) implements Geometry {
            Polygon {
                segments = List.copyOf(segments);
            }
        }

        static <T extends Geometry> T translate(T geometry, int xDelta, int yDelta) {
            return (T) switch (geometry) {
                case Polygon(var segments) ->
                      new Polygon(segments.stream()
                                          .map(segment -> translate(segment, xDelta, yDelta))
                                          .toList());

                case LineSegment(var start, var end) ->
                      new LineSegment(translate(start, xDelta, yDelta),
                                      translate(end, xDelta, yDelta));

                case Point(int x, int y) -> new Point(x + xDelta, y + yDelta);
            };
        }

        public static void main(String[] args) {
            var triangle = new Polygon(List.of(
                  new LineSegment(new Point(0, 0), new Point(0, 5)),
                  new LineSegment(new Point(0, 5), new Point(7, 0)),
                  new LineSegment(new Point(7, 0), new Point(0, 0))));
            System.out.println(triangle);

            System.out.println(translate(triangle, 10, 10));
        }
    }

    // Sequenced Collections
    // https://openjdk.java.net/jeps/431

    // A pretty simple one: new collections interfaces so that the notion of a
    // "sequenced collection" (i.e. one in which the first and last elements are well-defined)
    // can be expressed through the type system.
    // Caveat: like the rest of the collections API, methods can throw unsupported exceptions
    static class SequencedCollections {

        record Point(int x, int y) {}

        static boolean isClosed(List<Point> line) {
            // Before
            // Note that now, List is a SequenceCollection
            return line.get(0).equals(line.get(line.size() - 1));
        }

        static boolean isClosed2(SequencedCollection<Point> line) {
            return line.getFirst().equals(line.getLast());
        }

        public static void main(String[] args) {
            var line = List.of(
                  new Point(0, 0),
                  new Point(10, 10),
                  new Point(10, 0),
                  new Point(0, 0));
            System.out.println(isClosed(line));
            System.out.println(isClosed2(line));
        }
    }

    // UTF-8 by Default
    // https://openjdk.java.net/jeps/400

    // I/O and Strings have historically been a pretty big foot gun when dealing with different encodings
    // (especially common because of language differences, where certain character sets are required)
    // Most of the relevant constructors/methods have always had an overload where the encoding could
    // be specified, but if it was elided, then the system default would be used.
    // And that means it can vary from system to system.
    static class Encoding {

        public static void main(String[] args) throws IOException {
            // Serialized data. Clipboard, object sets, product generators, etc.
            byte[] serializedData = new byte[0];

            // Before, this was system dependent deserialization
            // Can lead to data corruption
            var deserializedData = new String(serializedData);
            var reader = new FileReader("data-file");

            // Better, but easy to forget
            deserializedData = new String(serializedData, StandardCharsets.UTF_8);
            reader = new FileReader("data-file", StandardCharsets.UTF_8);

            // But now, this implies UTF-8
            deserializedData = new String(serializedData);
            reader = new FileReader("data-file");

            // The JEP explains further how it works
            // and how to override the new behaviour with legacy behaviour, if needed
        }
    }

    // Code Snippets in Java API Documentation
    // https://openjdk.java.net/jeps/413

    // Writing code snippets in JavaDoc was always a pain:
    // Formatting wasn't easy, you had to fight with <pre> and @code tags
    // Now, there's first class support for it, and it benefits from syntax highlighting!
    static class CodeSnippets {

        /**
         * Shouts the given phrases by converting them to all caps. For example:
         * <p>
         * The old way:
         * <pre>{@code
         * var phrases = List.of("hello world", "how are you?", "this is great!");
         * shout(phrases); // List.of("HELLO WORLD", "HOW ARE YOU?", "THIS IS GREAT!")
         * }</pre>
         *
         * The new way:
         * {@snippet :
         * var phrases = List.of("hello world", "how are you?", "this is great!");
         * shout(phrases); // List.of("HELLO WORLD", "HOW ARE YOU?", "THIS IS GREAT!")
         * }
         *
         * There are other features too, like highlighting regions. Check out the JEP
         *
         * @param phrases the phrases to be capitalized
         * @return the capitalized phrases
         */
        static List<String> shout(List<String> phrases) {
            return phrases.stream().map(String::toUpperCase).toList();
        }
    }

    // Deprecate Finalization for Removal
    // https://openjdk.org/jeps/421

    // Probably not used by anyone here, but I recall it being used by AL
    // Finalize is going away because of how fiddly and unreliable it is
    // The JEP details the problems and suggests alternatives
}
