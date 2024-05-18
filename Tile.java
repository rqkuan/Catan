package Catan;

import javax.swing.*;


public class Tile extends JPanel {

    private Board.RESOURCE resource;
    public boolean thief = false;
    public static final int WIDTH = 94, HEIGHT = 108;
    private int row, column;
    public JButton button = new JButton();

    public Tile(int row, int column, Board.RESOURCE resource) {
        setOpaque(false);
        setLayout(null);
        
        //Hex Image
        JLabel iconDisplay = new JLabel(resource.icon);
        add(iconDisplay);
        iconDisplay.setBounds(0, 0, WIDTH, HEIGHT);

        //Number display and button for thief placement
        add(button);
        button.setOpaque(true);
        button.setBounds(WIDTH/2 - 13, HEIGHT/2 - 13, 26, 26);
        setComponentZOrder(button, 0);

        validate();

        //Tile attributes
        this.row = row;
        this.column = column;
        this.resource = resource;
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