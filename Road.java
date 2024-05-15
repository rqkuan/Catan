package Catan;

import java.awt.event.*;
import javax.swing.*;


public class Road extends JPanel {

    public boolean buildable = false;
    public static ImageIcon roadSlantUp = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantUp.png");
    public static ImageIcon roadSlantDown = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantDown.png");
    public static ImageIcon roadVertical = Catan.getResizedIcon(6, (Tile.HEIGHT - Tile.HEIGHT/15)/2, "Catan/Icons/CatanRoadVertical.png");
    public ImageIcon icon;
    public JLabel iconDisplay;
    public JButton button = new JButton();
    private Board board;

    public Road(ImageIcon icon, Board board) {
        this.board = board;
        this.icon = icon;
        setOpaque(false);
        setVisible(false);
        setLayout(null);

        //Road Image
        iconDisplay = new JLabel(icon);
        add(iconDisplay);

        //Button for road placement
        add(button);
        button.setOpaque(false);
        button.setVisible(false);
        button.setIcon(Corner.offerBuild);
        setComponentZOrder(button, 0);

        final Road road = this;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!road.buildable)
                    return;
                road.buildable = false;
                road.iconDisplay.setIcon(Catan.changeIconColor(road.icon, board.getCurPlayer().getColor()));
                
                //Add road functionality (update player's accessible corners)
                // <<Code Here>>

                Catan.semaphore.release();
            }
        });

        validate();
    }

}