package testing;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class StackTests {

    @Test
    public void shouldReturnNullWhenPeekingAnEmptyStack() {
        var stack = Stack.<String>empty();

        assertThat(stack.peek(), is(nullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenPoppingAnEmptyStack() {
        Stack.empty().pop();
    }

    @Test
    public void shouldPeekTheLastAddedItem() {
        var stack = Stack.<String>empty()
                         .push("Hello");

        assertThat(stack.peek(), is("Hello"));
    }

    @Test
    public void shouldReturnANewStackLessTheTopItemWhenPopped() {
        var stack = Stack.<String>empty()
                         .push("Hello")
                         .push("World");

        var poppedStack = stack.pop();

        assertThat(poppedStack.peek(), is("Hello"));
    }
}