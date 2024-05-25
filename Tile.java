package Catan;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/** Tile
 * The Tile class keeps track of tile data, displays tiles in the GUI, and handles placing thieves.
 * Each tile has a label used for displaying the tile itself and a button used for placing a thief/displaying the tile number. 
 * 
 * Attributes that the tile keeps track of: 
 * - Whether or not it has a thief on it
 * - What resource it generates
 * - The board that the tile belongs to
 * - The position of the tile in its board's matrix
 */
public class Tile extends JPanel {

    //Tile attributes
    private Board board;
    private Board.RESOURCE resource;
    public boolean thief = false;
    private int row, column;

    //GUI components/attributes
    public static final int WIDTH = 94, HEIGHT = 108;
    public JButton button;
    public JLabel iconDisplay;

    /** Tile Constructor
     * The Tile constructor initializes the tile attributes, sets up the tile in the GUI, 
     * and initializes its button for placing thieves. 
     * @param board
     * @param row
     * @param column
     * @param resource
     */
    public Tile(Board board, int row, int column, Board.RESOURCE resource) {
        //Initializing attributes
        this.board = board;
        this.row = row;
        this.column = column;
        this.resource = resource;

        //Hiding the panel itself (it's only there to contain the label and button)
        setOpaque(false);
        setLayout(null);
        
        //Tile Icon (the label)
        iconDisplay = new JLabel(resource.icon);
        add(iconDisplay);
        iconDisplay.setBounds(0, 0, WIDTH, HEIGHT);

        //Button for thief placement (and number display, but that's handled by the makeTileRow function)
        button = new JButton();
        add(button);
        button.setOpaque(true);
        button.setBounds(WIDTH/2 - 13, HEIGHT/2 - 13, 26, 26);
        button.setEnabled(false);
        setComponentZOrder(button, 0);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Updating the tile
                thief = true;
                Tile.this.iconDisplay.setEnabled(false);

                //Letting the placeThief thread from Board.offerPlaceThief know that a thief has been placed
                Catan.semaphore.release();
                
                //Finding players that can be stolen from
                LinkedList<Player> thieved = new LinkedList<Player>();
                for (int i = 0; i < 3; i++) {   //Top row of corners
                    Corner c = board.getCorner(row, column*2 + i);
                    if (c.getOwner() != null && !thieved.contains(c.getOwner()) && c.getOwner() != board.getCurPlayer())
                        thieved.add(c.getOwner());
                }
                for (int i = 0; i < 3; i++) {   //Bottom row of corners
                    Corner c = board.getCorner(row+1, (column - row%2)*2 + 1 + i);
                    if (c.getOwner() != null && !thieved.contains(c.getOwner()) && c.getOwner() != board.getCurPlayer())
                        thieved.add(c.getOwner());
                }

                //If no players can be stolen from, just let the thread know and stop here
                if (thieved.size() == 0) {
                    Catan.semaphore.release();
                    return;
                }


                //Choosing a player to steal from
                ForcedPopup playerThiefSelect = new ForcedPopup(); //This forces the player to choose someone to steal from
                for (Player p : thieved) {
                    //Adding each player that can be stolen from as a menu item in the popup
                    JMenuItem playerThiefMenuItem = new JMenuItem("Player " + (board.players.indexOf(p)+1));
                    playerThiefMenuItem.setForeground(p.getColor());
                    playerThiefMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //If the selected player doesn't actually have any resources, 
                            //just let the thread know, close the popup, and stop here. 
                            if (p.getTotalResources() == 0) {
                                Catan.semaphore.release();
                                playerThiefSelect.removeAll();
                                Tile.this.remove(playerThiefSelect);
                                playerThiefSelect.closePopup();
                                return;
                            }

                            //Randomizing the stolen resource
                            int n = Board.rn.nextInt(p.getTotalResources());
                            int wheatBound = p.getResource(Board.RESOURCE.WHEAT);
                            int sheepBound = wheatBound + p.getResource(Board.RESOURCE.SHEEP);
                            int timberBound = sheepBound + p.getResource(Board.RESOURCE.TIMBER);
                            int brickBound = timberBound + p.getResource(Board.RESOURCE.BRICK);
                            if (n < wheatBound) 
                                p.trade(board.getCurPlayer(), new int[] {1, 0, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});
                            else if (n < sheepBound)
                                p.trade(board.getCurPlayer(), new int[] {0, 1, 0, 0, 0}, new int[] {0, 0, 0, 0, 0});
                            else if (n < timberBound)
                                p.trade(board.getCurPlayer(), new int[] {0, 0, 1, 0, 0}, new int[] {0, 0, 0, 0, 0});
                            else if (n < brickBound)
                                p.trade(board.getCurPlayer(), new int[] {0, 0, 0, 1, 0}, new int[] {0, 0, 0, 0, 0});
                            else 
                                p.trade(board.getCurPlayer(), new int[] {0, 0, 0, 0, 1}, new int[] {0, 0, 0, 0, 0});
                            
                            //Letting the thread know that a player has been stolen from
                            Catan.semaphore.release();

                            //Closing the popup
                            playerThiefSelect.removeAll();
                            Tile.this.remove(playerThiefSelect);
                            playerThiefSelect.closePopup();
                        }
                    });
                    playerThiefSelect.add(playerThiefMenuItem);
                }
                playerThiefSelect.show(Tile.this, 0, 0);
            }
        });
    }

    public Board.RESOURCE getResource() {
        return resource;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}