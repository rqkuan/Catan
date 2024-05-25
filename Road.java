package Catan;

import java.awt.event.*;
import javax.swing.*;

/** Road
 * The Road class keeps track of road data, displays roads in the GUI, 
 * and handles the building of roads. (Including connecting corners when a road is built)
 * Each road has a label used for displaying the road itself and a button used for building the road. 
 * 
 * Attributes that the road keeps track of: 
 * - Whether a new road can be built in its road location
 * - Who the road's owner is
 * - The board that the road belongs to
 * - The position of the road in its board's matrix
 */
public class Road extends JPanel {

    //Road attributes
    public boolean buildable = false;
    private Player owner;
    private Board board;
    private int row, column;

    //GUI components/attributes
    public static ImageIcon roadSlantUp = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantUp.png");
    public static ImageIcon roadSlantDown = Catan.getResizedIcon((Tile.WIDTH - Tile.WIDTH/20)/2, 
                                        (int)((Tile.WIDTH - Tile.WIDTH/20)/2 * 0.577), "Catan/Icons/CatanRoadSlantDown.png");
    public static ImageIcon roadVertical = Catan.getResizedIcon(6, (Tile.HEIGHT - Tile.HEIGHT/15)/2, "Catan/Icons/CatanRoadVertical.png");
    public ImageIcon icon;
    public JLabel iconDisplay;
    public JButton button = new JButton();

    /** Road Constructor
     * The Road constructor initializes the road attributes, sets up the road in the GUI, 
     * and initializes its building button. 
     * @param icon
     * @param board
     * @param row
     * @param column
     */
    public Road(ImageIcon icon, Board board, int row, int column) {
        //Initializing attributes
        this.board = board;
        this.row = row;
        this.column = column;
        this.icon = icon;

        //Hiding the panel itself (it's only there to contain the label and button)
        setOpaque(false);
        setVisible(false);
        setLayout(null);

        //Road Icon (the label)
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
                //Make sure that the road is actually available for building
                if (!buildable)
                    return;

                //Once a road is built, it can't be build again
                buildable = false;
                button.setEnabled(false);

                //Updating the road
                owner = board.getCurPlayer();
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

                //Letting the buildRoad thread from Player.buildRoad(Board) know that a road has been built
                Catan.semaphore.release();
            }
        });
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