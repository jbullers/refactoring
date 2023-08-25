package testing;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * An immutable FIFO data structure.
 * <p>
 * Create a stack with {@code Stack.&lt;Type&gt;empty()} and add elements using {@link #push(E)}. The top of the stack
 * can be retrieved by calling {@link #peek()}, and the top-most element can be removed by calling {@link #pop()}.
 * Note that since this stack implementation is immutable, both {@code peek()} and {@code pop()} must be called to
 * remove and retain a reference to the top-most stack element.
 *
 * @param <E> the type of elements in this stack
 */
public class Stack<E> {

    private static final Stack<?> EMPTY = new Stack<>();

    /**
     * @param <E> the type of elements in this stack
     *
     * @return an empty stack to hold elements of the desired type.
     */
    @SuppressWarnings("unchecked")
    public static <E> Stack<E> empty() {
        return (Stack<E>) EMPTY;
    }

    private List<E> elements = new ArrayList<>();

    private Stack() {}

    /**
     * @param element the element to push
     *
     * @return a new stack containing the given element
     */
    @CheckReturnValue
    public Stack<E> push(@Nonnull E element) {
        var stack = new Stack<E>();
        stack.elements = new ArrayList<>(this.elements);
        stack.elements.add(requireNonNull(element, "Attempted to add null element to stack"));
        return stack;
    }

    /**
     * @return the top element on the stack
     */
    public @Nullable E peek() {
        return elements.get(elements.size() - 1);
    }

    /**
     * @return a new stack containing all but the top element
     */
    public @Nonnull Stack<E> pop() {
        var stack = new Stack<E>();
        stack.elements = new ArrayList<>(this.elements);
        stack.elements.remove(elements.size() - 1);
        return stack;
    }

    @Override
    public String toString() {
        return "Stack{" +
              "elements=" + elements +
              '}';
    }

    public static void main(String[] args) {
        var stack = Stack.<String>empty()
                         .push("Hello")
                         .push("World");

        System.out.println(stack.peek());
        System.out.println(stack.pop());
    }
}
