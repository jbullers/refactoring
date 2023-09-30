package testing;

import java.time.Duration;

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

    void pomodorosCompleted(int pomodorosCompleted) {
        this.pomodorosCompleted = pomodorosCompleted;
    }

    Session session() {
        return session;
    }

    void session(Session session) {
        this.session = session;
    }

    Duration workDuration() {
        return workDuration;
    }

    Duration longBreakDuration() {
        return longBreakDuration;
    }

    Duration shortBreakDuration() {
        return shortBreakDuration;
    }

    Duration currentDuration() {
        return currentDuration;
    }

    void currentDuration(Duration currentDuration) {
        this.currentDuration = currentDuration;
    }
}
