package Catan;

import java.awt.*;
import java.util.*;
import java.util.Queue;

import Catan.Board.DEVELOPMENT;

public class Player {

    private LinkedList<Corner> accessibleCorners = new LinkedList<Corner>();
    private LinkedList<Board.DEVELOPMENT> devCards = new LinkedList<Board.DEVELOPMENT>();
    private int[] resources = new int[Board.RESOURCE.values().length-1];
    private int settlements = 6, cities = 4, victoryPoints = 0, totalResources = 0, army = 0;
    private Color color;
    public boolean developed = false;

    public Player(Color color) {
        this.color = color;
    }

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

    public int getLongestRoad(Board board) {
        //Longest path of acyclic graph can be found like a tree diameter using DFS
        HashMap<Corner, Integer> dist;
        Corner diameterEnd;

        //Find one end of diameter
        dist = new HashMap<Corner, Integer>();
        dist.put(accessibleCorners.get(0), 0);
        distFarthestCorner(dist, board, accessibleCorners.get(0));

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



        if (dist.containsKey(accessibleCorners.get(2))) //The two starting settlements are connected
            return farthest;


            
        //If the starting settlements are not connected, you have to compute the diameter of the second startling settlement's graph too
        //Find one end of diameter
        dist = new HashMap<Corner, Integer>();
        dist.put(accessibleCorners.get(2), 0);
        distFarthestCorner(dist, board, accessibleCorners.get(2));

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

        return Integer.max(farthest, farthest2nd);
    }

    public boolean canBuildRoad(Board board) {
        //Check resources
        if (resources[Board.RESOURCE.TIMBER.ordinal()] == 0) 
            return false;
        if (resources[Board.RESOURCE.BRICK.ordinal()] == 0)
            return false;
        
        //Check space to build
        for (Corner c : accessibleCorners)
            if (board.getAdjacentRoads(c).size() != 0)
                return true;
        return false;
    }

    public void buildRoad(Board board) {
        addResource(Board.RESOURCE.TIMBER, -1);
        addResource(Board.RESOURCE.BRICK, -1);
        Board.RESOURCE.TIMBER.updateDisplay(board);
        Board.RESOURCE.BRICK.updateDisplay(board);
        
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

                Catan.semaphore.release();
            }
                
        };
        buildRoad.start();
    }

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

    public void buildSettlement(Board board) {
        addResource(Board.RESOURCE.TIMBER, -1);
        addResource(Board.RESOURCE.BRICK, -1);
        addResource(Board.RESOURCE.SHEEP, -1);
        addResource(Board.RESOURCE.WHEAT, -1);
        Board.RESOURCE.TIMBER.updateDisplay(board);
        Board.RESOURCE.BRICK.updateDisplay(board);
        Board.RESOURCE.SHEEP.updateDisplay(board);
        Board.RESOURCE.WHEAT.updateDisplay(board);

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

    public void buildCity(Board board) {
        addResource(Board.RESOURCE.ORE, -3);
        addResource(Board.RESOURCE.WHEAT, -2);
        Board.RESOURCE.ORE.updateDisplay(board);
        Board.RESOURCE.WHEAT.updateDisplay(board);

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

    public boolean canBuyDevCard() {
        if (resources[Board.RESOURCE.ORE.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.SHEEP.ordinal()] == 0)
            return false;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] == 0)
            return false;
        return true;
    }

    public void buyDevelopmentCard(Board board) {
        addResource(Board.RESOURCE.ORE, -1);
        addResource(Board.RESOURCE.SHEEP, -1);
        addResource(Board.RESOURCE.WHEAT, -1);
        Board.RESOURCE.ORE.updateDisplay(board);
        Board.RESOURCE.SHEEP.updateDisplay(board);
        Board.RESOURCE.WHEAT.updateDisplay(board);

        DEVELOPMENT devCard = board.getDevCard();
        devCards.add(devCard);

        //If the card is a victory point
        if (devCard == DEVELOPMENT.VICTORY_POINT) {
            victoryPoints++;
            board.updatePlayerDisplay();
        }

        //Update buttons from buying cost
        board.setButtonsEnabled(true);
        board.rollDiceButton.setEnabled(false);
    }

    public void trade(Player player, int[] give, int[] receive) {
        for (int i = 0; i < Board.RESOURCE.values().length-1; i++) {
            resources[i] -= give[i];
            player.resources[i] += give[i];
            resources[i] += receive[i];
            player.resources[i] -= receive[i];
            
            totalResources -= give[i];
            totalResources += receive[i];
        }
    }

    public void addResource(Board.RESOURCE resource, int amount) {
        if (resource == Board.RESOURCE.NONE)
            return;
        resources[resource.ordinal()] += amount;
        totalResources += amount;
    }

    public int getResource(Board.RESOURCE resource) {
        return resources[resource.ordinal()];
    }

    public int getTotalResources() {
        return totalResources;
    }

    public void addCorner(Corner c) {
        if (!accessibleCorners.contains(c)) {
            accessibleCorners.add(c);
        }
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

    public void incrementArmy() {
        army++;
    }

    public int getArmy() {
        return army;
    }
}