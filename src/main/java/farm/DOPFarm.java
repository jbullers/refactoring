package farm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.max;

public class DOPFarm {

    public static void main(String[] args) {
        var farmStates = new ArrayDeque<Farm>();
        farmStates.add(new Farm(
              new Basket(0),
              List.of(
                    new Bunny("Buster", 10, 0),
                    new Bunny("Babs", 8, 0))));

        var vegetables = List.of(
              new Lettuce(3),
              new Carrot(2),
              new Lettuce(7),
              new Carrot(5),
              new Lettuce(6));

        for (var veg : vegetables) {
            farmStates.addAll(harvestVegetable(farmStates.getLast(), veg));
        }

        for (var farm : farmStates) {
            System.out.printf("%s%n", farm);
        }
    }

    static Collection<Farm> harvestVegetable(Farm farm, Vegetable vegetable) {
        var updatedFarms = new ArrayDeque<Farm>();
        var remainingVegetable = vegetable;
        var updatedBunnies = new ArrayList<>(farm.bunnies());
        for (int i = 0; i < updatedBunnies.size(); i++) {
            var bunny = updatedBunnies.get(i);
            if (bunny.amountEaten() < bunny.capacity()) {
                var previousRemainingVegetable = remainingVegetable;
                remainingVegetable = eat(remainingVegetable, bunny.capacity() - bunny.amountEaten());
                updatedBunnies.set(i, bunny.eating(previousRemainingVegetable.size() - remainingVegetable.size()));
                updatedFarms.add(new Farm(farm.basket(), List.copyOf(updatedBunnies)));
            }

            if (remainingVegetable.size() == 0) return updatedFarms;
        }

        updatedFarms.add(new Farm(
              farm.basket().adding(remainingVegetable.size()),
              updatedFarms.getLast().bunnies()));

        return updatedFarms;
    }

    static Vegetable eat(Vegetable vegetable, int maxAmount) {
        return switch (vegetable) {
            case Lettuce(int size) -> new Lettuce(max(0, size - maxAmount));
            case Carrot c -> new Carrot(0);
        };
    }

    record Basket(int amount) {

        Basket adding(int newAmount) {
            return new Basket(amount + newAmount);
        }
    }

    record Bunny(String name, int capacity, int amountEaten) {

        Bunny eating(int amount) {
            return new Bunny(name, capacity, amountEaten + amount);
        }
    }

    sealed interface Vegetable {

        int size();
    }

    record Lettuce(int size) implements Vegetable {}

    record Carrot(int size) implements Vegetable {}

    record Farm(Basket basket, List<Bunny> bunnies) {}
}
