package testing;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class PomodoroModel {

    sealed interface PomodoroEvent {}
    record SessionStarted(State state) implements PomodoroEvent {}
    record Tick(Duration duration) implements PomodoroEvent {}
    record SessionEnded() implements PomodoroEvent {}

    @FunctionalInterface
    interface PomodoroListener {
        void stateChanged(PomodoroEvent event);
    }

    static final int MAX_WORK_POMODOROS = 4;
    enum Session { WORK, SHORT_BREAK, LONG_BREAK }
    record SessionDurations(Duration workDuration, Duration longBreakDuration, Duration shortBreakDuration) {}
    record State(int pomodorosCompleted, Session session, Duration currentDuration) {}

    private final SessionDurations sessionDurations;
    private State state;

    private PomodoroListener listener;

    PomodoroModel(SessionDurations sessionDurations) {
        this(sessionDurations, new State(0, Session.WORK, sessionDurations.workDuration()));
    }
    PomodoroModel(SessionDurations sessionDurations, State state) {
        this.sessionDurations = sessionDurations;
        this.state = state;
    }

    void registerPomodoroListener(PomodoroListener listener) {
        this.listener = listener;
        fireEvent(new SessionStarted(state));
    }

    private void fireEvent(PomodoroEvent event) {
        if (listener != null) {
            listener.stateChanged(event);
        }
    }

    void tick() {
        var updatedDuration = state.currentDuration().minus(1, ChronoUnit.SECONDS);
        if (updatedDuration.isZero()) {
            fireEvent(new SessionEnded());
            state = switch (state.session()) {
                case WORK -> {
                    var updatedPomodoros = state.pomodorosCompleted() + 1;
                    yield updatedPomodoros == MAX_WORK_POMODOROS ?
                          new State(updatedPomodoros, Session.LONG_BREAK, sessionDurations.longBreakDuration()) :
                          new State(updatedPomodoros, Session.SHORT_BREAK, sessionDurations.shortBreakDuration());
                }
                case SHORT_BREAK ->
                      new State(state.pomodorosCompleted(), Session.WORK, sessionDurations.workDuration());
                case LONG_BREAK ->
                      new State(0, Session.WORK, sessionDurations.workDuration());
            };
            fireEvent(new SessionStarted(state));
        } else {
            state = new State(state.pomodorosCompleted(), state.session(), updatedDuration);
            fireEvent(new Tick(updatedDuration));
        }
    }
}
