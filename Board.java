package Catan;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Board extends JFrame implements ActionListener{

    public enum RESOURCE {
        WHEAT("Catan/Icons/CatanWheatIcon.png"),
        SHEEP("Catan/Icons/CatanSheepIcon.png"),
        TIMBER("Catan/Icons/CatanTimberIcon.png"),
        BRICK("Catan/Icons/CatanBrickIcon.png"),
        ORE("Catan/Icons/CatanOreIcon.png"),
        NONE("Catan/Icons/CatanWheatIcon.png");

        public ImageIcon icon;
        private RESOURCE (String image_path) {
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File(image_path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image dimg = img.getScaledInstance(Tile.WIDTH, Tile.HEIGHT, Image.SCALE_DEFAULT);
            icon = new ImageIcon(dimg);
        }
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
    public static Random rn = new Random();
    
    private JPanel sidebar, bottombar, map;

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
        bottombar.setBackground(Color.decode("#CCCCCC"));
        bottombar.setLayout(null);



        //Hex tiles
        map = new JPanel();
        add(map);
        map.setBounds(0, 0, 640, 450);
        map.setBackground(Color.decode("#0099FF"));
        map.setLayout(null);

        for (int t = 2; t <= 12; t++)
            tiles[t] = new LinkedList<Tile>();

        makeTileRow(0, 1, 3);
        makeTileRow(1, 0, 3);
        makeTileRow(2, 0, 4);
        makeTileRow(3, 0, 3);
        makeTileRow(4, 1, 3);

    }

    public void makeTileRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            Tile tempTile = new Tile(row, column, RESOURCE.values()[rn.nextInt(RESOURCE.values().length)]);
            tiles[rn.nextInt(11) + 2].add(tempTile); 

            //Setting up in GUI
            map.add(tempTile);
            tempTile.setBounds((Tile.WIDTH - 4)*column + (int)(0.5 * (row%2) * (Tile.WIDTH - 4)), 
                                (int)((Tile.HEIGHT*1.5 - 6)*row/2.0), Tile.WIDTH, Tile.HEIGHT);
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