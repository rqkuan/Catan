package Catan;

import javax.swing.*;

public class Tile extends JButton {

    private Corner corners[] = new Corner[6];
    private Board.RESOURCE resource;
    public boolean thief = false;

    public Tile(Corner[] corners, Board.RESOURCE resource) {
        this.corners = corners;
        this.resource = resource;
    }

    public Corner[] getCorners() {
        return corners;
    }

    public Board.RESOURCE getResource() {
        return resource;
    }
}