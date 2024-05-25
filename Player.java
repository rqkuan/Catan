package Catan;

import java.awt.*;
import java.util.*;

/** Player
 * The Player class keeps track of player data and handles player actions.
 * Each player
 * 
 * Attributes that the player keeps track of: 
 * - Which corners is has "access" to
 * - What resources it has (and its total number of resources)
 * - What development cards it has
 * - How many settlements/cities/roads it can still place
 * - How "large" its army is
 * - What colour the player's buildings are
 * - Whether or not the player has used a development card this turn
 */
public class Player {

    private LinkedList<Corner> accessibleCorners = new LinkedList<Corner>();
    private LinkedList<Board.DEVELOPMENT> devCards = new LinkedList<Board.DEVELOPMENT>();
    private int[] resources = new int[Board.RESOURCE.values().length-1];
    private int settlements = 3, cities = 4, roads = 13, victoryPoints = 2, totalResources = 0, army = 0;
    private Color color;
    public boolean developed = false;

    /** Player Constructor
     * The Player Constructor only initialized the player's building colour, 
     * as the other attributes must start the same for all players. 
     * (This may change when full multiplayer is added, in case I want to give more customization options)
     * @param color
     */
    public Player(Color color) {
        this.color = color;
    }

    /** distFarthestCorner
     * This function executes a Depth-First Search traversal of the player's accessible corners, 
     * and returns the distance of the corner farthest from the starting point. 
     * 
     * @param dist --> HashMap used to keep track of corner distances (and also which corners have already been visited)
     * @param board --> The board's road and corner matricies are used in computing adjancency
     * @param start --> This initially passed as the starting corner, and is then used in the recursive call as the "current" corner
     * @return farthest --> The farthest distance of any corner reached from the start
     * 
     * This function is used in computing the player's longest road in Player.getLongestRoad(Board)
     */
    private int distFarthestCorner(HashMap<Corner, Integer> dist, Board board, Corner start) {
        int farthest = dist.get(start);

        LinkedList<Corner> adjCorners = board.getAdjacentCorners(start);
        LinkedList<Road> adjRoads = board.getAdjacentRoads(start);
        for (int i = 0; i < adjCorners.size(); i++) {
            Corner c = adjCorners.get(i);
            if (adjRoads.get(i).getOwner() != this) //Make sure the connecting road is owned by the player
                continue;
            if (dist.containsKey(c)) //Already visited
                continue;
            dist.put(c, dist.get(start) + 1);
            farthest = Integer.max(farthest, distFarthestCorner(dist, board, c));
        }

        return farthest;
    }

    /** getLongestRoad
     * This function computes and returns the length of the player's longest road using the distFarthestCorner function. 
     * 
     * Longest path of an acyclic graph can be found in the same way you would find a tree's diameter. 
     * The only difference in this case is that you must use DFS instead of BFS for flood-fill, 
     * since even though you are not allowed to go in cycles, the graph is not actually acyclic. 
     * 
     * @param board --> Used for computing adjacencies
     * @return int --> The length of the longest road
     */
    public int getLongestRoad(Board board) {
        //Initializing variables
        HashMap<Corner, Integer> dist;
        Corner diameterEnd;

        //Find one end of diameter
        dist = new HashMap<Corner, Integer>();
        dist.put(accessibleCorners.get(0), 0);
        distFarthestCorner(dist, board, accessibleCorners.get(0));

        //Getting the actual diameter end as a corner object
        diameterEnd = accessibleCorners.get(0);
        int farthest = 0;
        for (Corner c : dist.keySet()) {
            if (dist.get(c) <= farthest)
                continue;
            farthest = dist.get(c);
            diameterEnd = c;
        }
        //Find other end of diameter
        dist = new HashMap<Corner, Integer>();
        dist.put(diameterEnd, 0);
        farthest = distFarthestCorner(dist, board, diameterEnd);


        //If the two starting settlements are connected, just return the longest road
        if (dist.containsKey(accessibleCorners.get(2)))     
            return farthest;


        /* If the starting settlements are not connected, 
         * you have to compute the diameter of the second startling settlement's graph too,
         * since it wouldn't have been reached in the initial search. 
         */

        //Find one end of diameter
        dist = new HashMap<Corner, Integer>();
        dist.put(accessibleCorners.get(2), 0);
        distFarthestCorner(dist, board, accessibleCorners.get(2));

        //Getting the actual diameter end as a corner object
        diameterEnd = accessibleCorners.get(2);
        int farthest2nd = 0;
        for (Corner c : dist.keySet()) {
            if (dist.get(c) <= farthest)
                continue;
            farthest2nd = dist.get(c);
            diameterEnd = c;
        }
        //Find other end of diameter
        dist = new HashMap<Corner, Integer>();
        dist.put(diameterEnd, 0);
        farthest2nd = distFarthestCorner(dist, board, diameterEnd);

        //Return the longest road found
        return Integer.max(farthest, farthest2nd);
    }

