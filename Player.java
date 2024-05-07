package Catan;

import java.util.*;

public class Player {

    private LinkedList<Corner> accessibleCorners = new LinkedList<Corner>();
    private HashMap<Corner, LinkedList<Corner>> adj = new HashMap<Corner, LinkedList<Corner>>();
    private int[] devCards = new int[Board.DEVELOPMENT.values().length];
    private int[] resources = new int[Board.RESOURCE.values().length];
    private int settlements = 4, cities = 4;
    private boolean developed = false;

    public Player() {

    }

    public LinkedList<Road> buildRoad(Board board) {
        LinkedList<Road> buildable = new LinkedList<Road>();
        
        //Find the roads locations that the player can build at


        return buildable;
    }

    public LinkedList<Corner> buildSettlement(Board board) {
        LinkedList<Corner> buildable = new LinkedList<Corner>();
        
        //Find the corners that the player can build a settlement at


        return buildable;
    }

    public LinkedList<Corner> buildCity(Board board) {
        LinkedList<Corner> buildable = new LinkedList<Corner>();
        
        //Find the corners that the player can build a city at


        return buildable;
    }

    public void trade(Player player, int[] give, int [] receive) {
        //trading offer

    }

    public void connectCorner(Corner c1, Corner c2) {
        
    }

}