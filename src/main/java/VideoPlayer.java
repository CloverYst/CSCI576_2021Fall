import lombok.Data;
import lombok.SneakyThrows;
import org.bytedeco.opencv.opencv_core.Rect;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@Data
public class VideoPlayer extends Thread {

    private ImagePanel imagePanel;

    private VideoController videoController;

    private PlaySound playSound;

    private String audioFile;

    private VideoPlayerState videoPlayerState;

    private JSlider slider;

    private JLabel frameNumLabel;

    /**
     * 用文件地址创建一个VideoPlayer，创建时默认在暂停状态
     *
     * @param folderPath
     */
    public VideoPlayer(String folderPath) {
        this.videoController = new VideoController(folderPath);
        this.imagePanel = new ImagePanel(videoController.getCurFrame());
        this.slider = new JSlider();

        frameNumLabel = new JLabel();
        frameNumLabel.setBounds(30 + 352, 400, 200, 25);
        frameNumLabel.setText("Frame 1");

        slider = new JSlider(1, 9000);
        slider.setBounds(22, 380, 375, 20);
        slider.setValue(1);
        slider.setPaintTicks(true);

        slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int num = source.getValue();

            VideoPlayerState originalState = this.videoPlayerState;

            //System.out.println(num);
            int diff = Math.abs(num - videoController.getCurFrameNum());

            if (diff > 10) {
                this.pauseVideo();
            }

            moveToFrameNum(num);

            //System.out.println(diff);
            if (diff > 10) {
                if (originalState == VideoPlayerState.PLAYING) {
                    //System.out.println(diff);
                    this.resumeVideo();
                }
                //playSound.setSoundPos(videoController.getCurFrameNum());
            }


            frameNumLabel.setText("Playing Frame " + (num));
        });

        this.loadAudioFile();
        this.playSound = new PlaySound(this.videoController.getFolderPath() + "/" + this.audioFile);
        this.videoPlayerState = VideoPlayerState.PAUSE;
        this.moveToFrameNum(1);
        this.start();
    }

    public static void main(String[] args) throws InterruptedException {
        VideoPlayer videoPlayer = new VideoPlayer("/Users/wentaoni/Desktop/AIFilmOne");
        videoPlayer.moveToFrameNum(8500);
    }

    public void jumpToOtherFile(String folderPath, int frameNum) {
        this.videoController = new VideoController(folderPath);
        this.loadAudioFile();
        this.playSound.pauseSound();
        this.playSound = new PlaySound(this.videoController.getFolderPath() + "/" + this.audioFile);
        this.slider.setValue(frameNum);
        this.resumeVideo();
    }

    public ImagePanel getImagePanel() {
        return this.imagePanel;
    }

    /**
     * 读取声音文件并保存
     */
    private void loadAudioFile() {

        File f = new File(videoController.getFolderPath());

        String af = Objects.requireNonNull(f.list((File dir, String name) -> name.endsWith(".wav")))[0];

        this.audioFile = af;
    }

    /**
     * 暂停
     */
    public void pauseVideo() {
        videoPlayerState = VideoPlayerState.PAUSE;
        playSound.pauseSound();
    }

    /**
     * 播放视频
     */
    public void resumeVideo() {
        playSound.play(videoController.getCurFrameNum());
        synchronized (this) {
            videoPlayerState = VideoPlayerState.PLAYING;
            this.notify();
        }
    }

    public void stopVideo() {
        videoPlayerState = VideoPlayerState.PAUSE;
        playSound.pauseSound();
        slider.setValue(1);
    }

    /**
     * 将视频移动到某一帧，配合视频播放或者滑动条使用
     *
     * @param num
     */
    public void moveToFrameNum(int num) {
        this.videoController.moveToFrameByNum(num);
        ArrayList<Rect> rectList = new ArrayList<>();
        ArrayList<HyperLink> links = videoController.getAllHyperLinksByFrameNum(num);
        if (!links.isEmpty()) {
            for (HyperLink link : links) {
                if (link.getROIByFrameNum(num) != null) {
                    rectList.add(link.getROIByFrameNum(num));
                }
            }
        }
        this.imagePanel.setImg(this.videoController.getCurFrame(), rectList);
    }

    public int getCurFrameNum() {
        return videoController.getCurFrameNum();
    }

    /**
     * return the hyperlink on click, if there is no hyperlink match,
     * return null
     * 返回当前帧点击区域对应的hyperlink，如果没有返回null，如果有多个对应的hyperlink
     * 返回最先创建的hyperlink
     *
     * @param x
     * @param y
     * @return
     */
    public HyperLink getHyperLinkOnClick(int x, int y) {
        return this.videoController.getHyperLinkOnClick(x, y);
    }

    @SneakyThrows
    @Override
    public void run() {

        long prev = System.currentTimeMillis();

        while (true) {
            if (videoController.getCurFrameNum() == videoController.getFramePaths().length) {
                this.stopVideo();
            } else if (videoPlayerState == VideoPlayerState.PAUSE) {
                synchronized (this) {
                    this.wait();
                }
            }

            long current = System.currentTimeMillis();
            long interval = current - prev;
            if (interval < (videoController.getCurFrameNum() % 3 == 0 ? 34 : 33)) {
                continue;
            }
            prev = current;
            slider.setValue(videoController.getCurFrameNum() + 1);
        }
    }
}
