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
        
        for (; b.curPlayerIndex < b.players.size(); b.curPlayerIndex++) 
            b.offerStartingBuild();
        for (b.curPlayerIndex--; b.curPlayerIndex >= 0; b.curPlayerIndex--) {
            b.offerStartingBuild();
            int row = b.recentBuild.getRow();
            int column = b.recentBuild.getColumn();

            //The second starting building gives the player some initial resources too
            if (column % 2 == 0) {
                //Below the tile
                try {
                    Tile t = b.getTile(row-1, column/2 - row%2);
                    if (t != null) 
                        b.getCurPlayer().addResource(t.getResource(), 1);
                } catch (IndexOutOfBoundsException excpt) {}
                try {
                    Tile t = b.getTile(row, column/2);
                    if (t != null) 
                        b.getCurPlayer().addResource(t.getResource(), 1);
                } catch (IndexOutOfBoundsException excpt) {}
                try {
                    Tile t = b.getTile(row, column/2 - 1);
                    if (t != null)
                        b.getCurPlayer().addResource(t.getResource(), 1);
                } catch (IndexOutOfBoundsException excpt) {}
            } else {
                //Above the tile
                try {
                    Tile t = b.getTile(row, column/2);
                    if (t != null) 
                        b.getCurPlayer().addResource(t.getResource(), 1);
                } catch (IndexOutOfBoundsException excpt) {}
                try {
                    Tile t = b.getTile(row-1, column/2 - row%2);
                    if (t != null) 
                        b.getCurPlayer().addResource(t.getResource(), 1);
                } catch (IndexOutOfBoundsException excpt) {}
                try {
                    Tile t = b.getTile(row-1, column/2 + 1 - row%2);
                    if (t != null) 
                        b.getCurPlayer().addResource(t.getResource(), 1);
                } catch (IndexOutOfBoundsException excpt) {}
            }
        }
        b.curPlayerIndex = 0;
    }

}