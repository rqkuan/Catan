package Catan;

import javax.swing.*;


public class Road extends JPanel {

    public boolean owned;
    public static ImageIcon roadSlantUp = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantUp.png");
    public static ImageIcon roadSlantDown = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantDown.png");
    public static ImageIcon roadVertical = Catan.getResizedIcon(6, (Tile.HEIGHT - Tile.HEIGHT/15)/2, "Catan/Icons/CatanRoadVertical.png");
    private ImageIcon icon;

    public Road(ImageIcon icon) {
        this.icon = icon;
        setOpaque(false);
        setLayout(null);
        
        //Road Image
        JLabel iconDisplay = new JLabel();
        add(iconDisplay);
        iconDisplay.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());

        // //Button for road placement
        // TileButton tb = new TileButton();
        // add(tb);
        // tb.setOpaque(true);
        // tb.setBounds(getWidth()/2 - 10, getHeight()/2 - 10, 20, 20);
        // setComponentZOrder(tb, 0);

        validate();
    }

}