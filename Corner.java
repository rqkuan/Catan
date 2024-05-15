package Catan;

import java.awt.event.*;
import javax.swing.*;

public class Corner extends JButton {

    public enum STRUCTURE {
        NONE(0),
        SETTLEMENT(1),
        CITY(2);

        public final int generateAmount;
        private STRUCTURE(int generateAmount) {
            this.generateAmount = generateAmount;
        }
    }

    public boolean buildable = false;
    private Player owner;
    private STRUCTURE structure = STRUCTURE.NONE;
    private int row, column;
    public static final int RADIUS = 12;
    private Board board;
    public static ImageIcon offerBuild = Catan.getResizedIcon(RADIUS*2, RADIUS*2, "Catan/Icons/TempCornerBuild.png");
    private static ImageIcon settlementIcon = Catan.getResizedIcon(RADIUS*2, RADIUS*2, "Catan/Icons/CatanSettlement.png");
    

    public Corner(Board board, int row, int column) {
        setEnabled(true);
        setIcon(offerBuild);//testing icon
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
                setIcon(Catan.changeIconColor(settlementIcon, owner.getColor()));
                buildable = false;
                structure = STRUCTURE.values()[structure.ordinal() + 1];
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