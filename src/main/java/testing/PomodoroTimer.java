package testing;

import java.awt.BorderLayout;
import java.awt.Color;
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
import testing.PomodoroModel.Session;

public class PomodoroTimer extends JPanel {

    private static final String INCOMPLETE_POMODORO = "◌";
    private static final String COMPLETE_POMODORO = "●";

    private final PomodoroModel model;

    private final JLabel completedPomodorosLabel = new JLabel();
    private final JLabel sessionLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JButton startButton = new JButton("Start");
    private final Timer timer;

    PomodoroTimer(PomodoroModel model) {
        this.model = model;
        timer = new Timer(1000, evt -> model.tick());

        setLayout(new BorderLayout());
        add(pomodorosPanel(), BorderLayout.PAGE_START);
        add(timerPanel(), BorderLayout.CENTER);
        add(startButton(), BorderLayout.PAGE_END);

        model.registerPomodoroListener(evt -> {
            if (evt.currentDuration().isZero()) {
                toggleTimer();
            }
            setPomodorosLabel(evt.pomodorosCompleted());
            setSessionLabel(evt.session());
            setTimerLabel(evt.currentDuration());
        });
    }

    JPanel pomodorosPanel() {
        completedPomodorosLabel.setFont(completedPomodorosLabel.getFont().deriveFont(24f));
        completedPomodorosLabel.setForeground(Color.RED);

        var panel = new JPanel();
        panel.add(completedPomodorosLabel);

        return panel;
    }

    void setPomodorosLabel(int pomodorosCompleted) {
        completedPomodorosLabel.setText(
              COMPLETE_POMODORO.repeat(pomodorosCompleted) +
                    INCOMPLETE_POMODORO.repeat(PomodoroModel.MAX_WORK_POMODOROS - pomodorosCompleted));
    }

    Box timerPanel() {
        sessionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        var box = Box.createHorizontalBox();
        box.add(sessionLabel);
        box.add(Box.createHorizontalGlue());
        box.add(timerLabel);

        return box;
    }

    void setSessionLabel(Session session) {
        sessionLabel.setText(session == PomodoroModel.Session.WORK ? "Work" : "Break");
    }

    void setTimerLabel(Duration currentDuration) {
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

    static void createAndShowGui() {
        var frame = new JFrame("Pomodoro");
        frame.setContentPane(
              new PomodoroTimer(
                    new PomodoroModel(
                          Duration.of(10, ChronoUnit.SECONDS),
                          Duration.of(5, ChronoUnit.SECONDS),
                          Duration.of(3, ChronoUnit.SECONDS))));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PomodoroTimer::createAndShowGui);
    }
}
