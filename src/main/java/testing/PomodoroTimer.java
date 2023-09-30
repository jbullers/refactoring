package testing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class PomodoroTimer extends JPanel {

    private enum Session { WORK, SHORT_BREAK, LONG_BREAK }

    private static final String INCOMPLETE_POMODORO = "◌";
    private static final String COMPLETE_POMODORO = "●";
    private static final int MAX_WORK_POMODOROS = 4;

    private int pomodorosCompleted;
    private Session session = Session.WORK;
    private final Duration workDuration;
    private final Duration longBreakDuration;
    private final Duration shortBreakDuration;
    private Duration currentDuration;

    private final JLabel completedPomodorosLabel = new JLabel();
    private final JLabel sessionLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JButton startButton = new JButton("Start");
    private final Timer timer = new Timer(1000, this::countdownTimer);

    PomodoroTimer(Duration workDuration, Duration longBreakDuration, Duration shortBreakDuration) {
        this.workDuration = workDuration;
        this.longBreakDuration = longBreakDuration;
        this.shortBreakDuration = shortBreakDuration;
        currentDuration = workDuration;

        setLayout(new BorderLayout());
        add(pomodorosPanel(), BorderLayout.PAGE_START);
        add(timerPanel(), BorderLayout.CENTER);
        add(startButton(), BorderLayout.PAGE_END);
    }

    JPanel pomodorosPanel() {
        completedPomodorosLabel.setFont(completedPomodorosLabel.getFont().deriveFont(24f));
        completedPomodorosLabel.setForeground(Color.RED);
        setPomodorosLabel();

        var panel = new JPanel();
        panel.add(completedPomodorosLabel);

        return panel;
    }

    void setPomodorosLabel() {
        completedPomodorosLabel.setText(
              COMPLETE_POMODORO.repeat(pomodorosCompleted) +
              INCOMPLETE_POMODORO.repeat(MAX_WORK_POMODOROS - pomodorosCompleted));
    }

    Box timerPanel() {
        sessionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        setSessionLabel();
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        setTimerLabel();

        var box = Box.createHorizontalBox();
        box.add(sessionLabel);
        box.add(Box.createHorizontalGlue());
        box.add(timerLabel);

        return box;
    }

    void setSessionLabel() {
        sessionLabel.setText(session == Session.WORK ? "Work" : "Break");
    }

    void setTimerLabel() {
        timerLabel.setText(String.format(
              "%02d:%02d",
              currentDuration.toMinutes(),
              currentDuration.toSecondsPart()));
    }

    JButton startButton() {
        startButton.addActionListener(evt -> toggleTimer());
        return startButton;
    }

    void toggleTimer() {
        if (timer.isRunning()) {
            timer.stop();
            startButton.setText("Start");
        } else {
            timer.start();
            startButton.setText("Stop");
        }
    }

    void countdownTimer(ActionEvent e) {
        currentDuration = currentDuration.minus(1, ChronoUnit.SECONDS);
        if (currentDuration.isZero()) {
            toggleTimer();

            if (session == Session.WORK) {
                pomodorosCompleted++;
                setPomodorosLabel();

                if (pomodorosCompleted == MAX_WORK_POMODOROS) {
                    session = Session.LONG_BREAK;
                    currentDuration = longBreakDuration;
                } else {
                    session = Session.SHORT_BREAK;
                    currentDuration = shortBreakDuration;
                }
            } else if (session == Session.LONG_BREAK) {
                pomodorosCompleted = 0;
                setPomodorosLabel();
                session = Session.WORK;
                currentDuration = workDuration;
            } else if (session == Session.SHORT_BREAK) {
                session = Session.WORK;
                currentDuration = workDuration;
            }

            setSessionLabel();
        }
        setTimerLabel();
    }

    static void createAndShowGui() {
        var frame = new JFrame("Pomodoro");
        frame.setContentPane(new PomodoroTimer(Duration.of(10, ChronoUnit.SECONDS),
                                               Duration.of(5, ChronoUnit.SECONDS),
                                               Duration.of(3, ChronoUnit.SECONDS)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PomodoroTimer::createAndShowGui);
    }
}
