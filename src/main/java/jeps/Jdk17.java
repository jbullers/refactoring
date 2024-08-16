package jeps;

import java.awt.Color;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.intellij.lang.annotations.Language;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;

// https://openjdk.org/projects/jdk/17/jeps-since-jdk-11
public class Jdk17 {

    // Pattern Matching for instanceof
    // https://openjdk.java.net/jeps/394

    // Removes redundancy of casting and introducing a new local variable
    // where instanceof checks are necessary
    static class InstanceOf {

        public static void main(String[] args) {
            // Before
            var obj = (Object) "Hello World!";
            if (obj instanceof String) {
                String theString = (String) obj;
                if (theString.contains("World")) {
                    System.out.println(theString);
                } else {
                    System.out.println("Nope");
                }
            } else {
                System.out.println("Nope");
            }

            // After
            if (obj instanceof String theString && theString.contains("World")) {
                System.out.println(theString);
            } else {
                // theString isn't in scope here
                System.out.println("Nope");
            }
            // theString isn't in scope here

            // Also:
            if (!(obj instanceof String theString)) throw new IllegalArgumentException("Invalid");
            System.out.println(theString.toUpperCase());
        }
    }

    // Records
    // https://openjdk.java.net/jeps/395

    // For modeling transparent data containers that are (by default) value equal
    // Also sometimes called AND types, intersection types, or product types
    // Nice side effect: boilerplate reduction, which helps drive adoption
    static class RecordTypes {

        record Person(String name, int age, Set<Color> favoriteColors) {}

        public static void main(String[] args) {
            // Instances automatically have a default equals method based on all the fields
            var jason = new Person("Jason", 37, Set.of(BLUE, ORANGE));
            assert jason.equals(new Person("Jason", 37, Set.of(BLUE, ORANGE)));

            // Instances have a reasonable default toString impl
            System.out.println(jason);

            // Records can be defined in various scopes, including local to a method
            record Vehicle(String make, String model) {}

            // Records also have a default hashcode implementation based on all their fields
            // Since records are meant to be immutable and have stable hashcodes, they're safe as map keys
            var personToVehicle = Map.of(jason, new Vehicle("Mazda", "CX-5"));
            System.out.println(personToVehicle.get(new Person("Jason", 37, Set.of(BLUE, ORANGE))));

            // But!
            // Records are only shallowly immutable:
            Set<Color> fredColors = new HashSet<>();
            fredColors.add(GREEN);
            var fred = new Person("Fred", 42, fredColors);
            System.out.println(fred);
            fredColors.add(ORANGE);
            fred.favoriteColors().add(BLUE);
            System.out.println(fred);

            // It's up to you to make appropriate defensive copies:
            record Person2(String name, int age, Set<Color> favoriteColors) {
                Person2 {
                    favoriteColors = Set.copyOf(favoriteColors);
                }
            }

            fredColors.clear();
            fredColors.add(GREEN);
            var fred2 = new Person2("Fred", 42, fredColors);
            System.out.println(fred);
            // BOOM!
//             fred2.favoriteColors().add(BLUE);
        }
    }

    // Sealed Classes
    // https://openjdk.java.net/jeps/409

    // For controlling the allowed subclasses of a particular class/interface
    // Also sometimes called OR types, union types, or sum types
    // Allows modeling alternatives in a similar way to enums:
    // enums allow a fixed set of instances of a given type,
    // while sealed classes allow a fixed set of types within a hierarchy
    static class SealedTypes {

        record TranslatedText(String english, String french) {}

        sealed interface WeatherNarrative {}
        record DraftWeatherNarrative(String author, String text) implements WeatherNarrative {}
        record PublishedWeatherNarrative(String author, String text, Instant publishedAt) implements WeatherNarrative {}
        record TranslatedWeatherNarrative(String author, TranslatedText text, Instant publishedAt) implements WeatherNarrative {}