    /** canBuildRoad
     * This function checks whether or not the player can build a road. 
     * It takes into account whether the player still has roads to build, 
     * if they have the resources to do so, and if there is space to build a road. 
     * 
     * @param board --> used to get road objects for checking if there is space to build a new road
     * @return boolean --> whether or not the player can build a road. 
     */
    public boolean canBuildRoad(Board board) {
        //Check resources & build limit
        if (roads == 0)
            return false;
        if (resources[Board.RESOURCE.TIMBER.ordinal()] == 0) 
            return false;
        if (resources[Board.RESOURCE.BRICK.ordinal()] == 0)
            return false;
        
        //Check space to build
        for (Corner c : accessibleCorners) 
            for (Road r : board.getAdjacentRoads(c))
                if (r.getOwner() == null)
                    return true;
        return false;
    }

    /** buildRoad
     * This function simulates building a road on the specified board for the player. 
     * @param board
     */
    public void buildRoad(Board board) {
        //Removing resources (the cost of building a road)
        addResource(Board.RESOURCE.TIMBER, -1);
        addResource(Board.RESOURCE.BRICK, -1);

        //Updating resource displays
        Board.RESOURCE.TIMBER.updateDisplay(board);
        Board.RESOURCE.BRICK.updateDisplay(board);
        
        //Building thread
        Thread buildRoad = new Thread() {
            @Override
            public void run() {
                //Show buildable roads
                for (Corner c : accessibleCorners) 
                    if (c.getOwner() == null || c.getOwner() == Player.this) 
                        board.offerBuildRoads(c);
                
                //Make sure they click a road button
                Catan.waitForButton();

                //Hide (and disable) road buttons;
                board.hideRoads();

                //Renable sidebar and bottombar buttons
                board.setButtonsEnabled(true);
                board.rollDiceButton.setEnabled(false);

                //Updates for longest road
                board.checkLongestRoad();
                board.updatePlayerDisplay();

                //Building count
                roads--;

                Catan.semaphore.release();
            }
                
        };
        buildRoad.start();
    }

    /** canBuildSettlement
     * This function checks whether or not the player can build a settlement. 
     * It takes into account whether the player still has settlements to build, 
     * if they have the resources to do so, and if there is space to build a settlement. 
     * 
     * @return boolean --> whether or not the player can build a settlement. 
     */
    public boolean canBuildSettlement() {
        //Check resources & build limit
        if (settlements == 0)
            return false;
        if (resources[Board.RESOURCE.TIMBER.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.BRICK.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.SHEEP.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] == 0)
            return false;

        //Check space to build
        for (Corner c : accessibleCorners) 
            if (c.getOwner() == null && c.isEnabled()) 
                return true;
        return false;
    }

    /** buildSettlement
     * This function simulates building a settlement on the specified board for the player. 
     * @param board
     */
    public void buildSettlement(Board board) {
        //Removing resources (the cost of building a settlement)
        addResource(Board.RESOURCE.TIMBER, -1);
        addResource(Board.RESOURCE.BRICK, -1);
        addResource(Board.RESOURCE.SHEEP, -1);
        addResource(Board.RESOURCE.WHEAT, -1);

        //Updating resource displays
        Board.RESOURCE.TIMBER.updateDisplay(board);
        Board.RESOURCE.BRICK.updateDisplay(board);
        Board.RESOURCE.SHEEP.updateDisplay(board);
        Board.RESOURCE.WHEAT.updateDisplay(board);

        //Building thread
        Thread buildSettlement = new Thread() {
            @Override
            public void run() {
                //Show buildable corners
                for (Corner c : accessibleCorners) {
                    if (c.isEnabled() && c.getOwner() == null) {
                        c.buildable = true;
                        c.setVisible(true);
                    }   
                }

                //Make sure they click a corner button
                Catan.waitForButton();

                //Hide (and disable) corner buttons;
                board.hideCorners();

                //Renable sidebar and bottombar buttons
                board.setButtonsEnabled(true);
                board.rollDiceButton.setEnabled(false);

                //Victory points and building count
                victoryPoints++;
                settlements--;
                board.updatePlayerDisplay();

                Catan.semaphore.release();
            }
                
        };
        buildSettlement.start();
    }

