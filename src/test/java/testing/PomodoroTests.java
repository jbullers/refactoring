package testing;

import java.time.Duration;
import org.junit.Test;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static testing.PomodoroModel.Session.LONG_BREAK;
import static testing.PomodoroModel.Session.SHORT_BREAK;
import static testing.PomodoroModel.Session.WORK;

public class PomodoroTests {

    @Test
    public void shouldTransitionToShortBreakAfterCountingDownWorkWhenPomodoroCountIsLessThanMax() {
        PomodoroModel model = new PomodoroModel(Duration.of(1, SECONDS),
                                                Duration.ZERO,
                                                Duration.ZERO);
        assertThat(model.session(), is(WORK));

        model.tick(() -> {});

        assertThat(model.session(), is(SHORT_BREAK));
    }

    @Test
    public void shouldTransitionToLongBreakAfterCountingDownWorkWhenPomodoroCountIsMax() {
        PomodoroModel model = new PomodoroModel(Duration.of(1, SECONDS),
                                                Duration.ZERO,
                                                Duration.of(1, SECONDS));
        assertThat(model.session(), is(WORK));
        assertThat(model.pomodorosCompleted(), is(0));

        model.tick(() -> {}); // End 1 Pomodoro
        model.tick(() -> {}); // End Short Break
        model.tick(() -> {}); // End 2 Pomodoros
        model.tick(() -> {}); // End Short Break
        model.tick(() -> {}); // End 3 Pomodoros
        model.tick(() -> {}); // End Short Break
        model.tick(() -> {}); // End 4 Pomodoros

        assertThat(model.session(), is(LONG_BREAK));
    }

    @Test
    public void shouldTransitionToWorkAfterCountingDownShortBreak() {
        PomodoroModel model = new PomodoroModel(Duration.of(1, SECONDS),
                                                Duration.ZERO,
                                                Duration.of(1, SECONDS));
        assertThat(model.session(), is(WORK));

        model.tick(() -> {}); // End 1 Pomodoro
        model.tick(() -> {}); // End Short Break

        assertThat(model.session(), is(WORK));
    }
}
