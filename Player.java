package Catan;

import java.awt.*;
import java.util.*;

public class Player {

    private LinkedList<Corner> accessibleCorners = new LinkedList<Corner>();
    private int[] devCards = new int[Board.DEVELOPMENT.values().length];
    protected int[] resources = new int[Board.RESOURCE.values().length-1];
    private int settlements = 4, cities = 4, victoryPoints = -2;
    private boolean developed = false;
    private Color color;

    public Player(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void buildRoad(Board board) {
        if (resources[Board.RESOURCE.TIMBER.ordinal()] == 0 || resources[Board.RESOURCE.BRICK.ordinal()] == 0)
            return;
        resources[Board.RESOURCE.TIMBER.ordinal()]--;
        resources[Board.RESOURCE.BRICK.ordinal()]--;
        board.updateResourceAmount(board.timberAmount, Board.RESOURCE.TIMBER);
        board.updateResourceAmount(board.brickAmount, Board.RESOURCE.BRICK);
        
        Thread buildRoad = new Thread() {
            @Override
            public void run() {
                //Show buildable roads
                boolean spaceAvailable = false;
                for (Corner c : accessibleCorners) {
                    if (c.getOwner() == null || c.getOwner() == Player.this) {
                        board.offerBuildRoads(c);
                        spaceAvailable = true;
                    }
                }
                if (!spaceAvailable)
                    return;
                
                try {
                    Catan.semaphore.acquire();
                } catch (InterruptedException excpt) {}

                //Hide (and disable) road buttons;
                board.hideRoads();
            }
                
        };
        buildRoad.start();
    }

    public void buildSettlement(Board board) {
        if (settlements == 0)
            return;

        if (resources[Board.RESOURCE.TIMBER.ordinal()] == 0)
            return;
        if (resources[Board.RESOURCE.BRICK.ordinal()] == 0)
            return;
        if (resources[Board.RESOURCE.SHEEP.ordinal()] == 0)
            return;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] == 0)
            return;
        resources[Board.RESOURCE.TIMBER.ordinal()]--;
        resources[Board.RESOURCE.BRICK.ordinal()]--;
        resources[Board.RESOURCE.SHEEP.ordinal()]--;
        resources[Board.RESOURCE.WHEAT.ordinal()]--;
        board.updateResourceAmount(board.timberAmount, Board.RESOURCE.TIMBER);
        board.updateResourceAmount(board.brickAmount, Board.RESOURCE.BRICK);
        board.updateResourceAmount(board.sheepAmount, Board.RESOURCE.SHEEP);
        board.updateResourceAmount(board.wheatAmount, Board.RESOURCE.WHEAT);

        Thread buildSettlement = new Thread() {
            @Override
            public void run() {
                //Show buildable corners
                boolean spaceAvailable = false;
                for (Corner c : accessibleCorners) {
                    if (c.isEnabled() && c.getOwner() == null) {
                        c.buildable = true;
                        c.setVisible(true);
                        spaceAvailable = true;
                    }   
                }
                if (!spaceAvailable) 
                    return;

                try {
                    Catan.semaphore.acquire();
                } catch (InterruptedException excpt) {}

                //Hide (and disable) corner buttons;
                board.hideCorners();

                //Victory points and building count
                victoryPoints++;
                settlements--;
            }
                
        };
        buildSettlement.start();
    }

    public void buildCity(Board board) {
        if (cities == 0)
            return;

        if (resources[Board.RESOURCE.ORE.ordinal()] < 3)
            return;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] < 2)
            return;
        resources[Board.RESOURCE.ORE.ordinal()] -= 3;
        resources[Board.RESOURCE.WHEAT.ordinal()] -= 2;
        board.updateResourceAmount(board.oreAmount, Board.RESOURCE.ORE);
        board.updateResourceAmount(board.wheatAmount, Board.RESOURCE.WHEAT);

        Thread buildCity = new Thread() {
            @Override
            public void run() {
                //Show buildable corners
                boolean spaceAvailable = false;
                for (Corner c : accessibleCorners) {
                    if (c.isEnabled() && c.getOwner() == Player.this && c.getStructure() == Corner.STRUCTURE.SETTLEMENT) {
                        c.buildable = true;
                        c.setIcon(Corner.STRUCTURE.NONE.icon);
                        spaceAvailable = true;
                    }   
                }
                if (!spaceAvailable) 
                    return;

                try {
                    Catan.semaphore.acquire();
                } catch (InterruptedException excpt) {}

                //Hide (and disable) corner buttons;
                board.hideCorners();

                //Victory points and building count
                victoryPoints++;
                settlements++;
                cities--;
            }
                
        };
        buildCity.start();
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
    }

    public int getResource(Board.RESOURCE resource) {
        return resources[resource.ordinal()];
    }

    public void addCorner(Corner c) {
        if (!accessibleCorners.contains(c)) {
            accessibleCorners.add(c);
        }
    }

    public LinkedList<Corner> getAccessibleCorners() {
        return accessibleCorners;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }
}