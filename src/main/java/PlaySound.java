import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class PlaySound {

    private Clip clip;

    public PlaySound(String audioFilePath) {
        File file = new File(audioFilePath);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            this.clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private long getMillisecondFromFrame(long nbFrame) {
        if (nbFrame < 1) return 0;

        return (nbFrame * 33) + ((nbFrame / 3) + 1);
    }

    public void pauseSound() {
        clip.stop();
    }

    public void setSoundPos(int frame) {
        long ms = getMillisecondFromFrame(frame);
        clip.setMicrosecondPosition(ms * 1000);
    }

    public void play(int frame) {
        clip.stop();
        long ms = getMillisecondFromFrame(frame);
        clip.setMicrosecondPosition(ms * 1000);
        clip.start();
    }
}
