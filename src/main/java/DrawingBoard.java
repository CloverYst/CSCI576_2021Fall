import org.bytedeco.opencv.opencv_core.Rect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DrawingBoard extends JComponent {
    ArrayList<Shape> shapes = new ArrayList<Shape>();
    Point startDrag, endDrag;
    BufferedImage image;
    Rect selectedRect;
    ArrayList<Rect> rectList = new ArrayList<>();
    int width = 352;
    int height = 288;

    public DrawingBoard(BufferedImage image, Rect selectedRect, ArrayList<Rect> rectList, OnBoardDrewListener onBoardDrewListener) {
        this.image = image;
        this.selectedRect = selectedRect;
        this.rectList = rectList;

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startDrag = new Point(e.getX(), e.getY());
                endDrag = startDrag;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                Shape r = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
                shapes.add(r);

                if (startDrag.x > endDrag.x && startDrag.y > endDrag.y) {
                    onBoardDrewListener.onBoardDrew(new Rect(endDrag.x, endDrag.y, startDrag.x - endDrag.x, startDrag.y - endDrag.y));
                } else if (startDrag.x > endDrag.x) {
                    onBoardDrewListener.onBoardDrew(new Rect(endDrag.x, startDrag.y, startDrag.x - endDrag.x, endDrag.y - startDrag.y));
                } else if (startDrag.y > endDrag.y) {
                    onBoardDrewListener.onBoardDrew(new Rect(startDrag.x, endDrag.y, endDrag.x - startDrag.x, startDrag.y - endDrag.y));
                } else {
                    onBoardDrewListener.onBoardDrew(new Rect(startDrag.x, startDrag.y, endDrag.x - startDrag.x, endDrag.y - startDrag.y));
                }

                startDrag = null;
                endDrag = null;

                repaint();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endDrag = new Point(e.getX(), e.getY());
                repaint();
            }
        });
    }

    private void paintBackground(Graphics2D g2) {
        g2.drawImage(image, 0, 0, null);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintBackground(g2);
        Color[] colors = {Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE, Color.PINK};
        int colorIndex = 0;

        g2.setStroke(new BasicStroke(2));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

        for (Shape s : shapes) {
            g2.setPaint(colors[(colorIndex++) % 6]);
            g2.draw(s);
        }

        if (startDrag != null && endDrag != null) {
            g2.setPaint(Color.LIGHT_GRAY);
            Shape r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
            g2.draw(r);
        }

        if (!rectList.isEmpty()) {
            for (Rect rt : rectList) {
                if (rt != null) {
                    if (rt.equals(selectedRect)) {
                        g2.setPaint(Color.RED);
                    } else {
                        g2.setPaint(colors[(colorIndex++) % 6]);
                    }

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