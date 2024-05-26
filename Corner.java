package Catan;

import java.awt.event.*;
import javax.swing.*;

/** Corner
 * The Corner class keeps track of corner data, displays corners in the GUI, and handles building settlements/cities.
 * Each corner is a button used for both building new settlements/cities and displaying existing settlements/cities. 
 * 
 * Attributes that the corner keeps track of: 
 * - Whether or not it can currently be build on
 * - The board that the corner belongs to
 * - What structure is currently on it. 
 * - The position of the corner in its board's matrix
 */
public class Corner extends JButton {

    public enum STRUCTURE {
        NONE(0, "Catan/Icons/TempCornerBuild.png"),
        SETTLEMENT(1, "Catan/Icons/CatanSettlement.png"),
        CITY(2, "Catan/Icons/CatanCity.png");

        public final int generateAmount;
        public final ImageIcon icon;
        private STRUCTURE(int generateAmount, String image_path) {
            this.generateAmount = generateAmount;
            icon = Catan.getResizedIcon(RADIUS*2, RADIUS*2, image_path);
        }
    }

    //Corner attributes
    public boolean buildable = false;
    private Player owner;
    private STRUCTURE structure = STRUCTURE.NONE;
    private int row, column;
    private Board board;
    
    //GUI attributes
    public static final int RADIUS = 12;

    /** Corner Constructor
     * The Corner constructor initializes the corner attributes, sets up the corner in the GUI, 
     * and initializes itself for building cities/settlements (giving itself the proper button behavior). 
     * @param board
     * @param row
     * @param column
     */
    public Corner(Board board, int row, int column) {
        //Initializing attributes
        this.board = board;
        this.row = row;
        this.column = column;

        //GUI settings
        setEnabled(true);
        setIcon(structure.icon);
        setVisible(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        //Setting up the button behavior for building settlements/cities
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Make sure the corner can actually be built on
                if (!buildable)
                    return;

                //Updating corner attributes 
                buildable = false;
                owner = board.getCurPlayer();
                structure = STRUCTURE.values()[structure.ordinal() + 1];
                setIcon(Catan.changeIconColor(structure.icon, owner.getColor()));
                
                //Updating the most recently built-on corner (helps for resource generation from placing the first few tiles)
                board.recentBuild = Corner.this;

                //Adjacent corners can no longer be built on
                for (Corner c : board.getAdjacentCorners(Corner.this))
                    c.setEnabled(false);

                //Letting the thread know that a building has been built
                Catan.semaphore.release();
            }
        });
    }

    public void setStructure(STRUCTURE structure) {
        this.structure = structure;
    }

    public Player getOwner() {
        return owner;
    }

    public STRUCTURE getStructure() {
        return structure;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

}