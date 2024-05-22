package Catan;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;


public class Tile extends JPanel {

    private Board board;
    private Board.RESOURCE resource;
    public boolean thief = false;
    public static final int WIDTH = 94, HEIGHT = 108;
    private int row, column;

    public JButton button;
    public JLabel iconDisplay;

    public Tile(Board board, int row, int column, Board.RESOURCE resource) {
        setOpaque(false);
        setLayout(null);
        
        //Hex Image
        iconDisplay = new JLabel(resource.icon);
        add(iconDisplay);
        iconDisplay.setBounds(0, 0, WIDTH, HEIGHT);

        //Number display and button for thief placement
        button = new JButton();
        add(button);
        button.setOpaque(true);
        button.setBounds(WIDTH/2 - 13, HEIGHT/2 - 13, 26, 26);
        button.setEnabled(false);
        setComponentZOrder(button, 0);

        validate();

        //Tile attributes
        this.board = board;
        this.row = row;
        this.column = column;
        this.resource = resource;

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Updating the tile
                thief = true;
                Tile.this.iconDisplay.setEnabled(false);

                Catan.semaphore.release();
                
                //Letting the player steal from others
                LinkedList<Player> thiefed = new LinkedList<Player>();
                //Top row of corners
                for (int i = 0; i < 3; i++) {
                    Corner c = board.corners[row][column*2 + i];
                    if (c.getOwner() != null && !thiefed.contains(c.getOwner()) && c.getOwner() != board.getCurPlayer())
                        thiefed.add(c.getOwner());
                }
                //Bottom row of corners
                for (int i = 0; i < 3; i++) {
                    Corner c = board.corners[row+1][(column - row%2)*2 + 1 + i];
                    if (c.getOwner() != null && !thiefed.contains(c.getOwner()) && c.getOwner() != board.getCurPlayer())
                        thiefed.add(c.getOwner());
                }

                //Select player to steal from
                if (thiefed.size() == 0) {
                    Catan.semaphore.release();
                    return;
                }

                ForcedPopup playerThiefSelect = new ForcedPopup(); //This forces the player to choose someone to steal from

                for (Player p : thiefed) {
                    JMenuItem playerThiefMenuItem = new JMenuItem("Player " + (board.players.indexOf(p)+1));
                    playerThiefMenuItem.setForeground(p.getColor());
                    playerThiefMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //Randomizing the stolen resource
                            int n = Board.rn.nextInt(p.getTotalResources());
                            int wheatBound = p.getResource(Board.RESOURCE.WHEAT);
                            int sheepBound = wheatBound + p.getResource(Board.RESOURCE.SHEEP);
                            int timberBound = wheatBound + p.getResource(Board.RESOURCE.TIMBER);
                            int brickBound = wheatBound + p.getResource(Board.RESOURCE.BRICK);
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
                            
                            Catan.semaphore.release();

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