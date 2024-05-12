package Catan;

import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.image.*;

public class Catan {

    public static ImageIcon getResizedIcon(int width, int height, String path) {
        BufferedImage bimg = null;
        try {
            bimg = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImageIcon(bimg.getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public static void changeIconColor(ImageIcon icon, Color color) {
        Image img = icon.getImage();
        BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimg.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        for (int x = 0; x < bimg.getWidth(); x++) {
            for (int y = 0; y < bimg.getHeight(); y++) {
                int alpha = new Color(bimg.getRGB(x, y), true).getAlpha();
                Color colorShift = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                bimg.setRGB(x, y, colorShift.getRGB());
            }
        }
        icon = new ImageIcon(bimg);
    }

    public static void main(String[] args) {
        Board b = new Board();
    }

}