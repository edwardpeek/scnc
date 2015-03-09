package nz.ac.squash.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.db.beans.Member;
import nz.ac.squash.db.beans.Member.MemberResults;
import nz.ac.squash.db.beans.MemberStatus;
import nz.ac.squash.util.LatestExecutor;

import org.apache.commons.lang3.StringUtils;

public class SignInPanel extends JLayeredPane {
    private static final long serialVersionUID = 1L;
    private static final ListModel<Member> EMPTY_RESULTS = new DefaultListModel<Member>();

    private ExecutorService mSearchTask = new LatestExecutor();

    private Member mSelectedMember = null;

    private JTextField mSearchField;
    private JLabel mSearchHintLabel;
    private JList<Member> mResultList;
    private JSlider mSkillSlider;
    private JLabel lblNewLabel_1;
    private JLabel lblNewLabel_2;
    private JLabel lblIntermediate;
    private JLabel lblBeginner;
    private JButton mSignOutButton;
    private final ButtonGroup mPlayStyleGroup = new ButtonGroup();
    private JPanel mPlayerPanel;
    private JButton mSignInButton;
    private JRadioButton mWantGamesRadio;
    private JRadioButton mWantTrainingRadio;

    public SignInPanel() {
        createContents();
    }

    public void clear() {
        mSearchField.setText("");
        mSearchField.requestFocus();
        handleSearchQueryChanged("");

        hideMemberPanel();
    }

    private void createContents() {
        setOpaque(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0, 700, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 275, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0,
                Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0,
                Double.MIN_VALUE };
        setLayout(gridBagLayout);

        JLabel lblNewLabel = new JLabel((String) null);
        lblNewLabel.setIcon(new ImageIcon(SignInPanel.class
                .getResource("/images/icon_search.png")));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 0, 16);
        gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
        gbc_lblNewLabel.gridx = 1;
        gbc_lblNewLabel.gridy = 1;
        add(lblNewLabel, gbc_lblNewLabel);

        mSearchHintLabel = new JLabel(" Start typing your name");
        mSearchHintLabel.setCursor(Cursor
                .getPredefinedCursor(Cursor.TEXT_CURSOR));
        mSearchHintLabel.setFocusable(false);
        setLayer(mSearchHintLabel, 1);
        GridBagConstraints gbc_mSearchHintLabel = new GridBagConstraints();
        gbc_mSearchHintLabel.fill = GridBagConstraints.BOTH;
        gbc_mSearchHintLabel.gridx = 2;
        gbc_mSearchHintLabel.gridy = 1;
        add(mSearchHintLabel, gbc_mSearchHintLabel);
        mSearchHintLabel.setForeground(Color.LIGHT_GRAY);
        mSearchHintLabel.setFont(new Font("Tahoma", Font.PLAIN, 28));

