import org.bytedeco.opencv.opencv_core.Rect;

import java.util.HashMap;
import java.util.Map;

public class JSONUtil {

    public static HyperLinkJSON HyperLinkToJSON(HyperLink link) {
        HyperLinkJSON ret = new HyperLinkJSON();
        ret.setStartFrame(link.getStartFrame());
        ret.setEndFrame(link.getEndFrame());
        ret.setName(link.getName());
        ret.setJumpToFrameNum(link.getJumpToFrameNum());
        ret.setJumToFolderPath(link.getJumToFolderPath());

        HashMap<Integer, RectJSON> FrameNumToROI = new HashMap<>();

        for (Map.Entry<Integer, Rect> entry : link.getFrameNumToROI().entrySet()) {
            Rect rect = entry.getValue();
            RectJSON rectJSON = new RectJSON();
            rectJSON.setX(rect.x());
            rectJSON.setY(rect.y());
            rectJSON.setW(rect.width());
            rectJSON.setH(rect.height());
            FrameNumToROI.put(entry.getKey(), rectJSON);
        }

        ret.setFrameNumToROI(FrameNumToROI);

        return ret;
    }

    public static HyperLink JSONToHyperLink(HyperLinkJSON link) {
        HyperLink ret = new HyperLink();
        ret.setStartFrame(link.getStartFrame());
        ret.setEndFrame(link.getEndFrame());
        ret.setName(link.getName());
        ret.setJumpToFrameNum(link.getJumpToFrameNum());
        ret.setJumToFolderPath(link.getJumToFolderPath());

        HashMap<Integer, Rect> FrameNumToROI = new HashMap<>();

        for (Map.Entry<Integer, RectJSON> entry : link.getFrameNumToROI().entrySet()) {
            RectJSON rect = entry.getValue();
            FrameNumToROI.put(entry.getKey(), new Rect(rect.getX(), rect.getY(), rect.getW(), rect.getH()));
        }

        ret.setFrameNumToROI(FrameNumToROI);

        return ret;
    }
}
