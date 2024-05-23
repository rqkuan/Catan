package Catan;

import java.awt.*;
import java.util.*;

import Catan.Board.DEVELOPMENT;

public class Player {

    private LinkedList<Corner> accessibleCorners = new LinkedList<Corner>();
    private LinkedList<Board.DEVELOPMENT> devCards = new LinkedList<Board.DEVELOPMENT>();
    private int[] resources = new int[Board.RESOURCE.values().length-1];
    private int settlements = 4, cities = 4, victoryPoints = 0, totalResources = 0;
    private boolean developed = false;
    private Color color;

    public Player(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
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
        resources[Board.RESOURCE.TIMBER.ordinal()]--;
        resources[Board.RESOURCE.BRICK.ordinal()]--;
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
        resources[Board.RESOURCE.TIMBER.ordinal()]--;
        resources[Board.RESOURCE.BRICK.ordinal()]--;
        resources[Board.RESOURCE.SHEEP.ordinal()]--;
        resources[Board.RESOURCE.WHEAT.ordinal()]--;
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
        resources[Board.RESOURCE.ORE.ordinal()] -= 3;
        resources[Board.RESOURCE.WHEAT.ordinal()] -= 2;
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
        resources[Board.RESOURCE.ORE.ordinal()]--;
        resources[Board.RESOURCE.SHEEP.ordinal()]--;
        resources[Board.RESOURCE.WHEAT.ordinal()]--;
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
            this.resources[i] -= give[i];
            player.resources[i] += give[i];
            this.resources[i] += receive[i];
            player.resources[i] -= receive[i];
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

    public LinkedList<Corner> getAccessibleCorners() {
        return accessibleCorners;
    }
    
    public LinkedList<Board.DEVELOPMENT> getDevCards() {
        return devCards;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }
}