package refactor;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ArgsTest {

    @Test
    public void shouldAllowForMultipleBooleanArgsWithASingleHyphen() throws Exception {
        Args args = new Args("a,b,c", new String[] { "-ac" });

        assertThat(args.getBoolean('a'), is(true));
        assertThat(args.getBoolean('b'), is(false));
        assertThat(args.getBoolean('c'), is(true));
    }

    @Test
    public void shouldAllowForMultipleArgCharactersWithASingleHyphen() throws Exception {
        Args args = new Args("n#,b,s*", new String[] { "-nbs", "10", "Foo" });

        assertThat(args.getInt('n'), is(10));
        assertThat(args.getBoolean('b'), is(true));
        assertThat(args.getString('s'), is("Foo"));
    }

    @Test
    public void shouldAllowParseMultipleArgs() throws Exception {
        Args args = new Args("n#,b,s*", new String[] { "-n", "10", "-b", "-s", "Foo" });

        assertThat(args.getInt('n'), is(10));
        assertThat(args.getBoolean('b'), is(true));
        assertThat(args.getString('s'), is("Foo"));
    }
}