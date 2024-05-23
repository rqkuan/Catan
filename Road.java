package Catan;

import java.awt.event.*;
import javax.swing.*;


public class Road extends JPanel {

    public static ImageIcon roadSlantUp = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantUp.png");
    public static ImageIcon roadSlantDown = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantDown.png");
    public static ImageIcon roadVertical = Catan.getResizedIcon(6, (Tile.HEIGHT - Tile.HEIGHT/15)/2, "Catan/Icons/CatanRoadVertical.png");
    
    public ImageIcon icon;
    public JLabel iconDisplay;
    public JButton button = new JButton();
    public boolean buildable = false;
    
    private Player owner;
    private Board board;
    private int row, column;

    public Road(ImageIcon icon, Board board, int row, int column) {
        this.board = board;
        this.row = row;
        this.column = column;
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
        button.setIcon(Corner.STRUCTURE.NONE.icon);
        setComponentZOrder(button, 0);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!buildable)
                    return;
                buildable = false;
                owner = board.getCurPlayer();
                button.setEnabled(false);
                iconDisplay.setIcon(Catan.changeIconColor(icon, board.getCurPlayer().getColor()));
                
                //Road functionality (update player's accessible corners)
                if (row % 2 == 0) {
                    //Connects sideways
                    board.getCurPlayer().addCorner(board.getCorner(getRow()/2, getColumn()+1));     // right
                    board.getCurPlayer().addCorner(board.getCorner(getRow()/2, getColumn()));   // left
                } else {
                    //Connects vertically
                    board.getCurPlayer().addCorner(board.getCorner(getRow()/2, getColumn()*2));     // top
                    board.getCurPlayer().addCorner(board.getCorner(getRow()/2+1, getColumn()*2 + 1 - 2*((row/2)%2))); // bottom
                }

                Catan.semaphore.release();
            }
        });

        validate();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Player getOwner() {
        return owner;
    }
}