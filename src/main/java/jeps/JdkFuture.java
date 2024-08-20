package jeps;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Gatherer;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.toSet;

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

    // Provides an extension point to the Streams API similar to Collectors,
    // but for intermediate operations.
    static class StreamGatherers {

        record Person(String firstName, String lastName, int age) {}

        public static void main(String[] args) {
            var people = List.of(
                  new Person("Mary", "Smith", 28),
                  new Person("Jason", "Bullers", 37),
                  new Person("Jane", "Doe", 17),
                  new Person("Cael", "Bullers", 3),
                  new Person("John", "Doe", 16),
                  new Person("John", "Smith", 30),
                  new Person("Tim", "Smith", 2));

            // Single adult from each family
//            people.stream()
//                  .filter(person -> person.age() >= 18)
//                  .distinctBy(Person::lastName)
//                  .collect(toSet());

            record DistinctByLastName(Person person) {

                @Override
                public boolean equals(Object o) {
                    return o instanceof DistinctByLastName other &&
                           this.person().lastName().equals(other.person().lastName());
                }

                @Override
                public int hashCode() {
                    return Objects.hashCode(person().lastName());
                }
            }

            System.out.println(people.stream()
                                     .filter(person -> person.age() >= 18)
                                     .map(DistinctByLastName::new)
                                     .distinct()
                                     .map(DistinctByLastName::person)
                                     .collect(toSet()));

            System.out.println(people.stream()
                                     .filter(person -> person.age() >= 18)
                                     .gather(distinctBy(Person::lastName))
                                     .collect(toSet()));
        }

        static Gatherer<Person, Set<String>, Person> distinctBy(Function<Person, String> property) {
            return Gatherer.ofSequential(HashSet::new,
                                         (state, element, downstream) -> {
                                             if (state.add(property.apply(element))) {
                                                 return downstream.push(element);
                                             } else {
                                                 return true;
                                             }
                                         });
        }
    }

    // Flexible Constructor Bodies
    // https://openjdk.org/jeps/482

    // Removes the constraint on constructors that the first thing must be
    // a call to another constructor or to super.
    // This allows validation of arguments to occur before the super constructor is invoked
    // without having to create separate static methods to do the job.
    // It also helps resolve a longstanding problem where constructors of base classes
    // should not call methods that are overridable by derived classes.
    static class FlexibleConstructors {

        static class Employee {

            private final int id;
            private final String name;

            Employee(int id, String name) {
                // Some expensive set up, database stuff, whatever
                this.id = id;
                this.name = name;
                validate();
            }

            protected void validate() {
                if (id <= 0) throw new IllegalStateException("Employee ID must be >= 1");
            }
        }

        static class Manager extends Employee {

            private final Set<Employee> directReports;

            Manager(int id, String name, Set<Employee> directReports) {
                // Before
//                super(id, name);
//                this.directReports = Set.copyOf(directReports);

                // After
                this.directReports = Set.copyOf(directReports);
                super(id, name);
            }

            @Override
            protected void validate() {
                super.validate();
                if (directReports.isEmpty())
                    throw new IllegalStateException("Must have at least one direct report");
            }
        }

        public static void main(String[] args) {
//            new Manager(1, "Jason", Set.of());
            new Manager(2, "Michel", Set.of(new Employee(3, "Brendan")));
        }
    }

    // Markdown Documentation Comments
    // https://openjdk.org/jeps/467

    // If you've ever written Markdown and have also written JavaDoc that needs to include
    // lists or tables, you already know what's good about this :)
    // IntelliJ doesn't support it yet, so check out the JEP


    // Implicitly Declared Classes and Instance Main Methods
    // https://openjdk.org/jeps/477

// HelloWorld.java
//    String greetingFor(String name) {
//        return "Hello " + name;
//    }
//
//    void main() {
//        for (var name : List.of("Jason", "Fred", "World")) {
//            println(greetingFor(name));
//        }
//    }


    // Module Import Declarations
    // https://openjdk.org/jeps/476

    // Mainly for a smoother on ramp, just like the above JEP
    // You can now effectively do wildcard imports like `import java.util.*` at the module level:
    // import module java.base; for java.lang, java.util, etc.
    // import module java.desktop; for Swing, AWT

    // Implicitly declared classes `import module java.base` automatically
}
