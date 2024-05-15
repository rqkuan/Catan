package Catan;

import java.io.*;
import java.util.concurrent.Semaphore;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class Catan {

    public static Semaphore semaphore = new Semaphore(0);

    public static ImageIcon getResizedIcon(int width, int height, String path) {
        BufferedImage bimg = null;
        try {
            bimg = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImageIcon(bimg.getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    public static ImageIcon changeIconColor(ImageIcon icon, Color color) {
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
        return new ImageIcon(bimg);
    }

    public static void main(String[] args) throws InterruptedException {
        Board b = new Board();
        b.players.add(new Player(new Color(255, 0, 0)));
        b.players.add(new Player(new Color(0, 255, 0)));
        b.players.add(new Player(new Color(0, 0, 255)));
        
        for (int p = 0; p < b.players.size()*2; p++) {
            b.offerStartingBuild();
            b.nextPlayer();
        }
    }

}