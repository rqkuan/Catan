package Catan;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Board extends JFrame implements ActionListener{

    public enum RESOURCE {
        WHEAT,
        SHEEP, 
        TIMBER,
        BRICK,
        ORE,
        NONE;
    }

    public enum DEVELOPMENT {
        KNIGHT, 
        VICTORY_POINT,
        ROAD_BUILDING,
        YEAR_OF_PLENTY,
        MONOPOLY;
    }

    public ArrayList<Player> players = new ArrayList<Player>();
    private LinkedList<Tile> tiles[] = new LinkedList[13];
    private Corner corners[][];
    private Road road[][];
    private int resourceLimit, resources[], devCards[];
    
    private JPanel sidebar, bottombar;

    public Board() {
        //Create gui upon construction of board object

        //Setting the Frame
        setTitle("Catan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        setSize(840, 600);
        setVisible(true);
        Dimension dm = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dm.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dm.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);

        //Sidebar
        sidebar = new JPanel();
        add(sidebar);
        sidebar.setBounds(640, 0, 200, 600);
        sidebar.setBackground(Color.decode("#BBBBBB"));
        sidebar.setLayout(null);
        
        //Bottombar
        bottombar = new JPanel();
        add(bottombar);
        bottombar.setBounds(0, 450, 640, 150);
        bottombar.setBackground(Color.decode("#BBBBBB"));
        bottombar.setLayout(null);



        //Hex tiles

        //First row
        for (int t = 0; t < 3; t++) {
            Corner corners[] = new Corner[3];
            corners[0] = new Corner(); //need to change this to reference the correct corners in this.corners (2d array)
            corners[2] = new Corner();
            corners[3] = new Corner();

            tiles[0].add(new Tile(corners, RESOURCE.BRICK)); //Generate random resources later

            tiles[0].get(t).setBounds(70*(t+1), 0, 70, 70);
        }


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Processing button presses

    }

    public void build(Corner corner, Corner.STRUCTURE structure, Player player) {
        //helper method to build roads and structures

    }

    public Corner getCorner(int row, int column) {
        return corners[row][column];
    }

    public void rollDice() {

    }

    public void draw() {

    }

}