        static PublishedWeatherNarrative publish(DraftWeatherNarrative draft) {
            return new PublishedWeatherNarrative(draft.author(), draft.text(), Instant.now());
        }

        static TranslatedWeatherNarrative translate(PublishedWeatherNarrative published) {
            return new TranslatedWeatherNarrative(
                  published.author(),
                  new TranslatedText(published.text(), published.text()),
                  published.publishedAt());
        }

        public static void main(String[] args) {
            var weatherNarrative = new DraftWeatherNarrative("Jason", "Head for the hills!");
//            translate(weatherNarrative);
            var publishedWeatherNarrative = publish(weatherNarrative);
//            publish(publishedWeatherNarrative);
            WeatherNarrative translatedWeatherNarrative = translate(publishedWeatherNarrative);

            if (translatedWeatherNarrative instanceof TranslatedWeatherNarrative narrative &&
                narrative.text().english().contains("hills")) {
                System.out.println("Yup");
            }
        }
    }

    // Switch Expressions
    // https://openjdk.java.net/jeps/361

    // Enables switch to evaluate to the value of the matching case
    // Avoids the need to introduce a local variable and remember to assign and break for every case,
    // or to lift a switch out to its own function where each case returns
    static class SwitchExpressions {

        enum DayOfWeek {
            MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
        }

        enum PayRate {
            REGULAR, TIME_AND_A_HALF, DOUBLE_TIME;
        }

        // Before
        static PayRate payRateFor(DayOfWeek day) {
            switch (day) {
                // Intentional fallthrough!
                case MONDAY:
                case TUESDAY:
                case WEDNESDAY:
                case THURSDAY:
                case FRIDAY:
                    return PayRate.REGULAR;
                case SATURDAY:
                    return PayRate.TIME_AND_A_HALF;
                case SUNDAY:
                    return PayRate.DOUBLE_TIME;
                default:
                    // ??
                    return null;
            }
        }

        static void pay(String employee, DayOfWeek day) {
            var payRate = payRateFor(day);
            System.out.printf("Paying %s %s on %s%n", employee, payRate, day);
        }

        static void pay2(String employee, DayOfWeek day) {
            // Switch expressions are checked for exhaustiveness by the compiler
            PayRate payRate = switch (day) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> PayRate.REGULAR;
                case SATURDAY -> PayRate.TIME_AND_A_HALF;
                case SUNDAY -> PayRate.DOUBLE_TIME;
            };
            // Can explicitly 'yield' if right side of expression is a block

            System.out.printf("Paying %s %s on %s%n", employee, payRate, day);
        }

        public static void main(String[] args) {
            pay("Jason", DayOfWeek.TUESDAY);
            pay2("Jason", DayOfWeek.SUNDAY);
        }
    }


    // Text Blocks
    // https://openjdk.java.net/jeps/378

    // Allows multiline, preformatted text
    // Doesn't require escaping of "" and explicit \n like normal strings would
    // Useful for formatted text output, or for embedded JSON (e.g. in test code)
    static class TextBlocks {

        record Contact(String firstName, String lastName, String email) {

            @Override
            public String toString() {
                var template = """
                      CONTACT
                      Name: %s, %s
                      Email: %s""";
                return String.format(template, lastName, firstName, email);
            }
        }

        public static void main(String[] args) {
            System.out.println(new Contact("Jason", "Bullers", "jason.bullers@gmail.com"));

            @Language("JSON")
            var payload = """
                  {
                    "name": "Jason Bullers",
                    "age": 37,
                    "favoriteColors": ["ORANAGE", "BLUE"]
                  }""";

            System.out.println(payload);
        }
    }


    // Packaging Tool
    // https://openjdk.java.net/jeps/392

    // A command line utility that helps with the creation of distributable packages
    // Can create Windows exes, Linux pkg or deb files, etc.
    // If not already being used, maybe a nice simplification for NinJo packaging processes
}
