package jeps;

import javax.annotation.Nullable;

// https://openjdk.org/projects/jdk/22/
// https://openjdk.org/projects/jdk/23/
public class JdkFuture {

    // Unnamed Variables and Patterns
    // https://openjdk.org/jeps/456

    // Allows both the type and name of a variable in a pattern to be declared as ignored
    // Variable names can also be replaced by _ when they are unused (compiler enforces this)
    // _ has semantic meaning understood by the compiler, so there's no issue with name reuse
    static class Unnamed {

        record PhoneNumber(String phoneNumber) {}
        record Email(String email) {}
        record ContactInfo(@Nullable PhoneNumber phoneNumber, @Nullable Email email) {}
        record Customer(int id, String name, @Nullable ContactInfo contactInfo) {}

        static void sendEmailPromo(Customer customer) {
            if (customer instanceof Customer(_, _, ContactInfo(_, Email(String email)))) {
                System.out.println("Sending promo to " + email);
            }
        }

        public static void main(String[] args) {
            sendEmailPromo(new Customer(0,
                                        "Jason",
                                        new ContactInfo(
                                              new PhoneNumber("416-123-4567"),
                                              new Email("jason.bullers@gmail.com"))));

            try {
                Thread.sleep(1);
            } catch (InterruptedException _) {
                // Do nothing
            }
        }
    }

    // Primitive Types in Patterns, instanceof, and switch
    // https://openjdk.org/jeps/455

    // Allows primitive types to participate in the same places type and record patterns can
    static class PrimitivePatterns {

        static String drinkForAge(int age) {
            return switch (age) {
                case int i when i < 6 -> "Juice";
                case int i when i < 19 -> "Pop";
                case int i when i == 19 -> "Fancy";
                case int i -> "Alcohol";
            };
        }

        public static void main(String[] args) {
            System.out.println(drinkForAge(4));
            System.out.println(drinkForAge(19));
        }
    }

    // Stream Gatherers
    // https://openjdk.org/jeps/473



    // Flexible Constructor Bodies
    // https://openjdk.org/jeps/482



    // Markdown Documentation Comments
    // https://openjdk.org/jeps/467

    // If you've ever written Markdown and have also written JavaDoc that needs to include
    // lists or tables, you already know what's good about this :)
    // IntelliJ doesn't support it yet, so check out the JEP


    // Implicitly Declared Classes and Instance Main Methods
    // https://openjdk.org/jeps/477

    // See HelloWorld.java


    // Module Import Declarations
    // https://openjdk.org/jeps/476

    // Mainly for a smoother on ramp, just like the above JEP
    // You can now effectively do wildcard imports like `import java.util.*` at the module level:
    // import module java.base; for java.lang, java.util, etc.
    // import module java.desktop; for Swing, AWT

    // Implicitly declared classes `import module java.base` automatically
}
