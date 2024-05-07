package Catan;

import javax.swing.*;

public class Corner extends JButton{

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

}