package Catan;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

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
    private Corner corners[][] = new Corner[100][100]; //Organize later
    private Road roads[][] = new Road[100][100]; //Organize later
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



        //Map
        map = new JPanel();
        add(map);
        map.setBounds(0, 0, 640, 450);
        map.setBackground(Color.decode("#0099FF"));
        map.setLayout(null);

        for (int t = 2; t <= 12; t++)
            tiles[t] = new LinkedList<Tile>();

        makeTileRow(0, 1, 3);
        makeTileRow(1, 1, 4);
        makeTileRow(2, 0, 4);
        makeTileRow(3, 1, 4);
        makeTileRow(4, 1, 3);

        makeRoadRow(0, 2, 7);
        makeRoadRow(1, 1, 4);
        makeRoadRow(2, 1, 8);
        makeRoadRow(3, 1, 5);
        makeRoadRow(4, 0, 9);
        makeRoadRow(5, 0, 5);
        makeRoadRow(6, 0, 9);
        makeRoadRow(7, 1, 5);
        makeRoadRow(8, 1, 8);
        makeRoadRow(9, 1, 4);
        makeRoadRow(10, 2, 7);
        map.validate();

        makeCornerRow(0, 2, 8); 
        makeCornerRow(1, 2, 10);
        makeCornerRow(2, 0, 10);
        makeCornerRow(3, 1, 11);
        makeCornerRow(4, 1, 9);
        makeCornerRow(5, 3, 9);
        map.validate();

    }

    public void makeTileRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            Tile tempTile = new Tile(row, column, RESOURCE.values()[rn.nextInt(RESOURCE.values().length)]);
            tiles[rn.nextInt(11) + 2].add(tempTile); 

            //Setting up in GUI
            map.add(tempTile);
            int x = (Tile.WIDTH - Tile.WIDTH/20)*column - (int)(0.5 * (row%2) * (Tile.WIDTH - 4));
            int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*row/2.0);
            tempTile.setBounds(40+x, 15+y, Tile.WIDTH, Tile.HEIGHT);
        }
    }

    public void makeCornerRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            Corner tempCorner = new Corner(row, column);
            corners[row][column] = tempCorner; 

            //Setting up in GUI
            map.add(tempCorner);
            int x = (Tile.WIDTH - Tile.WIDTH/20)*(column/2) - (int)(0.5 * (row%2) * (Tile.WIDTH - 4)) + Tile.WIDTH/2; //Finding position of tile
            if (column% 2 == 0)
                x -= (Tile.WIDTH - Tile.WIDTH/20)/2; //Offsetting the corner accordingly

            int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*row/2.0) + Tile.HEIGHT/2; //Finding position of tile
            y -= (Tile.HEIGHT - Tile.HEIGHT/15)/4;
            if (column % 2 == 1)
                y -= (Tile.HEIGHT - Tile.HEIGHT/15)/4; //Offsetting the corner accordingly
            
            tempCorner.setBounds(40+x-Corner.RADIUS, 15+y-Corner.RADIUS, Corner.RADIUS*2, Corner.RADIUS*2);
            map.setComponentZOrder(tempCorner, 0);
        }
    }

    public void makeRoadRow(int row, int first_column, int last_column) {
        if (row % 2 == 0) {
            //Zigzagging roads
            for (int column = first_column; column <= last_column; column++) {
                Road tempRoad;
                if ((column + row/2) % 2 == 0) //Slanted roads with the same Road-column and Tile-row parity slant up
                    tempRoad = new Road(Road.roadSlantUp);
                else //The others slant down
                    tempRoad = new Road(Road.roadSlantDown);
                roads[row][column] = tempRoad; 

                //Setting up in GUI
                map.add(tempRoad);
                int x = (Tile.WIDTH - Tile.WIDTH/20)*(column/2) - (int)(0.5 * (row%2) * (Tile.WIDTH - 4)) + Tile.WIDTH/2; //Finding position of tile
                x += (Tile.WIDTH - Tile.WIDTH/20)/4;
                if (column% 2 == 0)
                    x -= (Tile.WIDTH - Tile.WIDTH/20)/2; //Offsetting the road accordingly

                int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*row/2/2.0) + Tile.HEIGHT/2; //Finding position of tile
                y -= (Tile.HEIGHT - Tile.HEIGHT/15)*3/8; //Offsetting the road accordingly
                
                int actualtw = (Tile.WIDTH - Tile.WIDTH/20);
                tempRoad.setBounds(40+x-actualtw/2/2, 15+y-(int)(actualtw/2 * 0.577)/2, actualtw/2, (int)(actualtw/2 * 0.577));
                map.setComponentZOrder(tempRoad, 0);
            }
        } else {
            //Vertical Roads
            for (int column = first_column; column <= last_column; column++) {
                Road tempRoad = new Road(Road.roadVertical);
                roads[row][column] = tempRoad; 

                //Setting up in GUI
                map.add(tempRoad);
                int x = (Tile.WIDTH - Tile.WIDTH/20)*column - (int)(0.5 * ((row/2)%2) * (Tile.WIDTH - 4)) + Tile.WIDTH/2; //Finding position of tile
                x -= (Tile.WIDTH - Tile.WIDTH/20)/2; //Offsetting the road accordingly

                int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*(row/2)/2.0) + Tile.HEIGHT/2; //Finding position of tile
                
                tempRoad.setBounds(40+x-3, 15+y-(Tile.HEIGHT-Tile.HEIGHT/10)/4, 6, (Tile.HEIGHT-Tile.HEIGHT/10)/2);
                map.setComponentZOrder(tempRoad, 0);
            }
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