        mSearchField = new JTextField();
        mSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleMemberSelected(mResultList.getSelectedValue());
                } else {
                    handleSearchQueryChanged(mSearchField.getText());
                }
            }
        });
        setLayer(mSearchField, 0);
        mSearchField.setAlignmentX(Component.CENTER_ALIGNMENT);
        mSearchField.setFont(new Font("Tahoma", Font.PLAIN, 28));
        mSearchField.setColumns(24);
        GridBagConstraints gbc_txtSearchForMember = new GridBagConstraints();
        gbc_txtSearchForMember.fill = GridBagConstraints.BOTH;
        gbc_txtSearchForMember.gridx = 2;
        gbc_txtSearchForMember.gridy = 1;
        add(mSearchField, gbc_txtSearchForMember);

        mResultList = new JList<Member>();
        mResultList.setVisible(false);
        mResultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMemberSelected(mResultList.getSelectedValue());
            }
        });
        setLayer(mResultList, 0);

        mPlayerPanel = new JPanel();
        mPlayerPanel.setVisible(false);
        setLayer(mPlayerPanel, 0);
        mPlayerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc_mPlayerPanel = new GridBagConstraints();
        gbc_mPlayerPanel.insets = new Insets(16, 0, 0, 0);
        gbc_mPlayerPanel.fill = GridBagConstraints.BOTH;
        gbc_mPlayerPanel.gridx = 2;
        gbc_mPlayerPanel.gridy = 2;
        add(mPlayerPanel, gbc_mPlayerPanel);
        GridBagLayout gbl_mPlayerPanel = new GridBagLayout();
        gbl_mPlayerPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
        gbl_mPlayerPanel.rowHeights = new int[] { 48, 0, 0, 0, 0, 0 };
        gbl_mPlayerPanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0,
                Double.MIN_VALUE };
        gbl_mPlayerPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0,
                Double.MIN_VALUE };
        mPlayerPanel.setLayout(gbl_mPlayerPanel);

        lblNewLabel_1 = new JLabel("Advanced");
        lblNewLabel_1.setPreferredSize(new Dimension(128, 32));
        lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTH;
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 1;
        mPlayerPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

        lblIntermediate = new JLabel("Intermediate");
        lblIntermediate.setPreferredSize(new Dimension(128, 32));
        lblIntermediate.setHorizontalAlignment(SwingConstants.CENTER);
        lblIntermediate.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_lblIntermediate = new GridBagConstraints();
        gbc_lblIntermediate.anchor = GridBagConstraints.NORTH;
        gbc_lblIntermediate.insets = new Insets(0, 48, 0, 0);
        gbc_lblIntermediate.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblIntermediate.gridx = 1;
        gbc_lblIntermediate.gridy = 1;
        mPlayerPanel.add(lblIntermediate, gbc_lblIntermediate);

        lblBeginner = new JLabel("Beginner");
        lblBeginner.setPreferredSize(new Dimension(128, 32));
        lblBeginner.setHorizontalAlignment(SwingConstants.CENTER);
        lblBeginner.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_lblBeginner = new GridBagConstraints();
        gbc_lblBeginner.anchor = GridBagConstraints.NORTH;
        gbc_lblBeginner.insets = new Insets(0, 0, 0, 48);
        gbc_lblBeginner.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblBeginner.gridx = 2;
        gbc_lblBeginner.gridy = 1;
        mPlayerPanel.add(lblBeginner, gbc_lblBeginner);

        lblNewLabel_2 = new JLabel("Never played");
        lblNewLabel_2.setPreferredSize(new Dimension(128, 32));
        lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTH;
        gbc_lblNewLabel_2.gridx = 3;
        gbc_lblNewLabel_2.gridy = 1;
        mPlayerPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);

        mSkillSlider = new JSlider();
        mSkillSlider.setMinorTickSpacing(1);
        mSkillSlider.setOpaque(false);
        mSkillSlider.setPaintTicks(true);
        mSkillSlider.setValue(66);
        mSkillSlider.setMaximum(9);
        mSkillSlider.setMajorTickSpacing(3);
        GridBagConstraints gbc_mSkillSlider = new GridBagConstraints();
        gbc_mSkillSlider.fill = GridBagConstraints.HORIZONTAL;
        gbc_mSkillSlider.anchor = GridBagConstraints.SOUTH;
        gbc_mSkillSlider.insets = new Insets(0, 64, 0, 64);
        gbc_mSkillSlider.gridwidth = 4;
        gbc_mSkillSlider.gridx = 0;
        gbc_mSkillSlider.gridy = 0;
        mPlayerPanel.add(mSkillSlider, gbc_mSkillSlider);

        mWantTrainingRadio = new JRadioButton(
                "I want to get training / learn how to play");
        mPlayStyleGroup.add(mWantTrainingRadio);
        mWantTrainingRadio.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mWantTrainingRadio.setOpaque(false);
        GridBagConstraints gbc_mWantTrainingRadio = new GridBagConstraints();
        gbc_mWantTrainingRadio.insets = new Insets(16, 64, 0, 0);
        gbc_mWantTrainingRadio.anchor = GridBagConstraints.WEST;
        gbc_mWantTrainingRadio.gridx = 0;
        gbc_mWantTrainingRadio.gridy = 2;
        gbc_mWantTrainingRadio.gridwidth = 4;
        mPlayerPanel.add(mWantTrainingRadio, gbc_mWantTrainingRadio);

        mWantGamesRadio = new JRadioButton("I want to play games");
        mPlayStyleGroup.add(mWantGamesRadio);
        mWantGamesRadio.setOpaque(false);
        mWantGamesRadio.setFont(new Font("Tahoma", Font.PLAIN, 18));
        GridBagConstraints gbc_mWantGamesRadio = new GridBagConstraints();
        gbc_mWantGamesRadio.anchor = GridBagConstraints.WEST;
        gbc_mWantGamesRadio.gridwidth = 4;
        gbc_mWantGamesRadio.insets = new Insets(0, 64, 0, 0);
        gbc_mWantGamesRadio.gridx = 0;
        gbc_mWantGamesRadio.gridy = 3;
        mPlayerPanel.add(mWantGamesRadio, gbc_mWantGamesRadio);

        mSignInButton = new JButton("Sign in");
        mSignInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleSignIn();
            }
        });
        mSignInButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mSignInButton.setPreferredSize(new Dimension(63, 48));
        mSignInButton.setOpaque(false);
        GridBagConstraints gbc_mSignInButton = new GridBagConstraints();
        gbc_mSignInButton.anchor = GridBagConstraints.SOUTH;
        gbc_mSignInButton.insets = new Insets(16, 64, 16, 16);
        gbc_mSignInButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_mSignInButton.gridwidth = 2;
        gbc_mSignInButton.gridx = 0;
        gbc_mSignInButton.gridy = 4;
        mPlayerPanel.add(mSignInButton, gbc_mSignInButton);

        mSignOutButton = new JButton("Sign out");
        mSignOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleSignOut();
            }
        });
        mSignOutButton.setEnabled(false);
        mSignOutButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
        mSignOutButton.setPreferredSize(new Dimension(71, 48));
        mSignOutButton.setOpaque(false);
        GridBagConstraints gbc_mSignOutButton = new GridBagConstraints();
        gbc_mSignOutButton.anchor = GridBagConstraints.SOUTH;
        gbc_mSignOutButton.insets = new Insets(16, 16, 16, 64);
        gbc_mSignOutButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_mSignOutButton.gridwidth = 2;
        gbc_mSignOutButton.gridx = 2;
        gbc_mSignOutButton.gridy = 4;
        mPlayerPanel.add(mSignOutButton, gbc_mSignOutButton);
        mResultList.setFont(new Font("Tahoma", Font.PLAIN, 24));
        mResultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mResultList.setVisibleRowCount(5);
        mResultList.setPreferredSize(mResultList
                .getPreferredScrollableViewportSize());
        GridBagConstraints gbc_mResultList = new GridBagConstraints();
        gbc_mResultList.insets = new Insets(0, 0, 0, 0);
        gbc_mResultList.anchor = GridBagConstraints.NORTH;
        gbc_mResultList.fill = GridBagConstraints.HORIZONTAL;
        gbc_mResultList.gridx = 2;
        gbc_mResultList.gridy = 2;
        add(mResultList, gbc_mResultList);
    }

    protected void handleSignIn() {
        final MemberStatus newStatus = new MemberStatus(mSelectedMember);
        newStatus.setSkillLevel(mSkillSlider.getValue() / 3.f + 1.f);
        newStatus.setWantsGames(mWantGamesRadio.isSelected());
        newStatus.setPresent(true);

        DB.queueTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                update(newStatus);
            }
        });

        hideMemberPanel();
    }

    protected void handleSignOut() {
        final MemberStatus newStatus = new MemberStatus(mSelectedMember);
        newStatus.setPresent(false);

        DB.queueTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                update(newStatus);
            }
        });

        hideMemberPanel();
    }

    protected void handleMemberSelected(Member member) {
        if (member == null) return;

        mSearchField.setText(member.getNameFormatted());
        updateResultData(new MemberResults());

        showMemberpanel(member);
    }

    protected void handleSearchQueryChanged(final String query) {
        mSearchHintLabel.setVisible(StringUtils.isEmpty(query));

        if (!StringUtils.isEmpty(query)) {
            mSearchTask.execute(new Runnable() {
                @Override
                public void run() {
                    final MemberResults results = Member.searchMembers(query,
                            5, false);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateResultData(results);
                        }
                    });
                }
            });
        } else {
            mResultList.setVisible(false);
            mResultList.setEnabled(false);
            mResultList.setModel(EMPTY_RESULTS);
        }
    }

    private void showMemberpanel(Member member) {
        mSelectedMember = member;
        MemberStatus previousStatus = new MemberStatus(member);

        final int skill = Math
                .round((previousStatus.getSkillLevel() - 1.f) * 3.f);
        mSkillSlider.setValue(skill);

        mWantGamesRadio.setSelected(previousStatus.wantsGames());
        mWantTrainingRadio.setSelected(!previousStatus.wantsGames());

        mSignInButton
                .setText(previousStatus.isPresent() ? "Update" : "Sign-in");

        mSignOutButton.setEnabled(previousStatus.isPresent());

        mPlayerPanel.setVisible(true);
    }

    private void hideMemberPanel() {
        mSelectedMember = null;

        mPlayerPanel.setVisible(false);

        mSearchField.setText("");
        mSearchField.requestFocus();
        handleSearchQueryChanged("");
    }

    private void updateResultData(MemberResults results) {
        mResultList.setListData(results.toArray(new Member[results.size()]));
        mResultList.setEnabled(true);

        mResultList.setVisibleRowCount(results.size());
        mResultList.setPreferredSize(mResultList
                .getPreferredScrollableViewportSize());

        if (results.hasUniqueMatch()) {
            mResultList.setSelectedIndex(0);
        }

        mResultList.setVisible(!results.isEmpty());
    }
}
