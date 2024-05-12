package Catan;

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
    public static final int RADIUS = 10;
    private static ImageIcon offerBuild = Catan.getResizedIcon(RADIUS*2, RADIUS*2, "Catan/Icons/TempCornerBuild.png");
    

    public Corner(int row, int column) {
        setOpaque(true);
        setIcon(offerBuild);//testing icon
        validate();
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