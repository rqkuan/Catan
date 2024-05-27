package Catan;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** Board 
 * The Board class manages both the GUI and the board interactions for the game. 
 * Each board has a map, sidebar, and bottombar used in displaying the gamemap, resources, and various interactable buttons. 
 * 
 * Attributes that the board keeps track of: 
 * - Tiles in the map and their locations
 * - Corners on each tile and their locations
 * - Roads on each tile and their locations
 * - Players
 * - Who the current player is
 * - Bank resources (although currently not implemented (undecided on whether I want this or not) *Rule Alteration*)
 * - Available development cards
 * - How long is the longest road, and who has it?
 * - How large is the largest army, and who has it?
 */
public class Board extends JFrame{

    /** RESOURCE
     * This enum implements the various resources available in the game. 
     * Each type of resource has an icon used to display its corresponding tile, 
     * labels to display the amount of that resource that the current player has (An "updateDisplay" function is used to update these labels), 
     * and labels and spinners for trading with the resource. 
     */
    public static enum RESOURCE {
        WHEAT("Catan/Icons/CatanWheatTile.png"),
        SHEEP("Catan/Icons/CatanSheepTile.png"),
        TIMBER("Catan/Icons/CatanTimberTile.png"),
        BRICK("Catan/Icons/CatanBrickTile.png"),
        ORE("Catan/Icons/CatanOreTile.png"),
        NONE("Catan/Icons/CatanWheatTile.png");

        public ImageIcon icon;
        public JLabel displayLabel, amountLabel, tradeLabel;
        public JSpinner tradeGive, tradeReceive;

        /** updateDisplay
         * Updates a resource's displayLabel and amountLabel for the specified board
         * @param board
         */
        public void updateDisplay(Board board) {
            String str = "";
            if (board.getCurPlayer().getResource(this) < 10)
                str += "0";
            str += board.getCurPlayer().getResource(this);
            this.amountLabel.setText(str);
        }

        private RESOURCE (String image_path) {
            icon = Catan.getResizedIcon(Tile.WIDTH, Tile.HEIGHT, image_path);
        }

        //The desert tile icon is just the wheat icon, but coloured black
        static {
            NONE.icon = Catan.changeIconColor(NONE.icon, new Color(0, 0, 0));
        }
    }

    /** DEVELOPMENT
     * This enum implements the various development cards that players can buy and use in the game. 
     * Each type of development card has a cardName attribute that helps in displaying the card itself, 
     * and implements the "developmentEffect" function, which handles the effects of each given card. 
     * 
     * Development cards may be used as soon as they're bought. *Rule Alteration*
     */
    public static enum DEVELOPMENT {
        KNIGHT("Knight") {
            public void developmentEffect(Board board) {
                //Knight cards allow players to move the thief to a new tile (they can steal from others too)

                board.offerPlaceThief();
                board.getCurPlayer().incrementArmy();
                
                //Update for largest army
                board.checkLargestArmy();
                board.updatePlayerDisplay();
                //Button reset/update is built-in to offerPlaceThief
            }
        },
        VICTORY_POINT("Victory Point") {
            public void developmentEffect(Board board) {
                //No effect. Each victory point card contributes to your total victory points 
                //(Victory points are openly displayed on each players turn. *Rule alteration*)
            }
        },
        ROAD_BUILDING("Road Building") {
            public void developmentEffect(Board board) {
                //Road building cards let the player build two roads for free (provided that they have the space to do so)

                board.getCurPlayer().addResource(RESOURCE.TIMBER, 2);
                board.getCurPlayer().addResource(RESOURCE.BRICK, 2);
                Thread roadBuilding = new Thread() {
                    public void run() {
                        for (int t = 0; t < 2; t++) {
                            if (!board.getCurPlayer().canBuildRoad(board))
                                return;
                            board.getCurPlayer().buildRoad(board);
                            Catan.waitForButton();
                        }
                    }  
                };
                roadBuilding.start();

                //Button reset/update is built-in to buildRoad
            }
        },
        YEAR_OF_PLENTY("Year of Plenty") {
            public void developmentEffect(Board board) {
                //Year of plenty cards allow the player to instantly gain two resources of their choice (can be two of the same)

                board.setButtonsEnabled(false);
                ForcedPopup resourceSelect = new ForcedPopup();
                for (RESOURCE r : Board.RESOURCE.values()) {
                    if (r == RESOURCE.NONE)
                        continue;
                    JMenuItem resourceMenuItem = new JMenuItem(r.name().charAt(0) + r.name().substring(1).toLowerCase());
                    resourceMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            board.getCurPlayer().addResource(r, 1);
                            r.updateDisplay(board);
                            Catan.semaphore.release();
                        }
                    });
                    resourceSelect.add(resourceMenuItem);
                }

