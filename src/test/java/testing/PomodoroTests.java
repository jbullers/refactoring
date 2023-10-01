package testing;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import testing.PomodoroModel.PomodoroEvent;
import testing.PomodoroModel.PomodoroListener;
import testing.PomodoroModel.Session;
import testing.PomodoroModel.SessionDurations;
import testing.PomodoroModel.SessionEnded;
import testing.PomodoroModel.SessionStarted;
import testing.PomodoroModel.State;
import testing.PomodoroModel.Tick;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static testing.PomodoroModel.Session.LONG_BREAK;
import static testing.PomodoroModel.Session.SHORT_BREAK;
import static testing.PomodoroModel.Session.WORK;

public class PomodoroTests {

    private final CapturingEventListener capturingEventListener = new CapturingEventListener();

    @Test
    public void shouldCountDownDurationOnTick() {
        PomodoroModel model = new PomodoroModel(
              sessionDurations(),
              new State(0, WORK, Duration.of(5, SECONDS)));
        model.registerPomodoroListener(capturingEventListener);

        model.tick();
        model.tick();

        assertThat(capturingEventListener.events, contains(List.of(
              is(equalTo(new SessionStarted(new State(0, WORK, Duration.of(5, SECONDS))))),
              is(equalTo(new Tick(Duration.of(4, SECONDS)))),
              is(equalTo(new Tick(Duration.of(3, SECONDS)))))));
    }

    @Test
    public void shouldTransitionToShortBreakAfterCountingDownWorkWhenPomodoroCountIsLessThanMax() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), SHORT_BREAK, Duration.of(5, SECONDS)),
              new State(0, WORK, Duration.of(1, SECONDS)));
        model.registerPomodoroListener(capturingEventListener);

        model.tick();

        assertThat(capturingEventListener.events, contains(List.of(
              is(equalTo(new SessionStarted(new State(0, WORK, Duration.of(1, SECONDS))))),
              is(instanceOf(SessionEnded.class)),
              is(equalTo(new SessionStarted(new State(1, SHORT_BREAK, Duration.of(5, SECONDS))))))));
    }

    @Test
    public void shouldTransitionToLongBreakAfterCountingDownWorkWhenPomodoroCountIsMax() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), LONG_BREAK, Duration.of(10, SECONDS)),
              new State(3, WORK, Duration.of(1, SECONDS)));
        model.registerPomodoroListener(capturingEventListener);

        model.tick();

        assertThat(capturingEventListener.events, contains(List.of(
              is(equalTo(new SessionStarted(new State(3, WORK, Duration.of(1, SECONDS))))),
              is(instanceOf(SessionEnded.class)),
              is(equalTo(new SessionStarted(new State(4, LONG_BREAK, Duration.of(10, SECONDS))))))));
    }

    @Test
    public void shouldTransitionToWorkAfterCountingDownShortBreak() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), WORK, Duration.of(3, SECONDS)),
              new State(2, SHORT_BREAK, Duration.of(1, SECONDS)));
        model.registerPomodoroListener(capturingEventListener);

        model.tick();

        assertThat(capturingEventListener.events, contains(List.of(
              is(equalTo(new SessionStarted(new State(2, SHORT_BREAK, Duration.of(1, SECONDS))))),
              is(instanceOf(SessionEnded.class)),
              is(equalTo(new SessionStarted(new State(2, WORK, Duration.of(3, SECONDS))))))));
    }

    @Test
    public void shouldResetPomodorosAndTransitionToWorkAfterCountingDownLongBreak() {
        PomodoroModel model = new PomodoroModel(
              withDuration(sessionDurations(), WORK, Duration.of(3, SECONDS)),
              new State(4, LONG_BREAK, Duration.of(1, SECONDS)));
        model.registerPomodoroListener(capturingEventListener);

        model.tick();

        assertThat(capturingEventListener.events, contains(List.of(
              is(equalTo(new SessionStarted(new State(4, LONG_BREAK, Duration.of(1, SECONDS))))),
              is(instanceOf(SessionEnded.class)),
              is(equalTo(new SessionStarted(new State(0, WORK, Duration.of(3, SECONDS))))))));
    }

    private static SessionDurations sessionDurations() {
        return new SessionDurations(Duration.of(3, SECONDS),
                                    Duration.of(2, SECONDS),
                                    Duration.of(1, SECONDS));
    }

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

    private static class CapturingEventListener implements PomodoroListener {

        final List<PomodoroEvent> events = new ArrayList<>();

        @Override
        public void stateChanged(PomodoroEvent event) {
            events.add(event);
        }
    }
}
