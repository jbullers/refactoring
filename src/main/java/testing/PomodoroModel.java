package testing;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class PomodoroModel {

    static final int MAX_WORK_POMODOROS = 4;

    record SessionDurations(Duration workDuration, Duration longBreakDuration, Duration shortBreakDuration) {}

    sealed interface State {}
    sealed interface ActiveSession extends State {}
    sealed interface EndedSession extends State {}
    record WorkSessionActive(int pomodorosCompleted, Duration currentDuration) implements ActiveSession {}
    record WorkSessionEnded(int pomodorosCompleted) implements EndedSession {}
    record BreakSessionActive(int pomodorosCompleted, Duration currentDuration) implements ActiveSession {}
    record BreakSessionEnded(int pomodorosCompleted) implements EndedSession {}

    private final SessionDurations sessionDurations;

    PomodoroModel(SessionDurations sessionDurations) {
        this.sessionDurations = sessionDurations;
    }

    State tick(State state) {
        return switch (state) {
            case WorkSessionActive(int pomodorosCompleted, Duration currentDuration) -> {
                var updatedDuration = currentDuration.minus(1, ChronoUnit.SECONDS);
                yield updatedDuration.isZero() ?
                      new WorkSessionEnded(pomodorosCompleted + 1) :
                      new WorkSessionActive(pomodorosCompleted, updatedDuration);
            }
            case WorkSessionEnded(int pomodorosCompleted) ->
                pomodorosCompleted < MAX_WORK_POMODOROS ?
                      new BreakSessionActive(pomodorosCompleted, sessionDurations.shortBreakDuration()) :
                      new BreakSessionActive(pomodorosCompleted, sessionDurations.longBreakDuration());
            case BreakSessionActive(int pomodorosCompleted, Duration currentDuration) -> {
                var updatedDuration = currentDuration.minus(1, ChronoUnit.SECONDS);
                yield updatedDuration.isZero() ?
                      new BreakSessionEnded(pomodorosCompleted) :
                      new BreakSessionActive(pomodorosCompleted, updatedDuration);
            }
            case BreakSessionEnded(int pomodorosCompleted) ->
                  pomodorosCompleted < MAX_WORK_POMODOROS ?
                      new WorkSessionActive(pomodorosCompleted, sessionDurations.workDuration()) :
                      new WorkSessionActive(0, sessionDurations.workDuration());
        };
    }
}
