package nz.ac.squash.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Match;
import nz.ac.squash.db.beans.MatchResult;
import nz.ac.squash.util.Utility;
import nz.ac.squash.widget.MatchPanel;
import nz.ac.squash.widget.generic.VTextIcon;
import nz.ac.squash.widget.generic.VerticalGridLayout;

public class SchedulePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int[] COURTS = new int[] { 5, 6, 7, 8, 1, 2, 3, 4 };
    private static final int SLOTS = 6;

    private MatchPanel[][] mMatchPanels;
    private JPanel mMatchGrid;

    public SchedulePanel() {
        createContents();

        mMatchPanels = new MatchPanel[COURTS.length][SLOTS];

        List<MatchPanel>[] courtGroups = (List<MatchPanel>[]) new List<?>[COURTS.length];
        List<MatchPanel>[] slotGroups = (List<MatchPanel>[]) new List<?>[SLOTS];

        for (int i = 0; i < slotGroups.length; i++) {
            slotGroups[i] = new ArrayList<>();
        }

        for (int i = 0; i < courtGroups.length; i++) {
            courtGroups[i] = new ArrayList<>();
        }

        for (int c = 0; c < COURTS.length; c++) {
            final int court = COURTS[c];
            int startingSlot = findStartingSlot(court);

            for (int s = 0; s < SLOTS; s++) {
                final int slot = s - 2;

                MatchPanel matchPanel = mMatchPanels[c][s] = new MatchPanel(
                        court, slot, slot + startingSlot, courtGroups[c],
                        slotGroups[s]);

                mMatchGrid.add(matchPanel);
                courtGroups[c].add(matchPanel);
                slotGroups[s].add(matchPanel);
            }
        }

        checkForCollisions();
    }

    private void createContents() {
        setOpaque(false);
        GridBagLayout gbl_schedulePanel = new GridBagLayout();
        gbl_schedulePanel.columnWidths = new int[] { 96, 0, 0, 0, 0, 0, 0, 0,
                0, 0 };
        gbl_schedulePanel.rowHeights = new int[] { 0, 0, 116, 116, 116, 116,
                116, 116, 0, 0 };
        gbl_schedulePanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
        gbl_schedulePanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
        setLayout(gbl_schedulePanel);

        JLabel lblNewLabel_6 = new JLabel("Upstairs");
        lblNewLabel_6.setForeground(Color.WHITE);
        lblNewLabel_6.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
        gbc_lblNewLabel_6.gridwidth = 4;
        gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_6.gridx = 1;
        gbc_lblNewLabel_6.gridy = 0;
        add(lblNewLabel_6, gbc_lblNewLabel_6);

        JLabel lblNewLabel_7 = new JLabel("Downstairs");
        lblNewLabel_7.setForeground(Color.WHITE);
        lblNewLabel_7.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
        gbc_lblNewLabel_7.gridwidth = 4;
        gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 0);
        gbc_lblNewLabel_7.gridx = 5;
        gbc_lblNewLabel_7.gridy = 0;
        add(lblNewLabel_7, gbc_lblNewLabel_7);

        JLabel lblNewLabel = new JLabel("Court 5");
        lblNewLabel.setForeground(Color.WHITE);
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 1;
        gbc_lblNewLabel.gridy = 1;
        add(lblNewLabel, gbc_lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Court 6");
        lblNewLabel_1.setForeground(Color.WHITE);
        lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 2;
        gbc_lblNewLabel_1.gridy = 1;
        add(lblNewLabel_1, gbc_lblNewLabel_1);

        JLabel lblNewLabel_2 = new JLabel("Court 7");
        lblNewLabel_2.setForeground(Color.WHITE);
        lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_2.gridx = 3;
        gbc_lblNewLabel_2.gridy = 1;
        add(lblNewLabel_2, gbc_lblNewLabel_2);

        JLabel lblNewLabel_3 = new JLabel("Court 8");
        lblNewLabel_3.setForeground(Color.WHITE);
        lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
        gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_3.gridx = 4;
        gbc_lblNewLabel_3.gridy = 1;
        add(lblNewLabel_3, gbc_lblNewLabel_3);

        JLabel lblNewLabel_4 = new JLabel("Court 1");
        lblNewLabel_4.setForeground(Color.WHITE);
        lblNewLabel_4.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
        gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_4.gridx = 5;
        gbc_lblNewLabel_4.gridy = 1;
        add(lblNewLabel_4, gbc_lblNewLabel_4);

        JLabel lblNewLabel_5 = new JLabel("Court 2");
        lblNewLabel_5.setForeground(Color.WHITE);
        lblNewLabel_5.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
        gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_5.gridx = 6;
        gbc_lblNewLabel_5.gridy = 1;
        add(lblNewLabel_5, gbc_lblNewLabel_5);

        JLabel lblCourt = new JLabel("Court 3");
        lblCourt.setForeground(Color.WHITE);
        lblCourt.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblCourt = new GridBagConstraints();
        gbc_lblCourt.insets = new Insets(0, 0, 5, 5);
        gbc_lblCourt.gridx = 7;
        gbc_lblCourt.gridy = 1;
        add(lblCourt, gbc_lblCourt);

        JLabel lblCourt_1 = new JLabel("Court 4");
        lblCourt_1.setForeground(Color.WHITE);
        lblCourt_1.setFont(new Font("Tahoma", Font.PLAIN, 24));
        GridBagConstraints gbc_lblCourt_1 = new GridBagConstraints();
        gbc_lblCourt_1.insets = new Insets(0, 0, 5, 0);
        gbc_lblCourt_1.gridx = 8;
        gbc_lblCourt_1.gridy = 1;
        add(lblCourt_1, gbc_lblCourt_1);

        JLabel rtdlblPastGames = new JLabel();
        rtdlblPastGames.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rtdlblPastGames.setIcon(new VTextIcon(rtdlblPastGames, "Previous",
                VTextIcon.ROTATE_LEFT));
        GridBagConstraints gbc_rtdlblPastGames = new GridBagConstraints();
        gbc_rtdlblPastGames.gridheight = 2;
        gbc_rtdlblPastGames.gridx = 0;
        gbc_rtdlblPastGames.gridy = 2;
        add(rtdlblPastGames, gbc_rtdlblPastGames);

        JLabel rtdlblNowPlaying = new JLabel();
        rtdlblNowPlaying.setForeground(Color.WHITE);
        rtdlblNowPlaying.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rtdlblNowPlaying.setIcon(new VTextIcon(rtdlblPastGames, "Playing",
                VTextIcon.ROTATE_LEFT));
        GridBagConstraints gbc_rtdlblNowPlaying = new GridBagConstraints();
        gbc_rtdlblNowPlaying.gridx = 0;
        gbc_rtdlblNowPlaying.gridy = 4;
        add(rtdlblNowPlaying, gbc_rtdlblNowPlaying);

        JLabel rtdlblUpcomingGames = new JLabel();
        rtdlblUpcomingGames.setFont(new Font("Tahoma", Font.PLAIN, 18));
        rtdlblUpcomingGames.setIcon(new VTextIcon(rtdlblPastGames, "Upcoming",
                VTextIcon.ROTATE_LEFT));
        GridBagConstraints gbc_rtdlblUpcomingGames = new GridBagConstraints();
        gbc_rtdlblUpcomingGames.gridheight = 3;
        gbc_rtdlblUpcomingGames.gridx = 0;
        gbc_rtdlblUpcomingGames.gridy = 5;
        add(rtdlblUpcomingGames, gbc_rtdlblUpcomingGames);

        mMatchGrid = new JPanel();
        mMatchGrid.setOpaque(false);
        GridBagConstraints gbc_mMatchGrid = new GridBagConstraints();
        gbc_mMatchGrid.gridheight = 6;
        gbc_mMatchGrid.gridwidth = 8;
        gbc_mMatchGrid.fill = GridBagConstraints.BOTH;
        gbc_mMatchGrid.gridx = 1;
        gbc_mMatchGrid.gridy = 2;
        add(mMatchGrid, gbc_mMatchGrid);
        mMatchGrid.setLayout(new VerticalGridLayout(0, 8, 0, 0));

        JPanel panel_7 = new JPanel();
        panel_7.setOpaque(false);
        GridBagConstraints gbc_panel_7 = new GridBagConstraints();
        gbc_panel_7.insets = new Insets(5, 0, 0, 0);
        gbc_panel_7.fill = GridBagConstraints.BOTH;
        gbc_panel_7.gridx = 0;
        gbc_panel_7.gridy = 8;
        add(panel_7, gbc_panel_7);
        panel_7.setLayout(new GridLayout(1, 0, 0, 0));

        JButton button = new JButton("\u25BC");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                previousSlot(-1);
            }
        });
        button.setOpaque(false);
        panel_7.add(button);

        JButton btnVvv = new JButton("\u25B2");
        btnVvv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextSlot(-1);
            }
        });
        btnVvv.setOpaque(false);
        panel_7.add(btnVvv);

        for (int court = 0; court < 8; court++) {
            final int courtF = court;

            JPanel panel_8 = new JPanel();
            panel_8.setOpaque(false);
            GridBagConstraints gbc_panel_8 = new GridBagConstraints();
            gbc_panel_8.anchor = GridBagConstraints.SOUTH;
            gbc_panel_8.gridx = 1 + courtF;
            gbc_panel_8.gridy = 8;
            add(panel_8, gbc_panel_8);
            panel_8.setLayout(new GridLayout(1, 0, 0, 0));

            JButton button_1 = new JButton("\u25BE");
            button_1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    previousSlot(courtF);
                }
            });
            button_1.setOpaque(false);
            panel_8.add(button_1);

            JButton button_2 = new JButton("\u25B4");
            button_2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    nextSlot(courtF);
                }
            });
            button_2.setOpaque(false);
            panel_8.add(button_2);
        }

        JPanel panel_1 = new JPanel();
        panel_1.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.gridheight = 2;
        gbc_panel_1.gridwidth = 9;
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 2;
        add(panel_1, gbc_panel_1);

        JPanel panel = new JPanel();
        panel.setBackground(Color.decode("#74B2E3"));
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridwidth = 9;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 4;
        add(panel, gbc_panel);

        JPanel panel_3 = new JPanel();
        panel_3.setBackground(Color.WHITE);
        GridBagConstraints gbc_panel_3 = new GridBagConstraints();
        gbc_panel_3.gridwidth = 9;
        gbc_panel_3.gridheight = 3;
        gbc_panel_3.fill = GridBagConstraints.BOTH;
        gbc_panel_3.gridx = 0;
        gbc_panel_3.gridy = 5;
        add(panel_3, gbc_panel_3);
    }

    private void nextSlot(int court) {
        int start = court == -1 ? 0 : court;
        int end = court == -1 ? mMatchPanels.length : court + 1;

        for (int c = start; c < end; ++c) {
            for (int slot = 0; slot < mMatchPanels[c].length; slot++) {
                final MatchPanel matchPanel = mMatchPanels[c][slot];
                matchPanel.nextSlot();
            }
        }

        checkForCollisions();
    }

    private void previousSlot(int court) {
        int start = court == -1 ? 0 : court;
        int end = court == -1 ? mMatchPanels.length : court + 1;

        for (int c = start; c < end; ++c) {
            for (int slot = 0; slot < mMatchPanels[c].length; slot++) {
                final MatchPanel matchPanel = mMatchPanels[c][slot];
                matchPanel.previousSlot();
            }
        }

        checkForCollisions();
    }

    private void checkForCollisions() {
        for (int court = 0; court < 8; court++) {
            for (int slot = 0; slot < 6; slot++) {
                // TODO Actual range.
                mMatchPanels[court][slot].checkForCollisions();
            }
        }
    }

    private static int findStartingSlot(final int court) {
        final Date today = Utility.today();

        return DB.executeTransaction(new Transaction<Integer>() {
            @Override
            public void run() {
                int slot = 0;

                // Start after every game that has finished.
                MatchResult latestResult = Utility
                        .first(query(
                                MatchResult.class,
                                "r where r.mMatch.mDate >= ?0 and r.mMatch.mCourt = ?1 order by r.mMatch.mTimeSlot desc",
                                today, court));
                if (latestResult != null) {
                    slot = latestResult.getMatch().getTimeSlot() + 1;
                }

                // Start on first game without a result.
                Match nextGame = Utility
                        .first(query(
                                Match.class,
                                "m where m.mDate >= ?0 and m.mCourt = ?1 and m.mTimeSlot >= ?2 order by m.mTimeSlot asc",
                                today, court, slot));
                if (nextGame != null) {
                    slot = nextGame.getTimeSlot();
                }

                setResult(slot);
            }
        });
    }

    public Iterable<MatchPanel> getMatchPanels() {
        return new Iterable<MatchPanel>() {
            @Override
            public Iterator<MatchPanel> iterator() {
                return new Iterator<MatchPanel>() {
                    private int i = 0;

                    @Override
                    public void remove() {
                        throw new RuntimeException("Iterable is read-only");
                    }

                    @Override
                    public MatchPanel next() {
                        final int court = i / SLOTS;
                        final int slot = i % SLOTS;
                        ++i;

                        return mMatchPanels[court][slot];
                    }

                    @Override
                    public boolean hasNext() {
                        return i < COURTS.length * SLOTS;
                    }
                };
            }
        };
    }
}