import lombok.Data;
import org.bytedeco.opencv.opencv_core.Rect;

import java.util.HashMap;

@Data
public class HyperLink {

    //The frame which the hyperlink begins
    private int startFrame;

    //The frame which the hyperlink ends
    private int endFrame;

    private String name;

    private int jumpToFrameNum;

    private String jumToFolderPath;

    //ROI Rectangle for each frame
    private HashMap<Integer, Rect> FrameNumToROI;

    public Rect getROIByFrameNum(int frameNum) {
        return FrameNumToROI.getOrDefault(frameNum, null);
    }
}
