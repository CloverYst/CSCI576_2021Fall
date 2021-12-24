import org.apache.commons.lang3.StringUtils;
import org.bytedeco.opencv.opencv_core.Rect;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import javax.swing.JFileChooser;

public class VideoAuthor {
    private final int width = 352;
    private final int height = 288;
    private JFrame frame;
    private JButton cntButton;
    private JList<String> list;
    private DefaultListModel<String> dlm = new DefaultListModel<>();
    private String imgTwoPath;
    private BufferedImage imgOne;
    private BufferedImage imgTwo;
    private JSlider slider1;
    private JSlider slider2;
    private JLabel lb1;
    private VideoController videoController1;
    private VideoController videoController2;
    private JLabel prevLeftImage;
    private JLabel prevRightImage;
    private DrawingBoard prvDrawingBoard;
    private JPanel leftRectangle;
    private JPanel rightRectangle;
    private int frameOneNum = 1;
    private int frameTwoNum = 1;
    private int slider1Index = 1;
    private String linkName;
    private Rect rect;
    private Rect selectedRect;
    private ArrayList<String> linksAdded = new ArrayList<>();
    private final OnBoardDrewListener onBoardDrewListener = rect -> {
        this.rect = rect;
        frameOneNum = slider1Index;
    };

    public static void main(String[] args) {
        VideoAuthor ren = new VideoAuthor();
        ren.createFrame(ren);
    }

    private void createPlayerOne(JFrame frame) {
        leftRectangle = new JPanel();
        leftRectangle.setBorder(new LineBorder(new Color(0, 0, 0)));
        leftRectangle.setBounds(80, 200, width, height);
        frame.getContentPane().add(leftRectangle);
    }

    private void createPlayerTwo(JFrame frame) {
        rightRectangle = new JPanel();
        rightRectangle.setBorder(new LineBorder(new Color(0, 0, 0)));
        rightRectangle.setBounds(180 + width, 200, width, height);
        frame.getContentPane().add(rightRectangle);
    }