    /** canBuildCity
     * This function checks whether or not the player can build a city. 
     * It takes into account whether the player still has cities to build, 
     * if they have the resources to do so, and if there is space to build a city. 
     * 
     * @return boolean --> whether or not the player can build a city. 
     */
    public boolean canBuildCity() {
        //Check resources & build limit
        if (cities == 0)
            return false;
        if (resources[Board.RESOURCE.ORE.ordinal()] < 3)
            return false;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] < 2)
            return false;
        
        //Check settlement to upgrade
        for (Corner c : accessibleCorners) 
            if (c.getOwner() == this && c.getStructure() == Corner.STRUCTURE.SETTLEMENT) 
                return true;
        return false;
    }

    /** buildCity
     * This function simulates building a city on the specified board for the player. 
     * @param board
     */
    public void buildCity(Board board) {
        //Removing resources (the cost of building a city)
        addResource(Board.RESOURCE.ORE, -3);
        addResource(Board.RESOURCE.WHEAT, -2);

        //Updating resource displays
        Board.RESOURCE.ORE.updateDisplay(board);
        Board.RESOURCE.WHEAT.updateDisplay(board);

        //Building thread
        Thread buildCity = new Thread() {
            @Override
            public void run() {
                //Show buildable corners
                for (Corner c : accessibleCorners) {
                    if (c.isEnabled() && c.getOwner() == Player.this && c.getStructure() == Corner.STRUCTURE.SETTLEMENT) {
                        c.buildable = true;
                        c.setIcon(Corner.STRUCTURE.NONE.icon);
                    }   
                }

                //Make sure they click a corner button
                Catan.waitForButton();

                //Hide (and disable) corner buttons;
                board.hideCorners();

                //Renable sidebar and bottombar buttons
                board.setButtonsEnabled(true);
                board.rollDiceButton.setEnabled(false);

                //Victory points and building count
                victoryPoints++;
                settlements++;
                cities--;
                board.updatePlayerDisplay();

                Catan.semaphore.release();
            }
                
        };
        buildCity.start();
    }

    /** canbuyDevCard
     * This function checks whether or not the player can buy a development card. 
     * It takes only into account whether the player has the resources to do so. 
     * (Whether or not the board has any development cards left is handled separately by the board itself)
     * 
     * @return boolean --> whether or not the player can afford a development card. 
     */
    public boolean canBuyDevCard() {
        if (resources[Board.RESOURCE.ORE.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.SHEEP.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] == 0)
            return false;
        return true;
    }

    /** buyDevelopmentCard
     * This function simulates buying a development card from the specified board for the player. 
     * @param board
     */
    public void buyDevelopmentCard(Board board) {
        //Removing resources (the cost of buying a development card)
        addResource(Board.RESOURCE.ORE, -1);
        addResource(Board.RESOURCE.SHEEP, -1);
        addResource(Board.RESOURCE.WHEAT, -1);

        //Updating resource displays
        Board.RESOURCE.ORE.updateDisplay(board);
        Board.RESOURCE.SHEEP.updateDisplay(board);
        Board.RESOURCE.WHEAT.updateDisplay(board);

        //Buying the development card
        Board.DEVELOPMENT devCard = board.getDevCard();
        devCards.add(devCard);

        //If the card is a victory point
        if (devCard == Board.DEVELOPMENT.VICTORY_POINT) {
            victoryPoints++;
            board.updatePlayerDisplay();
        }

        //Update buttons from buying cost
        board.setButtonsEnabled(true);
        board.rollDiceButton.setEnabled(false);
    }

    /** trade
     * This function simulates trading with another player. 
     * @param player --> The other player
     * @param give --> Resources given to the other player
     * @param receive -- Resources received from the other player
     */
    public void trade(Player player, int[] give, int[] receive) {
        for (int i = 0; i < Board.RESOURCE.values().length-1; i++) {
            //Updating the player's resources
            resources[i] -= give[i];
            resources[i] += receive[i];
            totalResources -= give[i];
            totalResources += receive[i];
            
            //Updating the other player's resources
            player.resources[i] += give[i];
            player.resources[i] -= receive[i];    
            player.totalResources += give[i];
            player.totalResources -= receive[i];
        }
    }

    /** addResource
     * This function adds a specified amount of a specified resource to the player's current resource pool. 
     * @param resource
     * @param amount
     */
    public void addResource(Board.RESOURCE resource, int amount) {
        if (resource == Board.RESOURCE.NONE)
            return;
        resources[resource.ordinal()] += amount;
        totalResources += amount;
    }

    /** addCorner
     * This funcion adds a specified corner to the player's list of accessible corners (if it's not already in the list). 
     * @param c
     */
    public void addCorner(Corner c) {
        if (!accessibleCorners.contains(c)) {
            accessibleCorners.add(c);
        }
    }

    /** incrementArmy
     * This function increments the player's army size by 1. 
     */
    public void incrementArmy() {
        army++;
    }

    /** getResource
     * This function returns the amount of the specified resource that the player has
     * @param resource
     * @return int --> amount of the resource
     */
    public int getResource(Board.RESOURCE resource) {
        return resources[resource.ordinal()];
    }

    public int getTotalResources() {
        return totalResources;
    }

    public Color getColor() {
        return color;
    }

    public LinkedList<Corner> getAccessibleCorners() {
        return accessibleCorners;
    }
    
    public LinkedList<Board.DEVELOPMENT> getDevCards() {
        return devCards;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public int getArmy() {
        return army;
    }
}