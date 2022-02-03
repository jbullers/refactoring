package refactor;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ArgsTest {

    @Test
    public void shouldAllowForMultipleBooleanArgsWithASingleHyphen() throws Exception {
        Args args = new Args("a,b,c", new String[]{"-ac"});

        assertThat(args.get('a', Boolean.class, false), is(true));
        assertThat(args.get('b', Boolean.class, false), is(false));
        assertThat(args.get('c', Boolean.class, false), is(true));
    }

    @Test
    public void shouldAllowForMultipleArgCharactersWithASingleHyphen() throws Exception {
        Args args = new Args("n#,b,s*", new String[]{"-nbs", "10", "Foo"});

        assertThat(args.get('n', Integer.class, 0), is(10));
        assertThat(args.get('b', Boolean.class, false), is(true));
        assertThat(args.get('s', String.class, ""), is("Foo"));
    }

    @Test
    public void shouldAllowParseMultipleArgs() throws Exception {
        Args args = new Args("n#,b,s*", new String[]{"-n", "10", "-b", "-s", "Foo"});

        assertThat(args.get('n', Integer.class, 0), is(10));
        assertThat(args.get('b', Boolean.class, false), is(true));
        assertThat(args.get('s', String.class, ""), is("Foo"));
    }
}