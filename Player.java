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
        //Find the roads locations that the player can build at

    }

    public void buildSettlement(Board board) {
        //Find the corners that the player can build a settlement at


    }

    public void buildCity(Board board) {
        //Find the corners that the player can build a city at


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