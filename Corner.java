package Catan;

import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class Corner extends JButton {

    public enum STRUCTURE {
        SETTLEMENT(1),
        CITY(2);

        public final int generateAmount;
        private STRUCTURE(int generateAmount) {
            this.generateAmount = generateAmount;
        }
    }

    private Player owner;
    private STRUCTURE structure;
    private int row, column;
    public static final int RADIUS = 10;

    private static ImageIcon offerBuild;
    static {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("Catan/Icons/TempCornerBuild.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image dimg = img.getScaledInstance(Corner.RADIUS*2, Corner.RADIUS*2, Image.SCALE_DEFAULT);
        offerBuild = new ImageIcon(dimg);
    }
    

    public Corner(int row, int column) {
        setOpaque(true);
        setIcon(offerBuild);//testing icon
        validate();
    }


    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public void setStructure(STRUCTURE structure) {
        this.structure = structure;
    }

    public Player getOwner() {
        return owner;
    }

    public STRUCTURE getStructure() {
        return structure;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}