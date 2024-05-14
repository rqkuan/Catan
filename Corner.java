package Catan;

import java.awt.event.*;
import javax.swing.*;

public class Corner extends JButton {

    public enum STRUCTURE {
        SETTLEMENT(1),
        CITY(2);

        public final int generateAmount;
        private STRUCTURE(int generateAmount) {
            this.generateAmount = generateAmount;
        }
    }

    private Player owner;
    private STRUCTURE structure;
    private int row, column;
    public static final int RADIUS = 12;
    private Board board;
    private static ImageIcon offerBuild = Catan.getResizedIcon(RADIUS*2, RADIUS*2, "Catan/Icons/TempCornerBuild.png");
    private static ImageIcon settlementIcon = Catan.getResizedIcon(RADIUS*2, RADIUS*2, "Catan/Icons/CatanSettlement.png");
    

    public Corner(Board board, int row, int column) {
        setEnabled(true);
        setIcon(offerBuild);//testing icon
        setOpaque(false);
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
                owner = board.getCurPlayer();
                owner.addCorner(corner);
                setIcon(Catan.changeIconColor(settlementIcon, owner.getColor()));

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
                    if (c != null) {
                        c.setVisible(false);
                        c.setEnabled(false);
                    }
                } catch (IndexOutOfBoundsException excpt) {}

                try {
                    c = board.corners[row][column-1];
                    if (c != null) {
                        c.setVisible(false);
                        c.setEnabled(false);
                    }
                } catch (IndexOutOfBoundsException excpt){}

                try {
                    c = board.corners[row][column+1];
                    if (c != null) {
                        c.setVisible(false);
                        c.setEnabled(false);
                    }
                } catch (IndexOutOfBoundsException excpt){}

                board.nextPlayer();
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