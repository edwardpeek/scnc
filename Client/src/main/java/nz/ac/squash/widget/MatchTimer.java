package nz.ac.squash.widget;

import org.apache.log4j.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static javax.sound.sampled.LineEvent.Type.STOP;

public class MatchTimer {
    private static final Logger LOGGER = Logger.getLogger(MatchTimer.class);

    private static final long TIMER_GRANULARITY = TimeUnit.MINUTES.toMillis(5);
    private static final DateFormat TIMER_FORMAT = new SimpleDateFormat("hh:mm");

    private JPanel mPanel;
    private JButton mAutoButton;
    private JButton mUpButton;
    private JButton mDownButton;

    private final Collection<MatchPanel> mAffectedMatches;
    private final Timer mAutoAdvanceTimer;
    private long mAutoAdvanceTime = 0;

    public MatchTimer(Collection<MatchPanel> targets) {
        mAffectedMatches = targets;
        mAutoAdvanceTimer = new Timer(0, e -> handleTimerEvent());

        mDownButton.addActionListener(e -> handleDownClick());
        mUpButton.addActionListener(e -> handleUpClick());
        mAutoButton.addActionListener(e -> handleToggleAutoAdvance());
    }

    public JPanel getPanel() {
        return mPanel;
    }

    private void handleUpClick() {
        if (isAutomatic()) {
            increaseTimer();
            refreshTimerLabel();
        } else {
            advanceMatches();
        }
    }

    private void handleDownClick() {
        if (isAutomatic()) {
            decreaseTimer();
            refreshTimerLabel();
        } else {
            reverseMatches();
        }
    }

    private void advanceMatches() {
        mAffectedMatches.forEach(MatchPanel::nextSlot);
    }

    private void reverseMatches() {
        mAffectedMatches.forEach(MatchPanel::previousSlot);
    }

    private void handleToggleAutoAdvance() {
        if (isAutomatic()) {
            mAutoAdvanceTime = 0;
            restartTimer();
            refreshTimerLabel();
        } else {
            mAutoAdvanceTime = System.currentTimeMillis();
            increaseTimer();
            refreshTimerLabel();
        }
    }

    private void handleTimerEvent() {
        advanceMatches();
        playAlertSound();

        mAutoAdvanceTime = 0;
        restartTimer();
        refreshTimerLabel();
    }

    private void increaseTimer() {
        mAutoAdvanceTime = (mAutoAdvanceTime / TIMER_GRANULARITY + 1L) * TIMER_GRANULARITY;
        restartTimer();
    }

    private void decreaseTimer() {
        mAutoAdvanceTime = (mAutoAdvanceTime / TIMER_GRANULARITY - 1L) * TIMER_GRANULARITY;
        restartTimer();
        refreshTimerLabel();
    }

    private void restartTimer() {
        long nowTimeMillis = System.currentTimeMillis();
        long delay = mAutoAdvanceTime - nowTimeMillis;

        mAutoAdvanceTimer.stop();
        if (delay > 0) {
            mAutoAdvanceTimer.setInitialDelay((int) delay);
            mAutoAdvanceTimer.start();
        }
    }

    private void refreshTimerLabel() {
        if (isAutomatic()) {
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(mAutoAdvanceTime);

            mAutoButton.setText(TIMER_FORMAT.format(time.getTime()));
        } else {
            mAutoButton.setText("Auto");
        }
    }

    private boolean isAutomatic() {
        return mAutoAdvanceTimer.isRunning();
    }

    private static void playAlertSound() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(ClassLoader.getSystemResourceAsStream("sounds/alert.wav")));
            clip.addLineListener(event -> {
                if (event.getType() == STOP) clip.close();
            });

            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            LOGGER.error("Failed to play alert tone", e);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mPanel = new JPanel();
        mPanel.setLayout(new GridBagLayout());
        mPanel.setOpaque(false);
        mAutoButton = new JButton();
        mAutoButton.setMargin(new Insets(2, 0, 2, 0));
        mAutoButton.setOpaque(false);
        mAutoButton.setPreferredSize(new Dimension(64, 32));
        mAutoButton.setText("Auto");
        mAutoButton.setVerifyInputWhenFocusTarget(false);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mPanel.add(mAutoButton, gbc);
        mUpButton = new JButton();
        mUpButton.setOpaque(false);
        mUpButton.setText("^");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mPanel.add(mUpButton, gbc);
        mDownButton = new JButton();
        mDownButton.setOpaque(false);
        mDownButton.setText("v");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mPanel.add(mDownButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mPanel;
    }
}
