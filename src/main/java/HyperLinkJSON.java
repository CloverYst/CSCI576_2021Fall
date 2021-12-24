import lombok.Data;

import java.util.HashMap;

/**
 * 使用的opencv库无法直接解析为JSON，所以需要生成一个类专门用于
 * JSON转换
 */
@Data
public class HyperLinkJSON {

    //The frame which the hyperlink begins
    private int startFrame;

    //The frame which the hyperlink ends
    private int endFrame;

    private String name;

    private int jumpToFrameNum;

    private String jumToFolderPath;

    //ROI Rectangle for each frame
    private HashMap<Integer, RectJSON> FrameNumToROI;
}


@Data
class RectJSON {
    private int x;

    private int y;

    private int w;

    private int h;
}