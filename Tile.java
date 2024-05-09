package Catan;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Tile extends JPanel {

    private Corner corners[] = new Corner[6];
    private Board.RESOURCE resource;
    public boolean thief = false;
    public static final int WIDTH = 78, HEIGHT = 90;
    private int row, column;

    public Tile(int row, int column, Board.RESOURCE resource) {
        setOpaque(false);
        setBackground(Color.decode("#eaecd0"));
        setLayout(null);
        
        //Hex Image
        JLabel iconDisplay = new JLabel(resource.icon);
        add(iconDisplay);
        iconDisplay.setBounds(0, 0, WIDTH, HEIGHT);

        //Number display and button for thief placement
        TileButton tb = new TileButton();
        add(tb);
        tb.setOpaque(true);
        tb.setBounds(WIDTH/2 - 13, HEIGHT/2 - 13, 26, 26);
        setComponentZOrder(tb, 0);

        validate();

        //Tile attributes
        this.row = row;
        this.column = column;
        this.resource = resource;
    }

    public Corner[] getCorners() {
        return corners;
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