import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_tracking.TrackerCSRT;
import org.bytedeco.opencv.opencv_video.Tracker;
import org.bytedeco.opencv.opencv_video.TrackerMIL;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 所有字段可以通过get方法获取
 */
@Data
public class VideoController {

    private static final String HYPERLINK_FILE_NAME = "hyperlink.json"; //hyperlink保存和读取的文件名
    private String folderPath; //视频文件夹地址
    private int curFrameNum; //当前帧地址
    private String[] framePaths; //当前视频所有帧的文件名，从1开始
    private BufferedImage curFrame; //当前播放或者编辑的帧
    private ArrayList<HyperLink> hyperLinks; //当前视频所有的hyperlink

    /**
     * 视频控制器，通过视频地址创建，创建时会读取文件下所有rgb文件，并将当前帧设定为1，
     * 同时读取文件夹底下的hyperlink.json文件并加载
     *
     * @param folderPath
     */
    public VideoController(String folderPath) {
        try {
            this.folderPath = folderPath;

            File f = new File(folderPath);

            if (!f.isDirectory()) {
                throw new Exception("invalid video folder");
            }

            framePaths = f.list((File dir, String name) -> name.endsWith(".rgb"));

            Arrays.sort(framePaths);

            if (framePaths != null && framePaths.length <= 0) {
                throw new Exception("empty folder");
            }

            curFrameNum = 1;

            curFrame = getFrameByNum(curFrameNum);

            hyperLinks = new ArrayList<>();

            if (hasHyperLinkFile()) {
                loadHyperLinkFile();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        VideoController controller = new VideoController("/Users/wentaoni/Desktop/AIFilmOne");
        System.out.println(controller.hyperLinks.toString());
    }

    /**
     * move to given frame by number, the function will set current frame and number to the given number
     * 移动到某一帧，会把当前帧和当前帧数设置为给的参数，配合滑动条或者视频播放使用
     *
     * @param num
     */
    public void moveToFrameByNum(int num) {

        curFrameNum = num;

        curFrame = getFrameByNum(num);
    }

    /**
     * create a hyperlink based on current frame
     * 在当前帧创建一个hyperlink
     *
     * @param ROI the region of interest created by user (a rectangle)
     * @return
     */
    public HyperLink createHyperLink(Rect ROI, String linkName, int startFrameNum, int jumpToFrameNum, String jumpToFolderPath) {
        //search the ROI for next 100 frames
        HashMap<Integer, Rect> FrameNumToROI = trackROI(ROI, startFrameNum, Math.min(startFrameNum + 500, framePaths.length - 1));

        int endFrameNum = startFrameNum;

        // get the endFrameNum of hyperlink
        for (Map.Entry<Integer, Rect> entry : FrameNumToROI.entrySet()) {
            endFrameNum = Math.max(startFrameNum, entry.getKey());
        }

        HyperLink hyperLink = new HyperLink();

        hyperLink.setStartFrame(startFrameNum);
        hyperLink.setEndFrame(endFrameNum);
        hyperLink.setName(linkName);
        hyperLink.setJumpToFrameNum(jumpToFrameNum);
        hyperLink.setJumToFolderPath(jumpToFolderPath);
        hyperLink.setFrameNumToROI(FrameNumToROI);

        return hyperLink;
    }

    /**
     * 往当前视频里添加hyperlink
     *
     * @param link
     */
    public void addHyperLink(HyperLink link) {
        hyperLinks.add(link);
    }

    /**
     * 根据名字获取HyperLink
     * @param name
     * @return
     */
    public HyperLink getHyperLinkByName(String name) {
        if (this.hyperLinks != null) {
            for (HyperLink hyperLink : this.hyperLinks) {
                if (hyperLink.getName().equals(name)) {
                    return hyperLink;
                }
            }
        }
        return null;
    }

    /**
     * check if the current folder has a hyperlink file
     * 检查当前视频文件夹是否有已经保存的hyperlink文件
     *
     * @return
     */
    public boolean hasHyperLinkFile() {
        File f = new File(folderPath + "/" + HYPERLINK_FILE_NAME);
        return f.exists();
    }

    /**
     * save hyperlink file as json
     * 保存当前添加的所有hyperlink
     *
     * @throws IOException
     */
    public void saveHyperLinks() {
        File f = new File(folderPath + "/" + HYPERLINK_FILE_NAME);
        if (f.exists()) {
            f.delete();
        }

        ArrayList<HyperLinkJSON> hyperLinkJSONS = new ArrayList<>();

        for (HyperLink hyperLink : hyperLinks) {
            HyperLinkJSON hyperLinkJson = JSONUtil.HyperLinkToJSON(hyperLink);
            hyperLinkJSONS.add(hyperLinkJson);
        }

        try {
            f.createNewFile();
            FileUtils.writeStringToFile(f, JSON.toJSONString(hyperLinkJSONS), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load hyperlink.json to an ArrayList of HyperLink
     * 加载hyperlink文件，调用时需要检查是否已有hyperlink
     *
     * @return if the load succeed
     */
    private boolean loadHyperLinkFile() {
        try {
            File f = new File(folderPath + "/" + HYPERLINK_FILE_NAME);
            JSONArray jsonArray = JSON.parseArray(FileUtils.readFileToString(f, Charset.defaultCharset()));
            ArrayList<HyperLink> newHyperLinks = new ArrayList<>();

            for (Object hyperLinkObject : jsonArray) {
                HyperLinkJSON hyperLinkJSON = JSONObject.parseObject(hyperLinkObject.toString(), HyperLinkJSON.class);
                newHyperLinks.add(JSONUtil.JSONToHyperLink(hyperLinkJSON));
            }
            hyperLinks = newHyperLinks;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * get all hyperlink on the given frameNum
     * 获取当前帧所包含的所有hyperlink
     *
     * @param frameNum
     * @return
     */
    public ArrayList<HyperLink> getAllHyperLinksByFrameNum(int frameNum) {

        ArrayList<HyperLink> hyperLinkOnFrame = new ArrayList<>();

        for (HyperLink hyperLink : hyperLinks) {
            if (frameNum >= hyperLink.getStartFrame() && frameNum < hyperLink.getEndFrame()) {
                hyperLinkOnFrame.add(hyperLink);
            }
        }

        return hyperLinkOnFrame;
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
        for (HyperLink hyperLink : this.hyperLinks) {
            //System.out.println(hyperLink);
            Rect rect = hyperLink.getROIByFrameNum(this.curFrameNum);
            //System.out.println(rect);
            if (rect != null && FrameUtil.isCoordinateInROI(x, y, rect)) {
                return hyperLink;
            }
        }
        return null;
    }

    /**
     * get the ROI from the startFrame to endFrame
     * 在后续100帧中寻找和当前帧选中区域（长方形）所匹配的区域
     *
     * @param ROI        the region of interest created by user
     * @param startFrame
     * @param endFrame
     * @return ROI by frameNum
     */
    private HashMap<Integer, Rect> trackROI(Rect ROI, int startFrame, int endFrame) {
        HashMap<Integer, Rect> FrameNumToROI = new HashMap<>();

        Tracker tracker = TrackerCSRT.create();

        tracker.init(FrameUtil.BufferedImageToMat(getFrameByNum(startFrame)), ROI);

        Rect curROI = new Rect(ROI.x(), ROI.y(), ROI.width(), ROI.height());

        FrameNumToROI.put(startFrame, ROI);

        int max = 255 * 3 * ROI.width() * ROI.height();

        BufferedImage prevImage = getFrameByNum(startFrame);

        for (int i = startFrame + 1; i < endFrame; i++) {
            BufferedImage cur = getFrameByNum(i);
            Rect ori = curROI;

            Mat curMat = FrameUtil.BufferedImageToMat(getFrameByNum(i));

            if (!tracker.update(curMat, curROI)) {
                break;
            }

            if (FrameUtil.shouldTrack(prevImage, ori, cur, curROI)) {
                break;
            }

            FrameNumToROI.put(i, new Rect(curROI.x(), curROI.y(), curROI.width(), curROI.height()));
            prevImage = cur;
        }

        return FrameNumToROI;
    }

    /**
     * get frame by number, the number starts at one
     * 获取某一帧的图片
     *
     * @param num
     * @return
     */
    private BufferedImage getFrameByNum(int num) {
        return FrameUtil.readFrame(folderPath + '/' + framePaths[num - 1]);
    }
}
