package testing;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

class PomodoroModel {

    record PomodoroEvent(int pomodorosCompleted, Session session, Duration currentDuration) {}

    @FunctionalInterface
    interface PomodoroListener {
        void stateChanged(PomodoroEvent event);
    }

    static final int MAX_WORK_POMODOROS = 4;
    enum Session { WORK, SHORT_BREAK, LONG_BREAK }

    private int pomodorosCompleted;

    private Session session = Session.WORK;
    private final Duration workDuration;
    private final Duration longBreakDuration;
    private final Duration shortBreakDuration;
    private Duration currentDuration;

    private PomodoroListener listener;

    PomodoroModel(Duration workDuration, Duration longBreakDuration, Duration shortBreakDuration) {
        this.workDuration = workDuration;
        this.longBreakDuration = longBreakDuration;
        this.shortBreakDuration = shortBreakDuration;
        currentDuration = workDuration;
    }

    void registerPomodoroListener(PomodoroListener listener) {
        this.listener = listener;
        fireEvent(new PomodoroEvent(pomodorosCompleted, session, currentDuration));
    }

    private void fireEvent(PomodoroEvent event) {
        if (listener != null) {
            listener.stateChanged(event);
        }
    }

    void tick() {
        currentDuration = currentDuration.minus(1, ChronoUnit.SECONDS);
        if (currentDuration.isZero()) {
            fireEvent(new PomodoroEvent(pomodorosCompleted, session, Duration.ZERO));
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
        fireEvent(new PomodoroEvent(pomodorosCompleted, session, currentDuration));
    }
}
