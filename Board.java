package Catan;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class Board extends JFrame{

    public static enum RESOURCE {
        WHEAT("Catan/Icons/CatanWheatTile.png"),
        SHEEP("Catan/Icons/CatanSheepTile.png"),
        TIMBER("Catan/Icons/CatanTimberTile.png"),
        BRICK("Catan/Icons/CatanBrickTile.png"),
        ORE("Catan/Icons/CatanOreTile.png"),
        NONE("Catan/Icons/CatanWheatTile.png");

        public ImageIcon icon;
        private RESOURCE (String image_path) {
            icon = Catan.getResizedIcon(Tile.WIDTH, Tile.HEIGHT, image_path);
        }

        static {
            NONE.icon = Catan.changeIconColor(NONE.icon, new Color(0, 0, 0));
        }
    }

    public static enum DEVELOPMENT {
        KNIGHT, 
        VICTORY_POINT,
        ROAD_BUILDING,
        YEAR_OF_PLENTY,
        MONOPOLY;
    }

    //Board attributes
    public Corner recentBuild;
    public ArrayList<Player> players = new ArrayList<Player>();
    public Player bank = new Player(new Color(0, 0, 0));
    private LinkedList<Tile> tilesNumRef[] = new LinkedList[13];
    public Tile tiles[][] = new Tile[5][5]; 
    public Corner corners[][] = new Corner[6][12]; 
    private Road roads[][] = new Road[11][11]; 
    private int resourceLimit, totalResources, devCards[];
    public int curPlayerIndex = 0;
    public static Random rn = new Random();

    //GUI objects/attributes
    private static final int mapXOffset = 88, mapYOffset = 15;
    public JPanel sidebar, bottombar, map;
    public JButton buildRoadButton, buildSettlementButton, buildCityButton, rollDiceButton, endTurnButton, tradeButton;
    public JLabel curPlayerLabel, wheatLabel, sheepLabel, timberLabel, brickLabel, oreLabel, 
                    rollLabel, wheatAmount, sheepAmount, timberAmount, brickAmount, oreAmount;

    public Board(int resourceLimit, int totalResources, int[] devCards) {
        this.resourceLimit = resourceLimit;
        this.totalResources = totalResources;
        for (int i = 0; i < RESOURCE.values().length-1; i++)
            bank.addResource(RESOURCE.values()[i], totalResources);
        this.devCards = devCards;

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
        bottombar.setBounds(0, 450, 640, 122);
        bottombar.setBackground(Color.decode("#CCCCCC"));
        bottombar.setLayout(null);



        //Map
        map = new JPanel();
        add(map);
        map.setBounds(0, 0, 640, 450);
        map.setBackground(Color.decode("#0099FF"));
        map.setLayout(null);

        for (int t = 2; t <= 12; t++)
            tilesNumRef[t] = new LinkedList<Tile>();

        makeTileRow(0, 1, 3);
        makeTileRow(1, 1, 4);
        makeTileRow(2, 0, 4);
        makeTileRow(3, 1, 4);
        makeTileRow(4, 1, 3);

        makeRoadRow(0, 2, 7);
        makeRoadRow(1, 1, 4);
        makeRoadRow(2, 2, 9);
        makeRoadRow(3, 1, 5);
        makeRoadRow(4, 0, 9);
        makeRoadRow(5, 0, 5);
        makeRoadRow(6, 1, 10);
        makeRoadRow(7, 1, 5);
        makeRoadRow(8, 1, 8);
        makeRoadRow(9, 1, 4);
        makeRoadRow(10, 3, 8);
        map.validate();

        makeCornerRow(0, 2, 8); 
        makeCornerRow(1, 2, 10);
        makeCornerRow(2, 0, 10);
        makeCornerRow(3, 1, 11);
        makeCornerRow(4, 1, 9);
        makeCornerRow(5, 3, 9);
        map.validate();


        //Buttons for building
        int buildButtonWidth = 100;
        int buildButtonHeight = 60;

        buildRoadButton = new JButton("Road");
        buildSettlementButton = new JButton("Settlement");
        buildCityButton = new JButton("City");

        //Road
        bottombar.add(buildRoadButton);
        buildRoadButton.setBounds(bottombar.getWidth() - 3*(buildButtonWidth + 5), (bottombar.getHeight() - buildButtonHeight)/2, buildButtonWidth, buildButtonHeight);
        buildRoadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buildSettlementButton.setEnabled(false);
                buildCityButton.setEnabled(false);
                getCurPlayer().buildRoad(Board.this);
                buildSettlementButton.setEnabled(true);
                buildCityButton.setEnabled(true);
            }
        });

        //Settlement
        bottombar.add(buildSettlementButton);
        buildSettlementButton.setBounds(bottombar.getWidth() - 2*(buildButtonWidth + 5), (bottombar.getHeight() - buildButtonHeight)/2, buildButtonWidth, buildButtonHeight);
        buildSettlementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buildRoadButton.setEnabled(false);
                buildCityButton.setEnabled(false);
                getCurPlayer().buildSettlement(Board.this);
                buildRoadButton.setEnabled(true);
                buildCityButton.setEnabled(true);
            }
        });

        //City
        bottombar.add(buildCityButton);
        buildCityButton.setBounds(bottombar.getWidth() - (buildButtonWidth + 5), (bottombar.getHeight() - buildButtonHeight)/2, buildButtonWidth, buildButtonHeight);
        buildCityButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buildRoadButton.setEnabled(false);
                buildSettlementButton.setEnabled(false);
                getCurPlayer().buildCity(Board.this);
                buildRoadButton.setEnabled(true);
                buildSettlementButton.setEnabled(true);
            }
        });

        //Dice Roll Button
        rollDiceButton = new JButton("Roll Dice");
        sidebar.add(rollDiceButton);
        rollDiceButton.setBounds(5, 450 + (bottombar.getHeight() - buildButtonHeight)/2, (sidebar.getWidth() - 15)/2, buildButtonHeight);
        rollDiceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rollDice();
                buildRoadButton.setEnabled(true);
                buildSettlementButton.setEnabled(true);
                buildCityButton.setEnabled(true);
                rollDiceButton.setEnabled(false);
                endTurnButton.setEnabled(true);
            }
        });
        rollLabel = new JLabel("--");
        sidebar.add(rollLabel);
        rollLabel.setBounds(rollDiceButton.getX()+rollDiceButton.getWidth()/2 - 7, rollDiceButton.getY()+rollDiceButton.getHeight()-5, 30, 30);

        //End Turn Button
        endTurnButton = new JButton("End Turn");
        sidebar.add(endTurnButton);
        endTurnButton.setBounds(5 + (sidebar.getWidth() - 15)/2 + 5, 450 + (bottombar.getHeight() - buildButtonHeight)/2, (sidebar.getWidth() - 15)/2, buildButtonHeight);
        endTurnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextPlayer();
                buildRoadButton.setEnabled(false);
                buildSettlementButton.setEnabled(false);
                buildCityButton.setEnabled(false);
                rollDiceButton.setEnabled(true);
                endTurnButton.setEnabled(false);
            }
        });


        //Resource/player display
        curPlayerLabel = new JLabel();
        bottombar.add(curPlayerLabel);
        curPlayerLabel.setBounds(2, 2, 70, 16);
        
        //Wheat display
        wheatLabel = new JLabel("00");
        bottombar.add(wheatLabel);
        wheatLabel.setBounds(20, 25, 50, bottombar.getHeight()-50);
        wheatLabel.setIcon(Catan.getResizedIcon(wheatLabel.getWidth(), wheatLabel.getHeight(), "Catan/Icons/CatanWheat.png"));
        wheatAmount = new JLabel();
        bottombar.add(wheatAmount);
        wheatAmount.setBounds(37, wheatLabel.getY() - 5 + wheatLabel.getHeight(), 30, 30);
        
        //Sheep display
        sheepLabel = new JLabel("00");
        bottombar.add(sheepLabel);
        sheepLabel.setBounds(wheatLabel.getX() + 1*(60), wheatLabel.getY(), wheatLabel.getWidth(), wheatLabel.getHeight());
        sheepLabel.setIcon(Catan.getResizedIcon(wheatLabel.getWidth(), wheatLabel.getHeight(), "Catan/Icons/CatanSheep.png"));
        sheepAmount = new JLabel();
        bottombar.add(sheepAmount);
        sheepAmount.setBounds(37 + 1*(60), wheatLabel.getY() - 5 + wheatLabel.getHeight(), 30, 30);
        
        //Timber display
        timberLabel = new JLabel("00");
        bottombar.add(timberLabel);
        timberLabel.setBounds(wheatLabel.getX() + 2*(60), wheatLabel.getY(), wheatLabel.getWidth(), wheatLabel.getHeight());
        timberLabel.setIcon(Catan.getResizedIcon(wheatLabel.getWidth(), wheatLabel.getHeight(), "Catan/Icons/CatanTimber.png"));
        timberAmount = new JLabel();
        bottombar.add(timberAmount);
        timberAmount.setBounds(37 + 2*(60), wheatLabel.getY() - 5 + wheatLabel.getHeight(), 30, 30);
        
        //Brick display
        brickLabel = new JLabel("00");
        bottombar.add(brickLabel);
        brickLabel.setBounds(wheatLabel.getX() + 3*(60), wheatLabel.getY(), wheatLabel.getWidth(), wheatLabel.getHeight());
        brickLabel.setIcon(Catan.getResizedIcon(wheatLabel.getWidth(), wheatLabel.getHeight(), "Catan/Icons/CatanBrick.png"));
        brickAmount = new JLabel();
        bottombar.add(brickAmount);
        brickAmount.setBounds(37 + 3*(60), wheatLabel.getY() - 5 + wheatLabel.getHeight(), 30, 30);
        
        //Ore display
        oreLabel = new JLabel("00");
        bottombar.add(oreLabel);
        oreLabel.setBounds(wheatLabel.getX() + 4*(60), wheatLabel.getY(), wheatLabel.getWidth(), wheatLabel.getHeight());
        oreLabel.setIcon(Catan.getResizedIcon(wheatLabel.getWidth(), wheatLabel.getHeight(), "Catan/Icons/CatanOre.png"));
        oreAmount = new JLabel();
        bottombar.add(oreAmount);
        oreAmount.setBounds(37 + 4*(60), wheatLabel.getY() - 5 + wheatLabel.getHeight(), 30, 30);


        //Trading
        tradeButton = new JButton("Trade");
        sidebar.add(tradeButton);
        tradeButton.setBounds(5, 5, 50, 50);
        tradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                offerTrades();
            }
        });
    }

    public void makeTileRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            Tile tempTile = new Tile(row, column, RESOURCE.values()[rn.nextInt(RESOURCE.values().length)]);
            int num = rn.nextInt(11) + 2;
            tilesNumRef[num].add(tempTile); 
            tiles[row][column] = tempTile;
            tempTile.button.setText(""+num);

            //Setting up in GUI
            map.add(tempTile);
            int x = (Tile.WIDTH - Tile.WIDTH/20)*column - (int)(0.5 * (row%2) * (Tile.WIDTH - 4));
            int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*row/2.0);
            tempTile.setBounds(mapXOffset+x, mapYOffset+y, Tile.WIDTH, Tile.HEIGHT);
        }
    }

    public void makeCornerRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            Corner tempCorner = new Corner(this, row, column);
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
            
            tempCorner.setBounds(mapXOffset+x-Corner.RADIUS, mapYOffset+y-Corner.RADIUS, Corner.RADIUS*2, Corner.RADIUS*2);
            map.setComponentZOrder(tempCorner, 0);
        }
    }

    public void makeRoadRow(int row, int first_column, int last_column) {
        if (row % 2 == 0) {
            //Zigzagging roads
            for (int column = first_column; column <= last_column; column++) {
                Road tempRoad;
                if (column % 2 == 0) //Even-columned roads slant up
                    tempRoad = new Road(Road.roadSlantUp, this, row, column);
                else //Odd-columned roads slant down
                    tempRoad = new Road(Road.roadSlantDown, this, row, column);
                roads[row][column] = tempRoad; 

                //Setting up in GUI
                map.add(tempRoad);
                int x = (Tile.WIDTH - Tile.WIDTH/20)*(column/2) - (int)(0.5 * ((row/2)%2) * (Tile.WIDTH - 4)) + Tile.WIDTH/2; //Finding position of tile
                x += (Tile.WIDTH - Tile.WIDTH/20)/4;
                if (column % 2 == 0)
                    x -= (Tile.WIDTH - Tile.WIDTH/20)/2; //Offsetting the road accordingly

                int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*row/2/2.0) + Tile.HEIGHT/2; //Finding position of tile
                y -= (Tile.HEIGHT - Tile.HEIGHT/15)*3/8; //Offsetting the road accordingly
                
                int actualtw = (Tile.WIDTH - Tile.WIDTH/20);
                tempRoad.setBounds(mapXOffset+x-actualtw/2/2, mapYOffset+y-(int)(actualtw/2 * 0.577)/2, actualtw/2, (int)(actualtw/2 * 0.577));
                tempRoad.button.setBounds(tempRoad.getWidth()/2 - 10, tempRoad.getHeight()/2 - 10, 20, 20);
                tempRoad.iconDisplay.setBounds(tempRoad.getWidth()/2 - tempRoad.icon.getIconWidth()/2, 
                                                tempRoad.getHeight()/2 - tempRoad.icon.getIconHeight()/2, 
                                                tempRoad.icon.getIconWidth(), tempRoad.icon.getIconHeight());
                map.setComponentZOrder(tempRoad, 0);
            }
        } else {
            //Vertical Roads
            for (int column = first_column; column <= last_column; column++) {
                Road tempRoad = new Road(Road.roadVertical, this, row, column);
                roads[row][column] = tempRoad; 

                //Setting up in GUI
                map.add(tempRoad);
                int x = (Tile.WIDTH - Tile.WIDTH/20)*column - (int)(0.5 * ((row/2)%2) * (Tile.WIDTH - 4)) + Tile.WIDTH/2; //Finding position of tile
                x -= (Tile.WIDTH - Tile.WIDTH/20)/2; //Offsetting the road accordingly

                int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/mapYOffset)*(row/2)/2.0) + Tile.HEIGHT/2; //Finding position of tile
                
                tempRoad.setBounds(mapXOffset+x-10, mapYOffset+y-(Tile.HEIGHT-Tile.HEIGHT/10)/4, 20, (Tile.HEIGHT-Tile.HEIGHT/10)/2);
                tempRoad.button.setBounds(tempRoad.getWidth()/2 - 10, tempRoad.getHeight()/2 - 10, 20, 20);
                tempRoad.iconDisplay.setBounds(tempRoad.getWidth()/2 - tempRoad.icon.getIconWidth()/2, 
                                                tempRoad.getHeight()/2 - tempRoad.icon.getIconHeight()/2, 
                                                tempRoad.icon.getIconWidth(), tempRoad.icon.getIconHeight());
                map.setComponentZOrder(tempRoad, 0);
            }
        }
    }

    public void offerStartingBuild() throws InterruptedException {
        //Show corner buttons
        for (Corner[] cs : corners) {
            for (Corner c : cs) {
                if (c == null)
                    continue;
                if (c.isEnabled() && c.getOwner() == null) {
                    c.buildable = true;
                    c.setVisible(true);
                }
            }
        }

        //Wait for user to press button
        Catan.semaphore.acquire();

        //Hide corner buttons
        hideCorners();

        //Build the road here too
        offerBuildRoads(recentBuild);

        //Wait for user to press button
        Catan.semaphore.acquire();

        //Hide road buttons;
        hideRoads();
    }

    public void offerBuildRoads(Corner c) {
        int column = c.getColumn();
        int row = c.getRow();

        //Vertical road connection
        Road r1 = null;
        try {
            if (column % 2 == 1) {
                //Connects upwards

                //Navigating around the staggered coordinate system
                if (row % 2 == 0) 
                    r1 = roads[2*row-1][column/2 + 1];
                else 
                    r1 = roads[2*row-1][column/2];
            } else {
                //Connects downwards
                r1 = roads[2*row+1][column/2];
            }
            if (r1 != null && r1.button.isEnabled()) {
                r1.button.setVisible(true);
                r1.setVisible(true);
                r1.buildable = true;
            }
        } catch (IndexOutOfBoundsException excpt) {}

        //Road to the right
        Road r2 = null;
        try {
            r2 = roads[row*2][column];
            if (r2 != null && r2.button.isEnabled()) {
                r2.button.setVisible(true);
                r2.setVisible(true);
                r2.buildable = true;
            }
        } catch (IndexOutOfBoundsException excpt) {}
        
        //Road to the left
        Road r3 = null;
        try {
            r3 = roads[row*2][column-1];
            if (r3 != null && r3.button.isEnabled()) {
                r3.button.setVisible(true);
                r3.setVisible(true);
                r3.buildable = true;
            }
        } catch (IndexOutOfBoundsException excpt) {}
    }

    public void hideCorners() {
        for (Corner[] cs : corners) {
            for (Corner c : cs) {
                if (c == null)
                    continue;
                c.buildable = false;

                switch (c.getStructure())  {
                    case NONE: 
                        c.setVisible(false);
                        break;
                    case SETTLEMENT:
                        c.setIcon(Catan.changeIconColor(Corner.STRUCTURE.SETTLEMENT.icon, c.getOwner().getColor()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void hideRoads() {
        for (Road[] rs : roads) {
            for (Road r : rs) {
                if (r == null) 
                    continue;
                r.button.setVisible(false);

                if (!r.buildable) 
                    continue;
                r.setVisible(false);
                r.buildable = false;
            }
        }
    }

    public Corner getCorner(int row, int column) {
        return corners[row][column];
    }

    public Tile getTile(int row, int column) {
        return tiles[row][column];
    }

    public void rollDice() {
        int roll = rn.nextInt(6)+1 + rn.nextInt(6)+1;
        rollLabel.setText(""+roll);
        for (Tile t : tilesNumRef[roll]) {
            //Top row of corners
            for (int i = 0; i < 3; i++) {
                Corner c = corners[t.getRow()][t.getColumn()*2 + i];
                if (c.getOwner() != null)
                    c.getOwner().addResource(t.getResource(), c.getStructure().generateAmount);
            }
            //Bottom row of corners
            for (int i = 0; i < 3; i++) {
                Corner c = corners[t.getRow()+1][(t.getColumn() - t.getRow()%2)*2 + 1 + i];
                if (c.getOwner() != null) 
                    c.getOwner().addResource(t.getResource(), c.getStructure().generateAmount);
            }
        }

        updateResourceAmount(wheatAmount, RESOURCE.WHEAT);
        updateResourceAmount(sheepAmount, RESOURCE.SHEEP);
        updateResourceAmount(timberAmount, RESOURCE.TIMBER);
        updateResourceAmount(brickAmount, RESOURCE.BRICK);
        updateResourceAmount(oreAmount, RESOURCE.ORE);
    }

    public void updateResourceAmount(JLabel Label, RESOURCE resource) {
        String str = "";
        if (getCurPlayer().getResource(resource) < 10)
            str += "0";
        str += getCurPlayer().getResource(resource);
        Label.setText(str);
    }

    public void offerTrades() {
        JDialog tradeMenu = new JDialog(Board.this, "Trading");
        tradeMenu.setSize(300, 280);
        tradeMenu.setVisible(true);
        tradeMenu.setLayout(null);
        tradeMenu.setResizable(false);

        JLabel tradeGiveLabel = new JLabel("Give: ");
        tradeMenu.add(tradeGiveLabel);
        tradeGiveLabel.setBounds(80, 10, 50, 20);
        JLabel tradeReceiveLabel = new JLabel("Receive: ");
        tradeMenu.add(tradeReceiveLabel);
        tradeReceiveLabel.setBounds(180, 10, 70, 20);

        //Wheat
        JLabel wheatTradeLabel = new JLabel("Wheat: ");
        tradeMenu.add(wheatTradeLabel);
        wheatTradeLabel.setBounds(10, 40, 70, 20);
        JSpinner wheatGive = new JSpinner(new SpinnerNumberModel(0, 0, getCurPlayer().getResource(RESOURCE.WHEAT), 1));
        tradeMenu.add(wheatGive);
        wheatGive.setBounds(80, 40, 50, 20);
        JSpinner wheatReceive = new JSpinner(new SpinnerNumberModel(0, 0, totalResources, 1));
        tradeMenu.add(wheatReceive);
        wheatReceive.setBounds(180, 40, 50, 20);

        //Sheep
        JLabel sheepTradeLabel = new JLabel("Sheep: ");
        tradeMenu.add(sheepTradeLabel);
        sheepTradeLabel.setBounds(10, 40 + 1*25, 70, 20);
        JSpinner sheepGive = new JSpinner(new SpinnerNumberModel(0, 0, getCurPlayer().getResource(RESOURCE.SHEEP), 1));
        tradeMenu.add(sheepGive);
        sheepGive.setBounds(80, 40 + 1*25, 50, 20);
        JSpinner sheepReceive = new JSpinner(new SpinnerNumberModel(0, 0, totalResources, 1));
        tradeMenu.add(sheepReceive);
        sheepReceive.setBounds(180, 40 + 1*25, 50, 20);

        //Timber
        JLabel timberTradeLabel = new JLabel("Timber: ");
        tradeMenu.add(timberTradeLabel);
        timberTradeLabel.setBounds(10, 40 + 2*25, 70, 20);
        JSpinner timberGive = new JSpinner(new SpinnerNumberModel(0, 0, getCurPlayer().getResource(RESOURCE.TIMBER), 1));
        tradeMenu.add(timberGive);
        timberGive.setBounds(80, 40 + 2*25, 50, 20);
        JSpinner timberReceive = new JSpinner(new SpinnerNumberModel(0, 0, totalResources, 1));
        tradeMenu.add(timberReceive);
        timberReceive.setBounds(180, 40 + 2*25, 50, 20);

        //Brick
        JLabel brickTradeLabel = new JLabel("Brick: ");
        tradeMenu.add(brickTradeLabel);
        brickTradeLabel.setBounds(10, 40 + 3*25, 70, 20);
        JSpinner brickGive = new JSpinner(new SpinnerNumberModel(0, 0, getCurPlayer().getResource(RESOURCE.BRICK), 1));
        tradeMenu.add(brickGive);
        brickGive.setBounds(80, 40 + 3*25, 50, 20);
        JSpinner brickReceive = new JSpinner(new SpinnerNumberModel(0, 0, totalResources, 1));
        tradeMenu.add(brickReceive);
        brickReceive.setBounds(180, 40 + 3*25, 50, 20);

        //Ore
        JLabel oreTradeLabel = new JLabel("Ore: ");
        tradeMenu.add(oreTradeLabel);
        oreTradeLabel.setBounds(10, 40 + 4*25, 70, 20);
        JSpinner oreGive = new JSpinner(new SpinnerNumberModel(0, 0, getCurPlayer().getResource(RESOURCE.ORE), 1));
        tradeMenu.add(oreGive);
        oreGive.setBounds(80, 40 + 4*25, 50, 20);
        JSpinner oreReceive = new JSpinner(new SpinnerNumberModel(0, 0, totalResources, 1));
        tradeMenu.add(oreReceive);
        oreReceive.setBounds(180, 40 + 4*25, 50, 20);


        //Results
        JButton bankTradeButton = new JButton("Trade with Bank");
        tradeMenu.add(bankTradeButton);
        bankTradeButton.setBounds(70, 180, 170, 20);
        bankTradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] receive = new int[RESOURCE.values().length-1];
                receive[RESOURCE.WHEAT.ordinal()] = (Integer) wheatReceive.getValue();
                receive[RESOURCE.SHEEP.ordinal()] = (Integer) sheepReceive.getValue();
                receive[RESOURCE.TIMBER.ordinal()] = (Integer) timberReceive.getValue();
                receive[RESOURCE.BRICK.ordinal()] = (Integer) brickReceive.getValue();
                receive[RESOURCE.ORE.ordinal()] = (Integer) oreReceive.getValue();

                int n = 0;
                for (int i : receive)
                    n += i;
                if (n != 1)
                    return;

                if ((Integer)wheatGive.getValue() == 4)
                    getCurPlayer().trade(Board.this.bank, new int[] {4, 0, 0, 0, 0}, receive);
                else if ((Integer)sheepGive.getValue() == 4)
                    getCurPlayer().trade(Board.this.bank, new int[] {0, 4, 0, 0, 0}, receive);
                else if ((Integer)timberGive.getValue() == 4)
                    getCurPlayer().trade(Board.this.bank, new int[] {0, 0, 4, 0, 0}, receive);
                else if ((Integer)brickGive.getValue() == 4)
                    getCurPlayer().trade(Board.this.bank, new int[] {0, 0, 0, 4, 0}, receive);
                else if ((Integer)oreGive.getValue() == 4)
                    getCurPlayer().trade(Board.this.bank, new int[] {0, 0, 0, 0, 4}, receive);
                
                updateResourceAmount(wheatAmount, RESOURCE.WHEAT);
                updateResourceAmount(sheepAmount, RESOURCE.SHEEP);
                updateResourceAmount(timberAmount, RESOURCE.TIMBER);
                updateResourceAmount(brickAmount, RESOURCE.BRICK);
                updateResourceAmount(oreAmount, RESOURCE.ORE);
                tradeMenu.dispose();
            }
        });

        JButton playerTradeButton = new JButton("Offer trade with players");
        tradeMenu.add(playerTradeButton);
        playerTradeButton.setBounds(70, 210, 170, 20);
    }

    public void nextPlayer() {
        curPlayerIndex++;
        curPlayerIndex %= players.size();
        curPlayerLabel.setText("Player " + (curPlayerIndex+1));
        curPlayerLabel.setForeground(getCurPlayer().getColor());

        updateResourceAmount(wheatAmount, RESOURCE.WHEAT);
        updateResourceAmount(sheepAmount, RESOURCE.SHEEP);
        updateResourceAmount(timberAmount, RESOURCE.TIMBER);
        updateResourceAmount(brickAmount, RESOURCE.BRICK);
        updateResourceAmount(oreAmount, RESOURCE.ORE);

        rollLabel.setText("--");
    }

    public Player getCurPlayer() {
        return players.get(curPlayerIndex);
    }

}