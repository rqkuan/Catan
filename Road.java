package Catan;

import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;


public class Road extends JPanel {

    public boolean owned;
    public static ImageIcon roadSlantUp;
    static {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("Catan/Icons/CatanRoadSlantUp.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int actualtw = (Tile.WIDTH - Tile.WIDTH/20);
        Image dimg = img.getScaledInstance(actualtw/2, (int)(actualtw/2 * 0.577), Image.SCALE_DEFAULT);
        roadSlantUp = new ImageIcon(dimg);
    }
    public static ImageIcon roadSlantDown;
    static {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("Catan/Icons/CatanRoadSlantDown.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int actualtw = (Tile.WIDTH - Tile.WIDTH/20);
        Image dimg = img.getScaledInstance(actualtw/2, (int)(actualtw/2 * 0.577), Image.SCALE_DEFAULT);
        roadSlantDown = new ImageIcon(dimg);
    }
    public static ImageIcon roadVertical;
    static {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("Catan/Icons/CatanRoadVertical.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image dimg = img.getScaledInstance(6, (Tile.WIDTH - Tile.WIDTH/20)/2, Image.SCALE_DEFAULT);
        roadVertical = new ImageIcon(dimg);
    }


    public Road(ImageIcon roadImage) {
        setOpaque(false);
        setLayout(null);
        
        //Road Image
        JLabel iconDisplay = new JLabel(roadImage);
        add(iconDisplay);
        iconDisplay.setBounds(0, 0, roadImage.getIconWidth(), roadImage.getIconHeight());

        // //Button for road placement
        // TileButton tb = new TileButton();
        // add(tb);
        // tb.setOpaque(true);
        // tb.setBounds(getWidth()/2 - 10, getHeight()/2 - 10, 20, 20);
        // setComponentZOrder(tb, 0);

        validate();
    }

}