    private void createFrame(VideoAuthor ren) {
        if (ren == null) {
            return;
        }

        frame = new JFrame();
        frame.setSize(1000, 600);
        JPanel panel = new JPanel();
        frame.add(panel);
        frame.getContentPane().setLayout(null);

        JLabel actionLabel = new JLabel("Action : ");
        actionLabel.setBounds(40, 40, 70, 15);
        frame.getContentPane().add(actionLabel);

        JButton btn1 = new JButton("Import Primary video");
        btn1.setBounds(100, 30, 180, 25);
        frame.getContentPane().add(btn1);
        btn1.addActionListener(getFileActionListener(btn1, true));

        JButton btn2 = new JButton("Import Secondary video");
        btn2.setBounds(100, 60, 180, 25);
        frame.getContentPane().add(btn2);
        btn2.addActionListener(getFileActionListener(btn2, false));

        JButton btn3 = new JButton("Create new hyperlink");
        btn3.setBounds(100, 90, 180, 25);
        frame.getContentPane().add(btn3);
        btn3.addActionListener(createHyperlinkActionListener());

        JLabel selectLabel = new JLabel("Select Link: ");
        selectLabel.setBounds(320, 40, 100, 15);
        frame.getContentPane().add(selectLabel);

        list = new JList<>();
        list.setModel(dlm);
        dlm = (DefaultListModel<String>) list.getModel();
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(new LineBorder(new Color(0, 0, 0)));
        scrollPane.setBounds(400, 30, 180, 90);
        frame.getContentPane().add(scrollPane);
        list.addMouseListener(selectLinkMouseListener());

        cntButton = new JButton("Connect Video");
        cntButton.setBounds(640, 40, 140, 70);
        frame.getContentPane().add(cntButton);
        cntButton.setEnabled(false);
        cntButton.addActionListener(connectVideoActionListener());

        JButton saveButton = new JButton("Save File");
        saveButton.setBounds(820, 40, 120, 70);
        frame.getContentPane().add(saveButton);
        saveButton.addActionListener(saveVideoActionListener());

        slider1 = new JSlider(1, 9000, 1);
        slider1.setBounds(80, 520, 360, 20);
        slider1.setPaintTicks(true);
        frame.getContentPane().add(slider1);

        lb1 = new JLabel();
        lb1.setBounds(240, 500, 100, 20);
        lb1.setText("Frame 1");
        frame.getContentPane().add(lb1);

        slider1.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            slider1Index = source.getValue();
            showImageInFrame(slider1Index, true);
            lb1.setText("Frame " + (slider1Index));
        });

        slider2 = new JSlider(1, 9000, 1);
        slider2.setBounds(180 + width, 520, 360, 20);
        slider2.setPaintTicks(true);
        frame.getContentPane().add(slider2);

        JLabel lb2 = new JLabel();
        lb2.setBounds(345 + width, 500, 100, 20);
        lb2.setText("Frame 1");
        frame.getContentPane().add(lb2);

        slider2.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int index = source.getValue();
            showImageInFrame(index, false);
            lb2.setText("Frame " + (index));
        });

        createPlayerOne(frame);
        createPlayerTwo(frame);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private ActionListener getFileActionListener(JButton button, boolean isLeft) {
        return e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(button);
            if (result == JFileChooser.APPROVE_OPTION) {
                File fileDir = fileChooser.getSelectedFile();
//                System.out.println("Selected folder path: " + fileDir.getAbsolutePath());
                try {
                    if (isLeft) {
                        videoController1 = new VideoController(fileDir.getAbsolutePath());

                        // clear old links
                        dlm.clear();

                        // Add existing hyperlinks to select link panel
                        ArrayList<HyperLink> links = videoController1.getHyperLinks();
                        if (!links.isEmpty()) {
                            for (HyperLink link : links) {
                                linksAdded.add(link.getName());
                                dlm.addElement(link.getName());
                                list.setSelectedValue(link.getName(), true);
                            }
                        }
                    } else {
                        imgTwoPath = fileDir.getAbsolutePath();
                        videoController2 = new VideoController(fileDir.getAbsolutePath());
                    }
                    showImageInFrame(1, isLeft);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private void showImageInFrame(int num, boolean isLeftFrame) {
        if ((isLeftFrame && videoController1 == null) || (!isLeftFrame && videoController2 == null)) {
            return;
        }

        JLabel label;

        if (isLeftFrame) {
            frame.remove(leftRectangle);
            videoController1.moveToFrameByNum(num);
            imgOne = videoController1.getCurFrame();
            label = new JLabel(new ImageIcon(imgOne));
            label.setBounds(80, 200, width, height);

            ArrayList<Rect> rectList = new ArrayList<>();
            ArrayList<HyperLink> links = videoController1.getAllHyperLinksByFrameNum(num);
            if (!links.isEmpty()) {
                for (HyperLink link : links) {
                    if (link.getROIByFrameNum(num) != null) {
                        rectList.add(link.getROIByFrameNum(num));
                    }
                }
            }

            DrawingBoard box = new DrawingBoard(imgOne, selectedRect, rectList, onBoardDrewListener);
            box.setBounds(80, 200, width, height);
            frame.getContentPane().add(box);
            if (prvDrawingBoard != null) {
                frame.remove(prvDrawingBoard);
                refreshFrame();
            }
            prvDrawingBoard = box;

            if (prevLeftImage != null) {
                frame.remove(prevLeftImage);
                refreshFrame();
            }

            prevLeftImage = label;
        } else {
            frameTwoNum = num;
            frame.remove(rightRectangle);
            videoController2.moveToFrameByNum(num);
            imgTwo = videoController2.getCurFrame();
            label = new JLabel(new ImageIcon(imgTwo));
            label.setBounds(180 + width, 200, width, height);

            if (prevRightImage != null) {
                frame.remove(prevRightImage);
                refreshFrame();
            }

            prevRightImage = label;
        }

        frame.getContentPane().add(label);
        refreshFrame();
    }

    private void refreshFrame() {
        frame.revalidate();
        frame.repaint();
    }

    private ActionListener createHyperlinkActionListener() {
        return e -> {
            if (imgOne == null) {
                JOptionPane.showMessageDialog(null, "Primary Video is not prepared.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (rect == null || slider1Index != frameOneNum) {
                JOptionPane.showMessageDialog(null, "Bounding box hasn't been defined", "Error", JOptionPane.ERROR_MESSAGE);
                rect = null;
                return;
            }

            String oldLinkName = linkName;

            // set new link name
            linkName = JOptionPane.showInputDialog("Link name: ");

            while (true) {
                if (StringUtils.isBlank(linkName)) {
                    JOptionPane.showMessageDialog(null, "Link creation has been canceled", "Cancel", JOptionPane.INFORMATION_MESSAGE);
                    linkName = oldLinkName;
                    return;
                } else if (linksAdded.contains(linkName)) {
                    linkName = JOptionPane.showInputDialog("Link name already exists.\nPlease give another name: ");
                } else {
                    break;
                }
            }

            // show added link name
            dlm.addElement(linkName);
            list.setSelectedValue(linkName, true);

            // update jumpToFrameNum and create Hyperlink
            Thread thread = new Thread(() -> {
                try {
                    HyperLink link = videoController1.createHyperLink(rect, linkName, slider1Index, frameTwoNum, imgTwoPath);
                    videoController1.addHyperLink(link);
                } catch (Exception ev) {
                    ev.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Hyperlink creation has encountered a problem.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    linksAdded.add(linkName);
                    rect = null;
                    JOptionPane.showMessageDialog(null, "Hyperlink has been successfully created", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // enable connect video button
                    cntButton.setEnabled(true);
                }
            });
            thread.start();
        };
    }

    private MouseAdapter selectLinkMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JList source = (JList) e.getSource();
                String selectLinkName = source.getSelectedValue().toString();
                if (linksAdded.contains(selectLinkName)) {
                    if (e.getClickCount() == 2) {
                        source.setSelectionBackground(Color.red);

                        try {
                            HyperLink link = videoController1.getHyperLinkByName(selectLinkName);
                            int index = link.getStartFrame();
                            selectedRect = link.getROIByFrameNum(index);
                            showImageInFrame(index, true);
                            slider1.setValue(index);
                            lb1.setText("Frame " + (index));
                            selectedRect = null;
                        } catch (Exception ev) {
                            JOptionPane.showMessageDialog(null, "This hyperlink is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
//                        dlm.removeElement(source.getSelectedValue());
                        }
                    } else if (e.getClickCount() == 3) {
                        try {
                            String oldLinkName = selectLinkName;
                            HyperLink link = videoController1.getHyperLinkByName(selectLinkName);
                            selectLinkName = JOptionPane.showInputDialog("Edit link name: ");

                            while (true) {
                                if (StringUtils.isBlank(selectLinkName)) {
                                    JOptionPane.showMessageDialog(null, "Name change has been canceled", "Cancel", JOptionPane.INFORMATION_MESSAGE);
                                    break;
                                } else if (linksAdded.contains(selectLinkName)) {
                                    selectLinkName = JOptionPane.showInputDialog("Link name already exists.\nPlease give another name: ");
                                } else {
                                    linksAdded.remove(oldLinkName);
                                    linksAdded.add(selectLinkName);
                                    dlm.setElementAt(selectLinkName, source.getSelectedIndex());
                                    JOptionPane.showMessageDialog(null, "Link name has been updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                                    link.setName(selectLinkName);
                                    break;
                                }
                            }

                            list.setSelectedValue(selectLinkName, true);
                        } catch (Exception ev) {
                            JOptionPane.showMessageDialog(null, "This hyperlink is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
//                        dlm.removeElement(source.getSelectedValue());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Hyperlink is not ready yet.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
    }

    private ActionListener connectVideoActionListener() {
        return e -> {
            if (imgTwo == null) {
                JOptionPane.showMessageDialog(null, "Secondary Video is not prepared.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            HyperLink link = videoController1.getHyperLinkByName(linkName);
            link.setJumpToFrameNum(frameTwoNum);
            JOptionPane.showMessageDialog(null, "Video has been successfully connected", "Success", JOptionPane.INFORMATION_MESSAGE);

            cntButton.setEnabled(false);
        };
    }

    private ActionListener saveVideoActionListener() {
        return e -> {
            if (imgOne == null) {
                JOptionPane.showMessageDialog(null, "No videos have been loaded", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (videoController1.hasHyperLinkFile()) {
                if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, "File already exists. Do you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION))
                    return;
            }

            Thread thread = new Thread(() -> {
                try {
                    videoController1.saveHyperLinks();
                } catch (Exception ev) {
                    JOptionPane.showMessageDialog(null, "File save has encountered a problem.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    JOptionPane.showMessageDialog(null, "Metadata has been successfully saved", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            thread.start();
        };
    }
}