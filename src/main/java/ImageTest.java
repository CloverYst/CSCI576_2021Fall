import org.bytedeco.opencv.opencv_core.Rect;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class ImageTest {

    public static void main(String[] args) throws InterruptedException {
        VideoController controller = new VideoController("/Users/wentaoni/Desktop/AIFilmOne");
        ImagePanel panel = new ImagePanel(controller.getCurFrame());

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        Thread.sleep(1000);
        controller.moveToFrameByNum(200);
    }
}

class ImagePanel extends JPanel {

    private Image img;

    private ArrayList<Rect> rectList;

    public ImagePanel(String img) {
        this(new ImageIcon(img).getImage());
    }

    public ImagePanel(Image img) {
        this.img = img;
        this.rectList = new ArrayList<>();
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    public void setImg(Image img, ArrayList<Rect> rectList) {
        this.img = img;
        this.rectList = rectList;
        repaint();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(img, 0, 0, null);
        Color[] colors = {Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE, Color.PINK};
        int colorIndex = 0;

        g2.setStroke(new BasicStroke(2));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

        if (rectList != null && !rectList.isEmpty()) {
            for (Rect rt : rectList) {
                if (rt != null) {
                    g2.setPaint(colors[(colorIndex++) % 6]);
                    Shape r = makeRectangle(rt.x(), rt.y(), rt.x() + rt.width(), rt.y() + rt.height());
                    g2.draw(r);
                } else {
                    throw new RuntimeException("rectList is empty");
                }
            }
        }
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
}
