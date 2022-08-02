package com.suapp.airexplorerfiledecrypter.userinterfaces.gui;

import com.suapp.airexplorerfiledecrypter.AirExplorerFileDecrypterMain;
import com.suapp.airexplorerfiledecrypter.streamwrapper.AirExplorerInputStreamWrapper;
import com.suapp.airexplorerfiledecrypter.utils.Document;
import com.suapp.airexplorerfiledecrypter.processor.Task;
import com.suapp.airexplorerfiledecrypter.processor.AirExplorerDecrypter;
import com.suapp.airexplorerfiledecrypter.processor.TaskExecutor.TaskProcessorOnProgressListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class GUI
{
    private final AirExplorerDecrypter proccessor = new AirExplorerDecrypter();
    private final JTextPane log = new JTextPane();
    private final JScrollPane scrollPane = new JScrollPane(log);
    
    private final SimpleAttributeSet greenPainter = new SimpleAttributeSet();
    private final SimpleAttributeSet redPainter = new SimpleAttributeSet();
    private final SimpleAttributeSet blackPainter = new SimpleAttributeSet();
    
    public void initGUI()
    {
        EventQueue.invokeLater(() ->
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
            {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Pane testPane = new Pane();
            JFrame frame = new JFrame(AirExplorerFileDecrypterMain.name + " " + AirExplorerFileDecrypterMain.version);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(testPane);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            testPane.println("Drop .cloudencoded/.cloudencoded2 files here\r\n", blackPainter);
        });
    }

    public class Pane extends JPanel implements DropTargetListener
    {

        public Pane()
        {
            log.setEditable(false);
            DropTarget dt = new DropTarget(
                    this,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    this,
                    true);

            DropTarget dt2 = new DropTarget(
                    scrollPane,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    this,
                    true);

            DropTarget dt3 = new DropTarget(
                    log,
                    DnDConstants.ACTION_COPY_OR_MOVE,
                    this,
                    true);

            StyleConstants.setForeground(redPainter, Color.RED);
            StyleConstants.setForeground(greenPainter, Color.green);
            StyleConstants.setForeground(blackPainter, Color.black);

            log.setBackground(Color.white);
            setBackground(Color.BLACK);
            GridLayout layout = new GridLayout(1, 1);
            setLayout(layout);
            add(scrollPane);

            initProcessorListeners();
        }

        @Override
        public Dimension getPreferredSize()
        {
            return new Dimension(1200, 500);
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde)
        {
            repaint();
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde)
        {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde)
        {
        }

        @Override
        public void dragExit(DropTargetEvent dte)
        {
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde)
        {
            String pass = JOptionPane.showInputDialog(this, "Please, enter the password: ", "Password", JOptionPane.INFORMATION_MESSAGE);

            Transferable t = dtde.getTransferable();

            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                try
                {
                    DataFlavor[] td = t.getTransferDataFlavors();
                    for (DataFlavor df : td)
                    {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> fileList = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        List<Task> list = getTasks(fileList, pass);
                        proccessor.addTasks(list);
                    }
                } catch (UnsupportedFlavorException | IOException ex)
                {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            

            repaint();
        }

        private List<Task> getTasks(List<File> fileList, String pass) throws IOException
        {
            List<Task> list = new LinkedList<>();
            
            for (File f : fileList)
            {
                File file = f;
                
                String name = file.getName();
                if (!(name.toLowerCase().endsWith(".cloudencoded2") || name.toLowerCase().endsWith(".cloudencoded")))
                {
                    println("Rejected " + name, redPainter);
                    continue;
                }
                
                FileInputStream fis = new FileInputStream(file);
                try
                {
                    AirExplorerInputStreamWrapper stream = new AirExplorerInputStreamWrapper(fis, new Document("filename", name).append("password", pass));
                    if (name.toLowerCase().endsWith(".cloudencoded2"))
                        println(name + " (" + AirExplorerInputStreamWrapper.decryptName(name, pass) + ") verified", blackPainter);
                    else
                        println(name + " (" + name.replace(".cloudencoded", "") + ") verified", blackPainter);
                    list.add(new Task(stream, file));
                    
                } catch (Exception e)
                {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, e);
                    println("Missmatching password for " + name + ". Skipping it...", redPainter);
                    fis.close();
                }
                
            }
            
            return list;
        }

        private void println(String text)
        {
            println(text, blackPainter);
        }

        public void println(String text, AttributeSet keyWord)
        {
            print(text + "\r\n", keyWord);
        }

        public void print(String text, AttributeSet keyWord)
        {
            try
            {
                StyledDocument doc = log.getStyledDocument();
                doc.insertString(doc.getLength(), text, keyWord);
            } catch (BadLocationException e)
            {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        private void initProcessorListeners()
        {
            proccessor.addOnStartListener((srcFilepath, srcName, destFilepath, destName) -> println("Decrypting " + destName + " in " + destFilepath, blackPainter));
            proccessor.addOnFinishListener((srcFilepath, srcName, destFilepath, destName) -> Pane.this.println("Decrypted " + destName + " in " + destFilepath));
            proccessor.addOnChangeListener(new TaskProcessorOnProgressListener()
            {
                boolean firstCall = true;
                int percent = 0;
                StyledDocument doc = log.getStyledDocument();
                Position pos = null;

                @Override
                public void onChangeProgress(double percent, long size, long processed)
                {
                    if (firstCall)
                    {
                        print("[ ", blackPainter);
                        try
                        {
                            pos = doc.createPosition(doc.getLength() - 1);
                        } catch (BadLocationException ex)
                        {
                            Logger.getLogger(AirExplorerFileDecrypterMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        for (int i = 0; i < 99; i++)
                            print(" ", greenPainter);
                        println("]", blackPainter);
                        firstCall = false;
                    }

                    int diff = (int) (percent - this.percent);
                    for (int i = 0; i < diff; i++)
                        try
                        {
                            doc.insertString(pos.getOffset(), "|", greenPainter);
                            doc.remove(pos.getOffset(), 1);
                        } catch (BadLocationException ex)
                        {
                            Logger.getLogger(AirExplorerFileDecrypterMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    if (diff > 0)
                        this.percent = (int) percent;
                    if (percent == 100)
                    {
                        println("");
                        firstCall = true;
                        this.percent = 0;
                        pos = null;
                    }
                }
            });
        }

    }
}
