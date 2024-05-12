package Catan;

import javax.swing.*;


public class Road extends JPanel {

    public boolean owned;
    public static ImageIcon roadSlantUp = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantUp.png");
    public static ImageIcon roadSlantDown = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantDown.png");
    public static ImageIcon roadVertical = Catan.getResizedIcon(6, (Tile.WIDTH - Tile.WIDTH/20)/2, "Catan/Icons/CatanRoadVertical.png");


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