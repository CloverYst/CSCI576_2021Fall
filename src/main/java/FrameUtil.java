import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;

public class FrameUtil {

    public static final int FRAME_WIDTH = 352;

    public static final int FRAME_HEIGHT = 288;


    /**
     * read frame, frame width and height are fixed
     *
     * @param imgPath
     * @return
     */
    public static BufferedImage readFrame(String imgPath) {
        try {

            BufferedImage img = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);

            int frameLength = FRAME_WIDTH * FRAME_HEIGHT * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < FRAME_HEIGHT; y++) {
                for (int x = 0; x < FRAME_WIDTH; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + FRAME_HEIGHT * FRAME_WIDTH];
                    byte b = bytes[ind + FRAME_HEIGHT * FRAME_WIDTH * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }

            return img;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isCoordinateInROI(int x, int y, Rect rect) {
        System.out.println(x + " " + y + " " + rect.x() + " " + rect.y() + " " + rect.width() + " " + rect.height());
        return x >= rect.x() && x <= (rect.x() + rect.width()) && y >= rect.y() && y <= (rect.y() + rect.height());
    }

    public static Mat BufferedImageToMat(BufferedImage img) {

        Mat mat = new Mat(img.getHeight(), img.getWidth(), CV_8UC3);

        int r, g, b;
        UByteRawIndexer indexer = mat.createIndexer();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);

                r = (byte) ((rgb >> 0) & 0xFF);
                g = (byte) ((rgb >> 8) & 0xFF);
                b = (byte) ((rgb >> 16) & 0xFF);

                indexer.put(y, x, 0, r);
                indexer.put(y, x, 1, g);
                indexer.put(y, x, 2, b);
            }
        }
        indexer.release();
        return mat;
    }

    public static boolean shouldTrack(BufferedImage img1, Rect rect1, BufferedImage img2, Rect rect2) {
        int diff = 0;
        int maxChange = 0;
        //System.out.println(rect1.x() + " " + rect1.width() + " " + rect1.y() + " " + rect2.height());
        for (int y = 0; y < rect1.height(); y++) {
            for (int x = 0; x < rect1.width(); x++) {
                if (((rect1.x() + x) >= 0 && (rect1.x() + x) < FRAME_WIDTH && (rect1.y() + y) >= 0 &&
                        (rect1.y() + y) < FRAME_HEIGHT) && ((rect2.x() + x) >= 0 && (rect2.x() + x) < FRAME_WIDTH && (rect2.y() + y) >= 0 &&
                        (rect2.y() + y) < FRAME_HEIGHT)) {
                    int r1 = getRed(img1.getRGB(rect1.x() + x, rect1.y() + y));
                    int g1 = getGreen(img1.getRGB(rect1.x() + x, rect1.y() + y));
                    int b1 = getBlue(img1.getRGB(rect1.x() + x, rect1.y() + y));
                    int r2 = getRed(img2.getRGB(rect2.x() + x, rect2.y() + y));
                    int g2 = getGreen(img2.getRGB(rect2.x() + x, rect2.y() + y));
                    int b2 = getBlue(img2.getRGB(rect2.x() + x, rect2.y() + y));
                    diff += (Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2));
                    maxChange += (255 * 3);
                }
            }
        }
        return diff > maxChange/6;
    }


    private static int getRed(int pixel) {
        return (pixel & 0x00ff0000) >> 16;
    }

    private static int getGreen(int pixel) {
        return (pixel & 0x0000ff00) >> 8;
    }

    private static int getBlue(int pixel) {
        return pixel & 0x000000ff;
    }
}
