package nz.ac.squash.windows;

import nz.ac.squash.db.DB;
import nz.ac.squash.db.DB.Transaction;
import nz.ac.squash.util.Importer;
import nz.ac.squash.util.SwingUtils;
import nz.ac.squash.util.Utility;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ImportWindow extends JDialog {
    private static final File DOWNLOAD_CONFIG_FILE = new File("db/import.uri");

    private JButton mDownloadButton;
    private JProgressBar mDownloadProgress;
    private JList mFileList;
    private JPanel panel;
    private JButton mCancelButton;
    private JButton mImportButton;
    private JTable mChangeTable;
    private JScrollPane scrollPane;

    private File mChangeFile = null;
    private List<Importer.ImportAction> mChangeSet = null;

    public static ImportWindow showDialog(Component parent) {
        final JFrame frame = parent instanceof JFrame ? (JFrame) parent
                : (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class,
                        parent);

        ImportWindow window = new ImportWindow(frame.getOwner());

        frame.getGlassPane().setVisible(true);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.getGlassPane().setVisible(false);
            }
        });

        window.setVisible(true);
        return window;
    }

    private ImportWindow(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        createContents();
        rescanDir();

        pack();
        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(mCancelButton);
        SwingUtils.closeOnEscape(this);
    }

    private void createContents() {
        getContentPane().setBackground(Color.WHITE);

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 50, 500, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0,
                Double.MIN_VALUE };
        getContentPane().setLayout(gridBagLayout);

        JLabel lblNewLabel = new JLabel("Import Members");
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setOpaque(true);
        lblNewLabel.setBackground(Color.decode("#535353"));
        lblNewLabel.setForeground(Color.WHITE);
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.ipady = 5;
        gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblNewLabel.gridwidth = 2;
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        getContentPane().add(lblNewLabel, gbc_lblNewLabel);

        mDownloadButton = new JButton("Download from Google Drive");
        mDownloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadMemberList();
            }
        });
        GridBagConstraints gbc_mDownloadButton = new GridBagConstraints();
        gbc_mDownloadButton.insets = new Insets(5, 5, 5, 5);
        gbc_mDownloadButton.gridx = 0;
        gbc_mDownloadButton.gridy = 1;
        getContentPane().add(mDownloadButton, gbc_mDownloadButton);

        mDownloadProgress = new JProgressBar();
        GridBagConstraints gbc_mDownloadProgress = new GridBagConstraints();
        gbc_mDownloadProgress.insets = new Insets(0, 0, 0, 5);
        gbc_mDownloadProgress.fill = GridBagConstraints.HORIZONTAL;
        gbc_mDownloadProgress.gridx = 1;
        gbc_mDownloadProgress.gridy = 1;
        getContentPane().add(mDownloadProgress, gbc_mDownloadProgress);

        mFileList = new JList();
        mFileList.addListSelectionListener(new ListSelectionListener() {
            int mPreviousIndex = -1;

            public void valueChanged(ListSelectionEvent e) {
                if (e.getFirstIndex() == mPreviousIndex) return;
                updateChangeSet();
                mPreviousIndex = e.getFirstIndex();
            }
        });
        mFileList.setBorder(new LineBorder(new Color(0, 0, 0)));
        GridBagConstraints gbc_mFileList = new GridBagConstraints();
        gbc_mFileList.insets = new Insets(0, 5, 5, 5);
        gbc_mFileList.gridwidth = 2;
        gbc_mFileList.fill = GridBagConstraints.BOTH;
        gbc_mFileList.gridx = 0;
        gbc_mFileList.gridy = 2;
        getContentPane().add(mFileList, gbc_mFileList);

        scrollPane = new JScrollPane();
        scrollPane.setMinimumSize(new Dimension(0, 0));
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 5, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 3;
        getContentPane().add(scrollPane, gbc_scrollPane);

        mChangeTable = new JTable();
        mChangeTable.setPreferredScrollableViewportSize(new Dimension(0, 100));
        scrollPane.setViewportView(mChangeTable);
        mChangeTable.setModel(new DefaultTableModel(new Object[][] {},
                new String[] { "Member", "Action" }) {
            Class[] columnTypes = new Class[] { String.class, String.class };

            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            boolean[] columnEditables = new boolean[] { false, false };

            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }
        });

        panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridwidth = 2;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 4;
        getContentPane().add(panel, gbc_panel);

        mCancelButton = new JButton("Cancel");
        mCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        panel.add(mCancelButton);

        mImportButton = new JButton("Import");
        mImportButton.setEnabled(false);
        mImportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyChangeSet();
            }
        });
        panel.add(mImportButton);
    }

    private void applyChangeSet() {
        DB.executeTransaction(new Transaction<Void>() {
            @Override
            public void run() {
                for (Importer.ImportAction action : mChangeSet) {
                    action.apply();
                }
            }
        });

        mChangeFile.delete();
        rescanDir();

        mChangeSet = null;
        mImportButton.setEnabled(false);

        refreshChangeTable();
    }

    private void refreshChangeTable() {
        final DefaultTableModel model = (DefaultTableModel) mChangeTable
                .getModel();

        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }

        if (mChangeSet == null) return;
        for (Importer.ImportAction action : mChangeSet) {
            model.addRow(new Object[] { action.getMember().getNameFormatted(),
                    action.getDescription() });
        }

        mImportButton.setEnabled(true);
    }

    private void downloadMemberList() {
        mDownloadButton.setEnabled(false);
        mDownloadProgress.setIndeterminate(true);

        final Thread downloadThread = new Thread(new Runnable() {
            private boolean tryDownload() {
                final String importUri;
                try (Scanner s = new Scanner(DOWNLOAD_CONFIG_FILE)) {
                    importUri = s.nextLine();
                } catch (IOException e) {
                    Logger.getLogger(ImportWindow.class).warn(
                            "Failed to find download URI file");
                    return false;
                }

                final String filename = "Google Docs membership at " +
                                        Utility.FILE_SAFE_FORMATTER
                                                .format(new Date()) + ".tsv";

                URL website = null;
                ReadableByteChannel rbc = null;
                FileOutputStream fos = null;
                try {
                    website = new URL(importUri);
                    rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream("db/" + filename);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                    return true;
                } catch (IOException e) {
                    Logger.getLogger(ImportWindow.class).warn(
                            "Failed to download membership list", e);
                    return false;
                } finally {
                    IOUtils.closeQuietly(rbc, fos);
                }
            }

            @Override
            public void run() {
                final boolean sucessfullyDownloaded = tryDownload();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (sucessfullyDownloaded) rescanDir();
                        mDownloadButton.setEnabled(true);
                        mDownloadProgress.setIndeterminate(false);
                    }
                });
            }
        });
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private void updateChangeSet() {
        mImportButton.setEnabled(false);

        mChangeFile = (File) mFileList.getSelectedValue();
        if (mChangeFile == null) return;

        mChangeSet = Importer.generateImport(mChangeFile);

        refreshChangeTable();
    }

    private void rescanDir() {
        DefaultListModel<File> model = new DefaultListModel<File>();

        for (File file : new File("db").listFiles()) {
            if (file.isDirectory() || !file.getName().endsWith(".tsv")) continue;

            model.addElement(file);
        }

        mFileList.setModel(model);
    }
}
