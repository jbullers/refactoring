package testing;

import java.time.Duration;
import org.junit.Test;
import testing.PomodoroModel.BreakSessionActive;
import testing.PomodoroModel.BreakSessionEnded;
import testing.PomodoroModel.SessionDurations;
import testing.PomodoroModel.WorkSessionActive;
import testing.PomodoroModel.WorkSessionEnded;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class PomodoroTests {

    @Test
    public void shouldCountDownDurationOnTick() {
        PomodoroModel model = new PomodoroModel(sessionDurations());

        var state = model.tick(model.tick(new WorkSessionActive(0, Duration.of(5, SECONDS))));

        assertThat(state, is(equalTo(new WorkSessionActive(0, Duration.of(3, SECONDS)))));
    }

    @Test
    public void shouldTransitionToShortBreakAfterCountingDownWorkWhenPomodoroCountIsLessThanMax() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), Session.SHORT_BREAK, Duration.of(5, SECONDS)));

        var state = model.tick(new WorkSessionActive(0, Duration.of(1, SECONDS)));
        assertThat(state, is(equalTo(new WorkSessionEnded(1))));
        state = model.tick(state);
        assertThat(state, is(equalTo(new BreakSessionActive(1, Duration.of(5, SECONDS)))));
    }

    @Test
    public void shouldTransitionToLongBreakAfterCountingDownWorkWhenPomodoroCountIsMax() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), Session.LONG_BREAK, Duration.of(10, SECONDS)));

        var state = model.tick(new WorkSessionActive(3, Duration.of(1, SECONDS)));
        assertThat(state, is(equalTo(new WorkSessionEnded(4))));
        state = model.tick(state);
        assertThat(state, is(equalTo(new BreakSessionActive(4, Duration.of(10, SECONDS)))));
    }

    @Test
    public void shouldTransitionToWorkAfterCountingDownShortBreak() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), Session.WORK, Duration.of(3, SECONDS)));

        var state = model.tick(new BreakSessionActive(2, Duration.of(1, SECONDS)));
        assertThat(state, is(equalTo(new BreakSessionEnded(2))));
        state = model.tick(state);
        assertThat(state, is(equalTo(new WorkSessionActive(2, Duration.of(3, SECONDS)))));
    }

    @Test
    public void shouldResetPomodorosAndTransitionToWorkAfterCountingDownLongBreak() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), Session.WORK, Duration.of(3, SECONDS)));

        var state = model.tick(new BreakSessionActive(4, Duration.of(1, SECONDS)));
        assertThat(state, is(equalTo(new BreakSessionEnded(4))));
        state = model.tick(state);
        assertThat(state, is(equalTo(new WorkSessionActive(0, Duration.of(3, SECONDS)))));
    }

    private static SessionDurations sessionDurations() {
        return new SessionDurations(Duration.of(3, SECONDS),
                                    Duration.of(2, SECONDS),
                                    Duration.of(1, SECONDS));
    }

    private enum Session { WORK, SHORT_BREAK, LONG_BREAK }

    private static SessionDurations withDuration(
          SessionDurations sessionDurations, Session session, Duration duration) {
        return switch (session) {
            case WORK -> new SessionDurations(duration,
                                              sessionDurations.longBreakDuration(),
                                              sessionDurations.shortBreakDuration());
            case LONG_BREAK -> new SessionDurations(sessionDurations.workDuration(),
                                                    duration,
                                                    sessionDurations.shortBreakDuration());
            case SHORT_BREAK -> new SessionDurations(sessionDurations.workDuration(),
                                                     sessionDurations.longBreakDuration(),
                                                     duration);
        };
    }
}
