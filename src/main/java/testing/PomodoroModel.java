package testing;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class PomodoroModel {

    static final int MAX_WORK_POMODOROS = 4;
    enum Session { WORK, SHORT_BREAK, LONG_BREAK }

    private int pomodorosCompleted;

    private Session session = Session.WORK;
    private final Duration workDuration;
    private final Duration longBreakDuration;
    private final Duration shortBreakDuration;
    private Duration currentDuration;

    PomodoroModel(Duration workDuration, Duration longBreakDuration, Duration shortBreakDuration) {
        this.workDuration = workDuration;
        this.longBreakDuration = longBreakDuration;
        this.shortBreakDuration = shortBreakDuration;
        currentDuration = workDuration;
    }

    int pomodorosCompleted() {
        return pomodorosCompleted;
    }

    Session session() {
        return session;
    }

    Duration currentDuration() {
        return currentDuration;
    }

    void tick(Runnable onZeroDuration) {
        currentDuration = currentDuration.minus(1, ChronoUnit.SECONDS);
        if (currentDuration.isZero()) {
            onZeroDuration.run();
            if (session == Session.WORK) {
                pomodorosCompleted = pomodorosCompleted + 1;

                if (pomodorosCompleted == MAX_WORK_POMODOROS) {
                    session = Session.LONG_BREAK;
                    currentDuration = longBreakDuration;
                } else {
                    session = Session.SHORT_BREAK;
                    currentDuration = shortBreakDuration;
                }
            } else if (session == Session.LONG_BREAK) {
                pomodorosCompleted = 0;
                session = Session.WORK;
                currentDuration = workDuration;
            } else if (session == Session.SHORT_BREAK) {
                session = Session.WORK;
                currentDuration = workDuration;
            }
        }
    }
}
