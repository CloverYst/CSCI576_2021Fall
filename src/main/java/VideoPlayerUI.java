import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


public class VideoPlayerUI {
    //video format
    int width = 352;
    int height = 288;

    //VideoPlayer
    VideoPlayer videoPlayer;

    //video player components
    JFrame frame;
    ImagePanel imagePanel;
    JButton selectBtn;
    JButton playBtn;
    JButton pauseBtn;
    JButton stopBtn;

    int num = 0;

    public VideoPlayerUI() {
        this.frame = new JFrame("Video Player");
        frame.setSize(520, 480);

        JPanel panel = new JPanel();
        frame.add(panel);
        frame.getContentPane().setLayout(null);

        //Buttons:
        //1.select file button
        selectBtn = new JButton("Select File");
        selectBtn.setBounds(20, 20, 100, 50);
        ImageIcon selectIcon = new ImageIcon("src/main/resources/open-folder.png");
        Image image = selectIcon.getImage().getScaledInstance(selectBtn.getWidth() * 3 / 5, selectBtn.getHeight() * 3 / 5, Image.SCALE_SMOOTH);
        selectIcon = new ImageIcon(image);
        selectBtn.setIcon(selectIcon);
        frame.getContentPane().add(selectBtn);

        //2.play video button
        playBtn = new JButton();
        playBtn.setBounds(60 + width, 100, 70, 70);
        playBtn.setText("play");
        ImageIcon playIcon = new ImageIcon("src/main/resources/play.png");
        Image image1 = playIcon.getImage().getScaledInstance(playBtn.getWidth() * 3 / 5, playBtn.getHeight() * 3 / 5, Image.SCALE_SMOOTH);
        playIcon = new ImageIcon(image1);
        playBtn.setIcon(playIcon);
        frame.getContentPane().add(playBtn);

        //3.pause video button
        pauseBtn = new JButton();
        pauseBtn.setBounds(60 + width, 180, 70, 70);
        pauseBtn.setText("pause");
        ImageIcon pauseIcon = new ImageIcon("src/main/resources/pause.png");
        Image image0 = pauseIcon.getImage().getScaledInstance(pauseBtn.getWidth() * 3 / 5, pauseBtn.getHeight() * 3 / 5, Image.SCALE_SMOOTH);
        pauseIcon = new ImageIcon(image0);
        pauseBtn.setIcon(pauseIcon);
        frame.getContentPane().add(pauseBtn);


        //4.stop video button
        stopBtn = new JButton();
        stopBtn.setBounds(60 + width, 260, 70, 70);
        stopBtn.setText("stop");
        ImageIcon stopIcon = new ImageIcon("src/main/resources/stop.png");
        Image image2 = stopIcon.getImage().getScaledInstance(stopBtn.getWidth() * 3 / 5, stopBtn.getHeight() * 3 / 5, Image.SCALE_SMOOTH);
        stopIcon = new ImageIcon(image2);
        stopBtn.setIcon(stopIcon);
        frame.getContentPane().add(stopBtn);
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                videoPlayer.stopVideo();
            }
        });


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        InitializeFrame();

    }

    public static void main(String[] args) {
        VideoPlayerUI ui = new VideoPlayerUI();
    }

    public void InitializeFrame() {
        JPanel emptyFrame = new JPanel();
        emptyFrame.setBorder(new LineBorder(new Color(0, 0, 0)));
        emptyFrame.setBounds(30, 80, width, height);
        frame.getContentPane().add(emptyFrame);

        selectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(selectBtn);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String folderPath = selectedFile.getAbsolutePath();
                    System.out.println("folder path: " + folderPath);
                    if (videoPlayer == null) {
                        videoPlayer = new VideoPlayer(folderPath);
                        imagePanel = videoPlayer.getImagePanel();
                        imagePanel.setBounds(30, 80, width, height);
                        imagePanel.setVisible(true);
                        Border blackLine = BorderFactory.createLineBorder(Color.black);
                        imagePanel.setBorder(blackLine);

                        frame.remove(emptyFrame);
                        frame.revalidate();
                        frame.repaint();

                        frame.getContentPane().add(imagePanel);
                        imagePanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                int x = e.getX(), y = e.getY();
                                //System.out.println(x + " " + y);
                                HyperLink hl = videoPlayer.getHyperLinkOnClick(x, y);
                                //System.out.println(videoPlayer.getVideoController().getHyperLinks().toString());
                                if (hl != null) {
                                    videoPlayer.pauseVideo();
                                    videoPlayer.jumpToOtherFile(hl.getJumToFolderPath(), hl.getJumpToFrameNum());
                                }
                            }
                        });

                        frame.getContentPane().add(videoPlayer.getSlider());
                        frame.getContentPane().add(videoPlayer.getFrameNumLabel());
                    } else {
                        videoPlayer.pauseVideo();
                        videoPlayer.jumpToOtherFile(folderPath, 1);
                    }
                }
            }
        });

        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                videoPlayer.resumeVideo();
            }
        });

        pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                videoPlayer.pauseVideo();
            }
        });

    }
}