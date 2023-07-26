package farm;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.String.format;

public class OOPFarm {

    public static void main(String... args) {
        var buster = new Bunny("Buster", 10);
        var babs = new Bunny("Babs", 8);
        var basket = new Basket();
        basket.addVegetableListener(buster::eatVegetable);
        basket.addVegetableListener(babs::eatVegetable);
        var farmer = new Farmer();

        farmer.harvestVegetable(basket, List.of(
              new Lettuce(3),
              new Carrot(2),
              new Lettuce(7),
              new Carrot(5),
              new Lettuce(6)));
    }
}

class Farmer {

    void harvestVegetable(Basket basket, List<Vegetable> vegetables) {
        for (var veg : vegetables) {
            System.out.printf("Putting %s in the basket%n", veg);
            basket.addVegetableToBasket(veg);
        }

        System.out.printf("Harvested %s of vegetables", basket);
    }
}

@SuppressWarnings("ALL")
class Bunny {

    private final String name;
    private final int capacity;
    private int amountEaten;

    Bunny(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    Vegetable eatVegetable(Vegetable vegetable) {
        if (amountEaten < capacity) {
            System.out.printf("%s is eating %s%n", this, vegetable);
            var remainingVegetable = vegetable.eat(capacity - amountEaten);
            amountEaten += vegetable.size() - remainingVegetable.size();
            return remainingVegetable;
        } else {
            System.out.printf("%s is too full to eat any more%n", this);
            return vegetable;
        }
    }

    @Override
    public String toString() {return format("%s Bunny(%d/%d)", name, amountEaten, capacity);}
}

@SuppressWarnings("ALL")
class Basket {

    private int amount;
    private List<VegetableListener> listeners = new ArrayList<>();

    void addVegetableToBasket(Vegetable vegetable) {
        var remainingVegetable = vegetable;
        for (var listener : listeners) {
            remainingVegetable = listener.vegetableAdded(remainingVegetable);
        }
        amount += remainingVegetable.size();
    }

    void addVegetableListener(VegetableListener listener) {listeners.add(listener);}

    @Override
    public String toString() {return format("Basket(%d)", amount);}
}

interface Vegetable {

    int size();

    Vegetable eat(int amount);
}

class Lettuce implements Vegetable {

    private final int size;

    Lettuce(int size) {this.size = size;}

    public int size() {return size;}

    public Lettuce eat(int amount) {return new Lettuce(max(0, size - amount));}

    @Override
    public String toString() {return format("Lettuce(%d)", size);}
}

class Carrot implements Vegetable {

    private final int size;

    Carrot(int size) {this.size = size;}

    @Override
    public int size() {return size;}

    @Override
    public Vegetable eat(int amount) {return new Carrot(0);}

    @Override
    public String toString() {return format("Carrot(%d)", size);}
}

@FunctionalInterface
interface VegetableListener {

    Vegetable vegetableAdded(Vegetable lettuce);
}