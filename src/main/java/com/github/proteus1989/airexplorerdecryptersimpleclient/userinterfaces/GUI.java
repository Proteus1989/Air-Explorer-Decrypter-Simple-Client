package com.github.proteus1989.airexplorerdecryptersimpleclient.userinterfaces;

import com.github.proteus1989.airexplorerdecryptersimpleclient.AirExplorerFileDecrypterMain;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.AirExplorerDecrypterSimpleClient;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.TaskData;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnChangeListener;
import com.github.proteus1989.airexplorerdecryptersimpleclient.utils.FileChecker;
import com.github.proteus1989.airexplorerdecryptersimpleclient.utils.Pair;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@Log
public class GUI extends UserInterface {
    private final JTextPane jTextPane = new JTextPane();
    private final StyledDocument styledDocument = jTextPane.getStyledDocument();
    private final JScrollPane jScrollPane = new JScrollPane(jTextPane);

    private final SimpleAttributeSet greenPainter = new SimpleAttributeSet();
    private final SimpleAttributeSet redPainter = new SimpleAttributeSet();
    private final SimpleAttributeSet blackPainter = new SimpleAttributeSet();

    public GUI(AirExplorerDecrypterSimpleClient airExplorerDecrypterSimpleClient) {
        super(airExplorerDecrypterSimpleClient);
    }

    @Override
    public void log(String text) {
        logln(text, blackPainter);
    }

    @Override
    public void errorLog(String text) {
        logln(text, redPainter);
    }

    public void logln(String text) {
        logln(text, blackPainter);
    }

    public void logln(String text, AttributeSet keyWord) {
        log(text + "\r\n", keyWord);
    }

    public void log(String text, AttributeSet keyWord) {
        try {
            styledDocument.insertString(styledDocument.getLength(), text, keyWord);
        } catch (BadLocationException e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        Panel myPanel = new Panel();
        JFrame frame = new JFrame(AirExplorerFileDecrypterMain.name + " " + AirExplorerFileDecrypterMain.version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(myPanel);
        frame.setVisible(true);
        frame.pack();
        frame.setLocationRelativeTo(null);
        logln("Drop .cloudencoded /.cloudencoded2 files here\r\n");
    }

    public class Panel extends JPanel implements DropTargetListener {

        public Panel() {
            jTextPane.setEditable(false);
            DropTarget dt = new DropTarget(
                    this,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    this,
                    true);

            DropTarget dt2 = new DropTarget(
                    jScrollPane,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    this,
                    true);

            DropTarget dt3 = new DropTarget(
                    jTextPane,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    this,
                    true);

            StyleConstants.setForeground(redPainter, Color.red);
            StyleConstants.setForeground(greenPainter, Color.green);
            StyleConstants.setForeground(blackPainter, Color.black);

            jTextPane.setBackground(Color.white);
            GridLayout layout = new GridLayout(1, 1);
            setLayout(layout);
            add(jScrollPane);

            initProcessorListeners();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1200, 500);
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            repaint();
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            String pass = JOptionPane.showInputDialog(this, "Please, enter the password: ", "Password", JOptionPane.INFORMATION_MESSAGE);

            Transferable t = dtde.getTransferable();

            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                try {
                    for (DataFlavor dataFlavor : t.getTransferDataFlavors()) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> fileList = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        enqueueTasks(fileList, pass);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }

            repaint();
        }

        private void enqueueTasks(List<File> fileList, String pass) throws IOException {
            FileChecker fileChecker = new FileChecker(fileList.stream());
            fileChecker.logNotValidFiles(GUI.this::errorLog);

            fileList = fileChecker.getValidFiles();

            if (!fileList.isEmpty()) {
                Pair<List<Future<TaskData>>, List<File>> listListPair = airExplorerDecrypterSimpleClient.enqueueTasks(fileList, fileList.get(0).getParentFile(), pass);
                listListPair.getRight().stream()
                        .map(File::getName)
                        .map("Mismatching password for %s. Skipping it..."::formatted)
                        .forEach(GUI.this::errorLog);
            }
        }

        private void initProcessorListeners() {
            airExplorerDecrypterSimpleClient.addOnStartListener((srcFilepath, srcName, destFilepath, destName) -> logln("Decrypting " + destName + " in " + destFilepath));
            airExplorerDecrypterSimpleClient.addOnFinishListener((srcFilepath, srcName, destFilepath, destName) -> logln("Decrypted " + destName + " in " + destFilepath));
            airExplorerDecrypterSimpleClient.addOnChangeListener(new TaskProcessorOnChangeListener() {
                boolean firstCall = true;
                int percent = 0;

                Position pos = null;

                @Override
                public void onChange(double percent, long size, long processed,
                                     String srcFolderPath, String srcName, String destFolderPath, String destName) {
                    if (firstCall) {
                        log("[ ");
                        try {
                            pos = styledDocument.createPosition(styledDocument.getLength() - 1);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(AirExplorerFileDecrypterMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        for (int i = 0; i < 99; i++)
                            log(" ", greenPainter);
                        log("]");
                        firstCall = false;
                    }

                    int diff = (int) (percent - this.percent);
                    for (int i = 0; i < diff; i++)
                        try {
                            styledDocument.insertString(pos.getOffset(), "|", greenPainter);
                            styledDocument.remove(pos.getOffset(), 1);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(AirExplorerFileDecrypterMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    if (diff > 0)
                        this.percent = (int) percent;
                    if (percent == 100) {
                        firstCall = true;
                        this.percent = 0;
                        pos = null;
                    }
                }
            });
        }
    }
}
