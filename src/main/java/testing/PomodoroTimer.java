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

    private static final String INCOMPLETE_POMODORO = "◌";
    private static final String COMPLETE_POMODORO = "●";

    private final PomodoroModel model;

    private final JLabel completedPomodorosLabel = new JLabel();
    private final JLabel sessionLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JButton startButton = new JButton("Start");
    private final Timer timer = new Timer(1000, this::countdownTimer);

    PomodoroTimer(PomodoroModel model) {
        this.model = model;

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
              COMPLETE_POMODORO.repeat(model.pomodorosCompleted()) +
                    INCOMPLETE_POMODORO.repeat(PomodoroModel.MAX_WORK_POMODOROS - model.pomodorosCompleted()));
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
        sessionLabel.setText(model.session() == PomodoroModel.Session.WORK ? "Work" : "Break");
    }

    void setTimerLabel() {
        timerLabel.setText(String.format(
              "%02d:%02d",
              model.currentDuration().toMinutes(),
              model.currentDuration().toSecondsPart()));
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
        model.tick(this::toggleTimer);
        setPomodorosLabel();
        setSessionLabel();
        setTimerLabel();
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
