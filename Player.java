package Catan;

import java.awt.*;
import java.util.*;

public class Player {

    private LinkedList<Corner> accessibleCorners = new LinkedList<Corner>();
    private int[] devCards = new int[Board.DEVELOPMENT.values().length];
    private int[] resources = new int[Board.RESOURCE.values().length];
    private int settlements = 4, cities = 4;
    private boolean developed = false;
    private Color color;

    public Player(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void buildRoad(Board board) {
        // if (resources[Board.RESOURCE.TIMBER.ordinal()] == 0 || resources[Board.RESOURCE.BRICK.ordinal()] == 0)
        //     return;
        // resources[Board.RESOURCE.TIMBER.ordinal()]--;
        // resources[Board.RESOURCE.BRICK.ordinal()]--;
        
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

        Thread buildSettlement = new Thread() {
            @Override
            public void run() {
                //Show buildable corners
                boolean spaceAvailable = false;
                for (Corner c : accessibleCorners) {
                    if (c == null)
                        continue;
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
            }
                
        };
        buildSettlement.start();
    }

    public void buildCity(Board board) {
        if (resources[Board.RESOURCE.ORE.ordinal()] < 3)
            return;
        if (resources[Board.RESOURCE.WHEAT.ordinal()] < 2)
            return;
        resources[Board.RESOURCE.ORE.ordinal()] -= 3;
        resources[Board.RESOURCE.WHEAT.ordinal()] -= 2;

        Thread buildCity = new Thread() {
            @Override
            public void run() {
                //Show buildable corners
                boolean spaceAvailable = false;
                for (Corner c : accessibleCorners) {
                    if (c == null)
                        continue;
                    if (c.isEnabled() && c.getOwner() == Player.this && c.getStructure() == Corner.STRUCTURE.SETTLEMENT) {
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
            }
                
        };
        buildCity.start();
    }

    public void trade(Player player, int[] give, int [] receive) {
        //trading offer

    }

    public void addResource(Board.RESOURCE resource, int amount) {
        resources[resource.ordinal()] += amount;
    }

    public void addCorner(Corner c) {
        if (!accessibleCorners.contains(c)) {
            accessibleCorners.add(c);
        }
    }

    public LinkedList<Corner> getAccessibleCorners() {
        return accessibleCorners;
    }
}