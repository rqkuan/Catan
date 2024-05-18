package Catan;

import java.awt.event.*;
import javax.swing.*;

public class Corner extends JButton {

    public enum STRUCTURE {
        NONE(0, "Catan/Icons/TempCornerBuild.png"),
        SETTLEMENT(1, "Catan/Icons/CatanSettlement.png"),
        CITY(2, "Catan/Icons/CatanSettlement.png");

        public final int generateAmount;
        public final ImageIcon icon;
        private STRUCTURE(int generateAmount, String image_path) {
            this.generateAmount = generateAmount;
            icon = Catan.getResizedIcon(RADIUS*2, RADIUS*2, image_path);
        }
    }

    public boolean buildable = false;
    private Player owner;
    private STRUCTURE structure = STRUCTURE.NONE;
    private int row, column;
    public static final int RADIUS = 12;
    private Board board;
    

    public Corner(Board board, int row, int column) {
        setEnabled(true);
        setIcon(structure.icon);
        setVisible(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        this.board = board;
        this.row = row;
        this.column = column;

        final Corner corner = this;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Processing button presses
                if (!buildable)
                    return;

                owner = board.getCurPlayer();
                owner.addCorner(corner);
                buildable = false;
                structure = STRUCTURE.values()[structure.ordinal() + 1];
                setIcon(Catan.changeIconColor(structure.icon, owner.getColor()));
                board.recentBuild = corner;

                //Adjacent corners can no longer be built on
                Corner c = null;
                try {
                    if (column % 2 == 1) {
                        //Connects upwards

                        //Navigating around the staggered coordinate system
                        if (row % 2 == 0) 
                            c = board.corners[row-1][column+1];
                        else 
                            c = board.corners[row-1][column-1];
                    } else {
                        //Connects downwards

                        //Navigating around the staggered coordinate system
                        if (row % 2 == 0) 
                            c = board.corners[row+1][column+1];
                        else 
                            c = board.corners[row+1][column-1];
                    }
                    if (c != null) 
                        c.setEnabled(false);
                } catch (IndexOutOfBoundsException excpt) {}

                try {
                    c = board.corners[row][column-1];
                    if (c != null) 
                        c.setEnabled(false);
                } catch (IndexOutOfBoundsException excpt){}

                try {
                    c = board.corners[row][column+1];
                    if (c != null) 
                        c.setEnabled(false);
                } catch (IndexOutOfBoundsException excpt){}

                Catan.semaphore.release();
            }
        });
    }

    public void setOwner(Player owner) {
        this.owner = owner;
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