                Thread yearOfPlenty = new Thread() {
                    public void run() {
                        for (int t = 0; t < 2; t++) {
                            resourceSelect.allowShowPopup();
                            resourceSelect.show(board.developButton, 0, 0);
                            Catan.waitForButton();
                            resourceSelect.closePopup();
                        }
                        board.setButtonsEnabled(true);
                        board.rollDiceButton.setEnabled(false);
                    }
                };
                yearOfPlenty.start();
            }
        },
        MONOPOLY("Monopoly") {
            public void developmentEffect(Board board) {
                //Monopoly cards lets the player claim all resources from other players of a chosen type. 
                //(The other players hand over all of that resource that they currently have) 

                board.setButtonsEnabled(false);
                ForcedPopup resourceSelect = new ForcedPopup();
                for (RESOURCE r : Board.RESOURCE.values()) {
                    if (r == RESOURCE.NONE)
                        continue;
                    JMenuItem resourceMenuItem = new JMenuItem(r.name().charAt(0) + r.name().substring(1).toLowerCase());
                    resourceMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            for (Player p : board.players) {
                                if (p == board.getCurPlayer())
                                    continue;
                                int[] receive = {0, 0, 0, 0, 0};
                                receive[r.ordinal()] = p.getResource(r);
                                board.getCurPlayer().trade(p, new int[] {0, 0, 0, 0, 0}, receive);
                                resourceSelect.closePopup();
                            }
                            r.updateDisplay(board);
                            Catan.semaphore.release();
                        }
                    });
                    resourceSelect.add(resourceMenuItem);
                }

                Thread monopoly = new Thread() {
                    public void run() {
                        resourceSelect.show(board.developButton, 0, 0);
                        Catan.waitForButton();
                        resourceSelect.closePopup();

                        board.setButtonsEnabled(true);
                        board.rollDiceButton.setEnabled(false);
                    }
                };
                monopoly.start();
            }
        };

        public String cardName;
        public abstract void developmentEffect(Board board);
        private DEVELOPMENT (String cardName) {
            this.cardName = cardName;
        }
    }

    //Board attributes
    public Corner recentBuild;
    public ArrayList<Player> players = new ArrayList<Player>();
    public Player bank = new Player(new Color(0, 0, 0)), longestRoadPlayer = bank, largestArmyPlayer = bank;
    private LinkedList<Tile> tilesNumRef[] = new LinkedList[13];
    private Tile tiles[][] = new Tile[5][5]; 
    private Corner corners[][] = new Corner[6][12]; 
    private Road roads[][] = new Road[11][11]; 
    private LinkedList<DEVELOPMENT> developmentCards = new LinkedList<DEVELOPMENT>();
    public int curPlayerIndex = 0;
    public static Random rn = new Random();

    //Catan constants/setup variables (to follow the rules of catan)
    private int resourceLimit, VPRequirement, longestRoad = 4, largestArmy = 2; 
    private LinkedList<RESOURCE> resourceRandomizer = new LinkedList<RESOURCE>();
    private LinkedList<Integer> tileNumRandomizer = new LinkedList<Integer>();

    //GUI components/attributes
    private static final int mapXOffset = 88, mapYOffset = 15;
    public JPanel sidebar, bottombar, map;
    public JButton buildRoadButton, buildSettlementButton, buildCityButton, rollDiceButton, endTurnButton, tradeButton, buyDevCardButton, developButton;
    public JLabel curPlayerLabel, rollLabel;
    public JDialog tradeMenu;


    /** Board Constructor
     * The Board constructor initializes the entire GUI window, 
     * building the map and placing all the appropriate buttons/labels in the sidebar and bottom bar. 
     * 
     * ActionListeners are also added to each button accordingly. 
     * @param resourceLimit --> will be used to keep track of how many resources a player can keep 
     *                          before rolling a 7 forces them to give up half their resources. (To be added with full multiplayer)
     * @param devCards --> used to fill developmentCards with the appropriate amount of each card
     * @param VPRequirement --> used to keep track of how many victory points are required to win
     */
    public Board(int resourceLimit, int[] devCards, int VPRequirement) {
        //Initializing attributes
        this.resourceLimit = resourceLimit;
        DEVELOPMENT[] tempDevs = DEVELOPMENT.values();
        for (int d = 0; d < devCards.length; d++) 
            for (int i = 0; i < devCards[d]; i++)
                developmentCards.add(tempDevs[d]);
        this.VPRequirement = VPRequirement;


    //Create gui upon construction of board object

        //Setting the Frame
        setTitle("Catan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        setSize(840, 600);
        setVisible(true);
        Dimension dm = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int) ((dm.getWidth() - this.getWidth()) / 2), (int) ((dm.getHeight() - this.getHeight()) / 2));

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

        //Setup for tiles
        for (int t = 2; t <= 12; t++)
            tilesNumRef[t] = new LinkedList<Tile>();

        //Setting up resource randomization
        for (int n = 0; n < 3; n++) {
            resourceRandomizer.add(RESOURCE.BRICK);
            resourceRandomizer.add(RESOURCE.ORE);
        }
        for (int n = 0; n < 4; n++) {
            resourceRandomizer.add(RESOURCE.WHEAT);
            resourceRandomizer.add(RESOURCE.SHEEP);
            resourceRandomizer.add(RESOURCE.TIMBER);
        }

        //Setting up tile number randomization
        for (int n = 0; n < 2; n++) 
            for (int i = 3; i <= 11; i++)
                if (i != 7)
                    tileNumRandomizer.add(i);
        tileNumRandomizer.add(2);
        tileNumRandomizer.add(12);
        tileNumRandomizer.add(0); //Desert Tile

        //Tiles
        makeTileRow(0, 1, 3);
        makeTileRow(1, 1, 4);
        makeTileRow(2, 0, 4);
        makeTileRow(3, 1, 4);
        makeTileRow(4, 1, 3);

        //Roads
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

        //Corners
        makeCornerRow(0, 2, 8); 
        makeCornerRow(1, 2, 10);
        makeCornerRow(2, 0, 10);
        makeCornerRow(3, 1, 11);
        makeCornerRow(4, 1, 9);
        makeCornerRow(5, 3, 9);


    //Buttons for building, trading, etc. 
        int buildButtonWidth = 100;
        int buildButtonHeight = 60;

        //Road Button
        buildRoadButton = new JButton("Road");
        bottombar.add(buildRoadButton);
        buildRoadButton.setBounds(bottombar.getWidth() - 3*(buildButtonWidth + 5), (bottombar.getHeight() - buildButtonHeight)/2, buildButtonWidth, buildButtonHeight);
        buildRoadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setButtonsEnabled(false);
                getCurPlayer().buildRoad(Board.this);
            }
        });

        //Settlement Button
        buildSettlementButton = new JButton("Settlement");
        bottombar.add(buildSettlementButton);
        buildSettlementButton.setBounds(bottombar.getWidth() - 2*(buildButtonWidth + 5), (bottombar.getHeight() - buildButtonHeight)/2, buildButtonWidth, buildButtonHeight);
        buildSettlementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setButtonsEnabled(false);
                getCurPlayer().buildSettlement(Board.this);
            }
        });

        //City Button
        buildCityButton = new JButton("City");
        bottombar.add(buildCityButton);
        buildCityButton.setBounds(bottombar.getWidth() - (buildButtonWidth + 5), (bottombar.getHeight() - buildButtonHeight)/2, buildButtonWidth, buildButtonHeight);
        buildCityButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setButtonsEnabled(false);
                getCurPlayer().buildCity(Board.this);
            }
        });

        //Dice Roll Button
        rollDiceButton = new JButton("Roll Dice");
        sidebar.add(rollDiceButton);
        rollDiceButton.setBounds(5, 450 + (bottombar.getHeight() - buildButtonHeight)/2, (sidebar.getWidth() - 15)/2, buildButtonHeight);
        rollDiceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rollDice();
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
                if (tradeMenu != null)
                    tradeMenu.dispose();

                //Check win
                if (getCurPlayerTotalVP() >= VPRequirement) {
        //Win Screen

                    //Win Screen Menu
                    JDialog winScreen = new JDialog(Board.this, "Game End");
                    winScreen.setLayout(null);
                    winScreen.setResizable(false);
                    winScreen.setSize(340, 200);
                    winScreen.setVisible(true);
                    winScreen.setLocation(getX() + getWidth()/2 - winScreen.getWidth()/2, getY() + getHeight()/2 - winScreen.getHeight()/2);
                    
                    //Winner Display Label
                    JLabel winLabel = new JLabel("Player " + (curPlayerIndex+1) + " Wins! (" + getCurPlayerTotalVP() + " Victory Points)", SwingConstants.CENTER);
                    winScreen.add(winLabel);

                    //"Longest Road" Holder Label
                    JLabel longestRoadLabel = new JLabel("", SwingConstants.CENTER);
                    winScreen.add(longestRoadLabel);
                    if (longestRoadPlayer != bank)
                        longestRoadLabel.setText("Longest Road: Player " + (players.indexOf(longestRoadPlayer)+1) + " (" + longestRoad + ")");
                    else
                        longestRoadLabel.setText("Longest Road: N/A");

                    //"Largest Army" Holder Label
                    JLabel largestArmyLabel = new JLabel("", SwingConstants.CENTER);
                    winScreen.add(largestArmyLabel);
                    if (largestArmyPlayer != bank)
                        largestArmyLabel.setText("Largest Army: Player " + (players.indexOf(largestArmyPlayer)+1) + " (" + largestArmy + ")");
                    else
                        largestArmyLabel.setText("Largest Army: N/A");

                    winLabel.setBounds(0, 10, winScreen.getWidth(), 30);
                    longestRoadLabel.setBounds(0, 40, winScreen.getWidth(), 30);
                    largestArmyLabel.setBounds(0, 70, winScreen.getWidth(), 30);

                    //Play Again Button
                    JButton playAgainButton = new JButton("Play Again");
                    winScreen.add(playAgainButton);
                    playAgainButton.setBounds(winScreen.getWidth()/2 - 80, 100, 160, 30);
                    playAgainButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Catan.playAgainSemaphore.release();
                            Board.this.dispose();
                        }
                    });

                    //Quit Button
                    JButton quitButton = new JButton("Quit");
                    winScreen.add(quitButton);
                    quitButton.setBounds(winScreen.getWidth()/2 - 80, 130, 160, 30);
                    quitButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.exit(0);
                        }
                    });

                    setButtonsEnabled(false);
                    return;
                }

                //Go to next player's turn
                nextPlayer();
                setButtonsEnabled(false);
                rollDiceButton.setEnabled(true);
                rollLabel.setText("--");
            }
        });

        //Trading Button
        tradeButton = new JButton("Trade");
        sidebar.add(tradeButton);
        tradeButton.setBounds(5, 360 + (bottombar.getHeight() - buildButtonHeight), sidebar.getWidth() - 10, buildButtonHeight);
        tradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tradeMenu != null)
                    tradeMenu.dispose();
                offerTrades();
            }
        });

        //Buy Development Card Button
        buyDevCardButton = new JButton("Development Card");
        sidebar.add(buyDevCardButton);
        buyDevCardButton.setBounds(5, 240 + (bottombar.getHeight() - buildButtonHeight), sidebar.getWidth() - 10, buildButtonHeight);
        buyDevCardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCurPlayer().buyDevelopmentCard(Board.this);
            }
        });

        //Use Development Card Button
        developButton = new JButton("Develop (Use card)");
        sidebar.add(developButton);
        developButton.setBounds(5, 300 + (bottombar.getHeight() - buildButtonHeight), sidebar.getWidth() - 10, buildButtonHeight);
        developButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPopupMenu devCardSelect = new JPopupMenu();
                for (DEVELOPMENT d : getCurPlayer().getDevCards()) {
                    JMenuItem devCardMenuItem = new JMenuItem(d.cardName);
                    devCardMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            d.developmentEffect(Board.this);
                            getCurPlayer().getDevCards().remove(d);
                            getCurPlayer().developed = true;
                            developmentCards.add(d);
                        }
                    });
                    devCardSelect.add(devCardMenuItem);
                    if (d == DEVELOPMENT.VICTORY_POINT)
                        devCardMenuItem.setEnabled(false);
                }
                devCardSelect.show(developButton, 0, 0);
            }
        });


        //Current Player Display
        curPlayerLabel = new JLabel();
        bottombar.add(curPlayerLabel);
        curPlayerLabel.setBounds(2, 2, 170, 16);
        
        //Current Player's Resources Display
        for (RESOURCE r : RESOURCE.values()) {
            if (r == RESOURCE.NONE)
                continue;
            r.displayLabel = new JLabel(); //Resource symbol/icon display
            bottombar.add(r.displayLabel);
            r.displayLabel.setBounds(20 + r.ordinal()*(60), 25, 50, 72);
            r.displayLabel.setIcon(Catan.getResizedIcon(50, 72, "Catan/Icons/Catan" + r.name().charAt(0) + r.name().substring(1).toLowerCase() + ".png"));
            r.amountLabel = new JLabel("00"); //Resource amount display
            bottombar.add(r.amountLabel);
            r.amountLabel.setBounds(37 + r.ordinal()*(60), 25 - 5 + 72, 30, 30);
        }
    }

    /** makeTileRow
     * This function creates and displays a continuous row of tiles on the map. 
     * The function randomizes each tile resource and number. 
     * 
     * Tiles are created on the specified row, from the specified first column to the specified last column (inclusive)
     * @param row
     * @param first_column
     * @param last_column
     */
    private void makeTileRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            //Creating tile
            int num = tileNumRandomizer.remove(rn.nextInt(tileNumRandomizer.size()));
            Tile tempTile;
            if (num == 0) { //Desert Tile
                tempTile = new Tile(this, row, column, RESOURCE.NONE);
                tempTile.thief = true;
            } else {
                tempTile = new Tile(this, row, column, resourceRandomizer.remove(rn.nextInt(resourceRandomizer.size())));
                tilesNumRef[num].add(tempTile); //Tracking tile number for resource generation
                tempTile.button.setText(""+num);
            }

            //Tracking in the tile matrix
            tiles[row][column] = tempTile;

            //Setting up in GUI
            map.add(tempTile);
            int x = (Tile.WIDTH - Tile.WIDTH/20)*column - (int)(0.5 * (row%2) * (Tile.WIDTH - 4));
            int y = (int)((Tile.HEIGHT*1.5 - Tile.HEIGHT/15)*row/2.0);
            tempTile.setBounds(mapXOffset+x, mapYOffset+y, Tile.WIDTH, Tile.HEIGHT);
        }
    }

    /** makeCornerRow
     * This function creates and displays a continuous row of corners on the map. 
     * Note that each column of tiles has two corresponding columns of corners. 
     * 
     * Corners are created on the specified row, from the specified first column to the specified last column (inclusive)
     * @param row
     * @param first_column
     * @param last_column
     */
    private void makeCornerRow(int row, int first_column, int last_column) {
        for (int column = first_column; column <= last_column; column++) {
            //Creating corner
            Corner tempCorner = new Corner(this, row, column);

            //Tracking in the corner matrix
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

    /** makeRoadRow
     * This function creates and displays a continuous row of roads on the map. 
     * Each corner has a corresponding road leading to the right AND a corresponding road going vertically up/down from it
     * 
     * Roads are created on the specified row, from the specified first column to the specified last column (inclusive)
     * @param row
     * @param first_column
     * @param last_column
     */
    private void makeRoadRow(int row, int first_column, int last_column) {
        if (row % 2 == 0) {
            //Zigzagging (Horizontal) roads
            for (int column = first_column; column <= last_column; column++) {
                //Creating road
                Road tempRoad;
                if (column % 2 == 0) 
                    tempRoad = new Road(Road.roadSlantUp, this, row, column);       //Even-columned roads slant up
                else 
                    tempRoad = new Road(Road.roadSlantDown, this, row, column);     //Odd-columned roads slant down
                
                //Tracking in the road matrix
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
                //Creating road
                Road tempRoad = new Road(Road.roadVertical, this, row, column);

                //Tracking in the road matrix
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

    /** offerStartingBuild
     * This function handles building the first settlements/roads that each player starts with. 
     * When called, it makes the player build both a settlement and a road coming from that settlement. 
     */
    public void offerStartingBuild() {
        //Show corner buttons for building
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
        Catan.waitForButton();

        //Hide corner buttons after building
        hideCorners();

        //Build the road here too
        offerBuildRoads(recentBuild);

        //Wait for user to press button
        Catan.waitForButton();

        //Hide road buttons;
        hideRoads();
    }

    /** offerBuildRoads
     * This function enables the road building buttons adjacent to a specified corner, 
     * allowing the current player to build a road. 
     * @param c
     * 
     * This function is also used in the Player class for the buildRoad function (to enable the appropriate buttons). 
     */
    public void offerBuildRoads(Corner c) {
        for (Road r : getAdjacentRoads(c)) {
            if (!r.button.isEnabled()) 
                continue;
            r.button.setVisible(true);
            r.setVisible(true);
            r.buildable = true;
        }
    }

    /** getAdjacentRoads
     * This function returns the existing and adjacent roads of a specified corner as a LinkedList. 
     * @param c
     * @return adjRoads
     * 
     * This function is used in the offerBuildRoads function to find which roads to enable,
     * and in the DFS implementation used to find a player's longest continuous road (for computing corner adjacency). 
     */
    public LinkedList<Road> getAdjacentRoads (Corner c) {
        LinkedList<Road> adjRoads = new LinkedList<Road>();

        int column = c.getColumn();
        int row = c.getRow();

        //Vertical road connection
        Road r = null;
        try {
            if (column % 2 == 1) {
                //Connects upwards

                //Navigating around the staggered coordinate system
                if (row % 2 == 0) 
                    r = roads[2*row-1][column/2 + 1];
                else 
                    r = roads[2*row-1][column/2];
            } else {
                //Connects downwards
                r = roads[2*row+1][column/2];
            }
            if (r != null)
                adjRoads.add(r);
        } catch (IndexOutOfBoundsException excpt) {}

        //Road to the left
        try {
            r = roads[row*2][column-1];
            if (r != null)
                adjRoads.add(r);
        } catch (IndexOutOfBoundsException excpt) {}

        //Road to the right
        try {
            r = roads[row*2][column];
            if (r != null)
                adjRoads.add(r);
        } catch (IndexOutOfBoundsException excpt) {}

        return adjRoads;
    }

    /** getAdjacentCorners
     * This function returns the existing and adjacent corners of a specified corner as a LinkedList. 
     * @param c
     * @return adjCorners
     * 
     * This function is used in the DFS implementation used to find a player's longest continuous road. 
     * (for computing corner adjacency)
     */
    public LinkedList<Corner> getAdjacentCorners(Corner c) {
        LinkedList<Corner> adjCorners = new LinkedList<Corner>();

        int row = c.getRow();
        int column = c.getColumn();

        //Vertical Connections
        try {
            if (column % 2 == 1) {
                //Connects upwards

                //Navigating around the staggered coordinate system
                if (row % 2 == 0) 
                    c = corners[row-1][column+1];
                else 
                    c = corners[row-1][column-1];
            } else {
                //Connects downwards

                //Navigating around the staggered coordinate system
                if (row % 2 == 0) 
                    c = corners[row+1][column+1];
                else 
                    c = corners[row+1][column-1];
            }
            if (c != null) 
                adjCorners.add(c);
        } catch (IndexOutOfBoundsException excpt) {}

        //Leftward Connection
        try {
            c = corners[row][column-1];
            if (c != null) 
                adjCorners.add(c);
        } catch (IndexOutOfBoundsException excpt){}

        //Rightward Connection
        try {
            c = corners[row][column+1];
            if (c != null) 
                adjCorners.add(c);
        } catch (IndexOutOfBoundsException excpt){}

        return adjCorners;
    }

    /** hideCorners
     * This function hides all offered building locations for settlements/cities (and disables them). 
     * 
     * This function is always used to update the board after a corner is built on. 
     */
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

    /** hideRoads
     * This function hides all offered building locations for roads (and disables them). 
     * 
     * This function is always used to update the board after a road is built. 
     */
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

    /** rollDice
     * This function implements rolling the dice and all features associated with it. 
     * It is called when the dice rolling button is pressed. 
     */
    private void rollDice() {
        //Simulate a dice roll and update the GUI
        int roll = rn.nextInt(6)+1 + rn.nextInt(6)+1;
        rollLabel.setText(""+roll);

        /* Rolling a seven allows the player to move the thief, but doesn't generate any resources. 
         * 
         * *Rule Alteration* 
         * The thief stealing from players with too many resources is currently unimplemented. 
         * (To be added with full multiplayer)
         */
        if (roll == 7) {
            offerPlaceThief();
            return;
        }


        //Generate resources from each tile with the number rolled
        for (Tile t : tilesNumRef[roll]) {
            //Tiles with thieves on them don't generate resources
            if (t.thief)
                continue;
                
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

        //Update the resource display
        for (RESOURCE r : RESOURCE.values()) 
            if (r != RESOURCE.NONE) 
                r.updateDisplay(this);
        
        //Update buttons for new resources (some things may have become newly affordable)
        setButtonsEnabled(true);
        rollDiceButton.setEnabled(false);
    }

    /** offerTrades
     * This function creates a window interface used for trading (both with other players and with the bank)
     * It is called when the trading button is pressed. 
     */
    private void offerTrades() {
        //Create trading window
        tradeMenu = new JDialog(Board.this, "Trading");
        tradeMenu.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Board.this.setButtonsEnabled(true);
                Board.this.rollDiceButton.setEnabled(false);
            }
        });

        //Display trading window
        tradeMenu.setSize(300, 280);
        tradeMenu.setLocation(getX()+getWidth()-tradeMenu.getWidth(), getY());
        tradeMenu.setVisible(true);
        tradeMenu.setLayout(null);
        tradeMenu.setResizable(false);

        //Setting up labels to show which values represent giving/receiving resources
        JLabel tradeGiveLabel = new JLabel("Give: ");
        tradeMenu.add(tradeGiveLabel);
        tradeGiveLabel.setBounds(80, 10, 50, 20);
        JLabel tradeReceiveLabel = new JLabel("Receive: ");
        tradeMenu.add(tradeReceiveLabel);
        tradeReceiveLabel.setBounds(180, 10, 70, 20);

        //Display resource labels and spinners for specifying quantities
        for (RESOURCE r : RESOURCE.values()) {
            if (r == RESOURCE.NONE)
                continue;
            //Label display
            r.tradeLabel = new JLabel(r.name().charAt(0) + r.name().substring(1).toLowerCase() + ": ");
            tradeMenu.add(r.tradeLabel);
            r.tradeLabel.setBounds(10, 40 + r.ordinal()*25, 70, 20);
            
            //Give-amount spinner display
            r.tradeGive = new JSpinner(new SpinnerNumberModel(0, 0, getCurPlayer().getResource(r), 1));
            tradeMenu.add(r.tradeGive);
            r.tradeGive.setBounds(80, 40 + r.ordinal()*25, 50, 20);
            
            //Receive-amount spinner display
            r.tradeReceive = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
            tradeMenu.add(r.tradeReceive);
            r.tradeReceive.setBounds(180, 40 + r.ordinal()*25, 50, 20);
        }

        //Trade With Bank button
        JButton bankTradeButton = new JButton("Trade with Bank");
        tradeMenu.add(bankTradeButton);
        bankTradeButton.setBounds(70, 180, 170, 20);
        bankTradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //When trading with the bank, the player must give exactly 4 of any single resource to receive 1 of any resource
                
                //Make sure the specified receiving values is only 1 of a single resource
                int[] receive = new int[RESOURCE.values().length-1];
                for (RESOURCE r : RESOURCE.values()) {
                    if (r == RESOURCE.NONE)
                        continue;
                    receive[r.ordinal()] = (Integer) r.tradeReceive.getValue();
                }
                int n = 0;
                for (int i : receive)
                    n += i;
                if (n != 1)
                    return;

                //Make sure the specified giving values is exactly 4 of any single resource
                RESOURCE giveBankResource = RESOURCE.NONE;
                for (RESOURCE r : RESOURCE.values()) {
                    if (r == RESOURCE.NONE)
                        continue;
                    if ((Integer) r.tradeGive.getValue() == 4) {
                        giveBankResource = r;
                        break;
                    }
                }
                int[] give = {0, 0, 0, 0, 0};
                give[giveBankResource.ordinal()] = 4;

                //Trade with the bank
                getCurPlayer().trade(Board.this.bank, give, receive);

                //Close the trade menu
                tradeMenu.dispose();

                //Update resource display
                for (RESOURCE r : RESOURCE.values()) 
                if (r != RESOURCE.NONE) 
                    r.updateDisplay(Board.this);
                
                //Update buttons for new resources (some things may have become newly affordable)
                setButtonsEnabled(true);
                rollDiceButton.setEnabled(false);
            }
        });

        //Trade With Player Button
        JButton playerTradeButton = new JButton("Offer trade with players");
        tradeMenu.add(playerTradeButton);
        playerTradeButton.setBounds(70, 210, 170, 20);
        playerTradeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Prevent the player from offering another player-trade while the current offer is going. 
                //(It just becomes confusing)
                playerTradeButton.setEnabled(false); 

                //Grab the specified giving and receiving amounts from the spinners
                int[] give = new int[RESOURCE.values().length-1];
                int[] receive = new int[RESOURCE.values().length-1];
                for (RESOURCE r : RESOURCE.values()) {
                    if (r == RESOURCE.NONE)
                        continue;
                    give[r.ordinal()] = (Integer) r.tradeGive.getValue();
                    receive[r.ordinal()] = (Integer) r.tradeReceive.getValue();
                }

                //Popup menu to select which player wants to accept the trade
                ForcedPopup playerTradeSelect = new ForcedPopup();
                for (int i = 0; i < players.size(); i++) {
                    final Player p = players.get(i);
                    if (p == getCurPlayer())
                        continue;

                    //Adding players to the menu
                    JMenuItem playerTradeMenuItem = new JMenuItem("Player " + (i+1));
                    playerTradeMenuItem.setForeground(players.get(i).getColor());
                    playerTradeMenuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //Trade with this player (specified by which was pressed button)

                            //Make sure that both players actually have the specified resources to trade with
                            for (int i = 0; i < RESOURCE.values().length-1; i++) {
                                if (getCurPlayer().getResource(RESOURCE.values()[i]) < give[i])
                                    return;
                                if (p.getResource(RESOURCE.values()[i]) < receive[i])
                                    return;
                            }
                            
                            //Do the trade
                            getCurPlayer().trade(p, give, receive);

                            //Close the trade menu
                            playerTradeSelect.closePopup();
                            tradeMenu.dispose();
                            
                            //Update resource display
                            for (RESOURCE r : RESOURCE.values()) 
                                if (r != RESOURCE.NONE) 
                                    r.updateDisplay(Board.this);                            

                            //Update buttons for new resources (some things may have become newly affordable)
                            setButtonsEnabled(true);
                            rollDiceButton.setEnabled(false);
                        }
                    });
                    playerTradeSelect.add(playerTradeMenuItem);
                }

                //Adding the option to cancel instead of choosing a player to trade with
                JMenuItem cancelTrade = new JMenuItem("Cancel");
                playerTradeSelect.add(cancelTrade);
                cancelTrade.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //Reset
                        playerTradeSelect.closePopup();
                        playerTradeButton.setEnabled(true);
                        bankTradeButton.setEnabled(true);
                    }
                });
                playerTradeSelect.show(tradeMenu, 0, 0);
            }
        });
    }

    /** offerPlaceThief
     * This function allows the player to move the thief to a different tile, 
     * preventing that tile from generating resources, 
     * and letting the player steal from one other player that has a building "touching" that tile. 
     * 
     * This function is called whenever a dice roll results in a 7, and when a "Knight" development card is used. 
     */
    public void offerPlaceThief() {
        //Prevent other buttons from being pressed until the thief is placed (and a player is stolen from if possible). 
        setButtonsEnabled(false);
        Thread placeThief = new Thread() {
            @Override
            public void run() {
                //Let player place the thief
                Tile prevThieved = null;
                for (Tile[] ts : tiles) {
                    for (Tile t : ts) {
                        if (t == null)
                            continue;
                        if (!t.thief)
                            t.button.setEnabled(true);
                        else
                            prevThieved = t;
                    }
                }

                //Make sure player selects a tile
                Catan.waitForButton();

                //Make sure the player can only select that one tile
                for (Tile[] ts : tiles) {
                    for (Tile t : ts) {
                        if (t == null)
                            continue;
                        t.button.setEnabled(false);
                    }
                }

                //Update previously thieved tile
                prevThieved.thief = false;
                prevThieved.iconDisplay.setEnabled(true);

                //Make sure player selects someone to steal from (if possible)
                /* Special edge case: 
                 * Catan.waitForButton() may drain the permit meant for this acquire if there is no player to steal from. 
                 * Instead of using the function, just manually acquire without draining previous permits. 
                 */
                try {
                    Catan.semaphore.acquire();
                } catch (InterruptedException excpt) {}

                //Update resource display
                for (RESOURCE r : RESOURCE.values()) 
                    if (r != RESOURCE.NONE) 
                        r.updateDisplay(Board.this);
                
                //Reseting buttons
                setButtonsEnabled(true);
                rollDiceButton.setEnabled(false);
            };
        };
        placeThief.start();
    }

    /** nextPlayer
     * This function iterates curPlayerIndex to the next player index (wrapping around to Player 1 after the last player), 
     * and updates the GUI accordingly. 
     */
    public void nextPlayer() {
        //Iterate to next player
        curPlayerIndex++;
        curPlayerIndex %= players.size();

        //Updating player display
        checkLongestRoad();
        checkLargestArmy();
        updatePlayerDisplay();
        getCurPlayer().developed = false;

        //Updating resource display
        for (RESOURCE r : RESOURCE.values()) 
            if (r != RESOURCE.NONE) 
                r.updateDisplay(this);
    }

    /** updatePlayerDisplay
     * This function updates the player display to show the current player (by number)
     * and the amount of victory points they have. 
     * The display label text is also changed to match the player's colour (for additional clarity). 
     */
    public void updatePlayerDisplay() {
        curPlayerLabel.setText("Player " + (curPlayerIndex+1) + " (" + getCurPlayerTotalVP() + " VP)");
        curPlayerLabel.setForeground(getCurPlayer().getColor());
    }

    /** setButtonsEnabled
     * This function enables/disables all the buttons *other than the roll button* in the sidebar and bottombar. 
     * When enabling them, only buttons that can currently be used by the player are enabled (for easier interaction). 
     * @param enabled
     */
    public void setButtonsEnabled(boolean enabled) {
        for (Component c : Board.this.sidebar.getComponents()) {
            try {
                JButton b = (JButton) c;
                b.setEnabled(enabled);
            } catch (Exception excpt) {}
        }
        for (Component c : Board.this.bottombar.getComponents()) {
            try {
                JButton b = (JButton) c;
                b.setEnabled(enabled);
            } catch (Exception excpt) {}
        }

        if (!enabled)
            return;
        rollDiceButton.setEnabled(false);

        //If the buttons were enabled, disable the ones that can't be used. 
        Player p = getCurPlayer();
        buildRoadButton.setEnabled(p.canBuildRoad(this));
        buildSettlementButton.setEnabled(p.canBuildSettlement());
        buildCityButton.setEnabled(p.canBuildCity());
        buyDevCardButton.setEnabled(p.canBuyDevCard() && developmentCards.size() != 0);
        developButton.setEnabled(p.getDevCards().size() > 0 && !p.developed);
    }

    /** checkLongestRoad
     * This function checks the length of the current player's longest road and compares it to the current "longest road."
     * If it finds that this player's longest road is longer, 
     * it updates the length of current longest road and the player that holds it (to be used in calculating total VP). 
     */
    public void checkLongestRoad() {
        int curPlayerLongestRoad = getCurPlayer().getLongestRoad(Board.this);
        if (curPlayerLongestRoad > longestRoad) {
            longestRoad = curPlayerLongestRoad;
            longestRoadPlayer = getCurPlayer();
        }
    }

    /** checkLargestArmy
     * This function checks the current player's "army size" and compares it to the current "largest army."
     * If it finds that this player's army is "larger," 
     * it updates the size of current largest army and the player that holds it (to be used in calculating total VP). 
     */
    public void checkLargestArmy() {
        if (getCurPlayer().getArmy() > largestArmy) {
            largestArmy = getCurPlayer().getArmy();
            largestArmyPlayer = getCurPlayer();
        }
    }

    /** getCurPlayerTotalVP
     * This function calculates and returns the current player's total victory points. 
     * (Takes into account longest road and largest army)
     * @return totalVP
     */
    private int getCurPlayerTotalVP() {
        //Calculate totalVP (to include longest road and biggest army)
        int totalVP = getCurPlayer().getVictoryPoints();
        if (longestRoadPlayer == getCurPlayer())
            totalVP += 2;
        if (largestArmyPlayer == getCurPlayer())
            totalVP += 2;
        
        return totalVP;
    }

    /** getDevCard
     * This function returns a random, available (not held by a player) development card (and makes it "unavailable"). 
     * @return devCard
     */
    public DEVELOPMENT getDevCard() {
        return developmentCards.remove(rn.nextInt(developmentCards.size()));
    }

    /** getCurPlayer
     * This function returns the player whose turn is currently happening. 
     * (Found from curPlayerIndex)
     * @return curPlayer
     */
    public Player getCurPlayer() {
        return players.get(curPlayerIndex);
    }

    /** getCorner
     * This function returns the corner in the corner matrix at the specified row and column. 
     * @param row
     * @param column
     * @return corner
     * 
     * (Used in theiving to help find players that can be stolen from)
     */
    public Corner getCorner(int row, int column) {
        return corners[row][column];
    }

    /** getTile
     * This function returns the tile in the tile matrix at the specified row and column. 
     * @param row
     * @param column
     * @return tile
     * 
     * (Used when starting settlements are being placed to help in giving players their starting resources)
     */
    public Tile getTile(int row, int column) {
        return tiles[row][column];
    }
}