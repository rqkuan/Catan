package Catan;

import java.io.*;
import java.util.concurrent.Semaphore;
import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/** Catan
 * The Catan class acts as the main class and handles game setup. 
 * It also contains multiple simple helper functions, and helps manage asynchronous executions. 
 */
public class Catan {

    //Semaphores for pausing threads to help with asynchronous operations
    public static Semaphore semaphore = new Semaphore(0), playAgainSemaphore = new Semaphore(0);

    /** waitForButton
     * This function waits for the main semaphore to receive a new permit. 
     * (Waits for a button press before allowing the thread it's called from to continue running)
     */
    public static void waitForButton() {
        try {
            semaphore.drainPermits();
            semaphore.acquire();
        } catch (InterruptedException excpt) {
            excpt.printStackTrace();
            semaphore.release();
        }
    }

    /** getResizedIcon
     * This function returns a resized version of a image file. 
     * It is used for resizing sprites. 
     * @param width
     * @param height
     * @param path --> The filepath of the image
     * @return ImageIcon --> The resized image as an ImageIcon object
     */
    public static ImageIcon getResizedIcon(int width, int height, String path) {
        //Getting the image
        BufferedImage bimg = null;
        try {
            bimg = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Resizing and returning
        return new ImageIcon(bimg.getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    /** changeIconColor
     * This function returns a version of a given ImageIcon with the image recoloured to the specified colour. 
     * @param icon
     * @param color
     * @return ImageIcon --> The recoloured ImageIcon
     */
    public static ImageIcon changeIconColor(ImageIcon icon, Color color) {
        //Getting the image from the icon object
        Image img = icon.getImage();

        //"Converting" the image to a BufferedImage icon for editing purposes
        //(You can't actually convert it, so a new BufferedImage is made, and the pixels are simply copied over)
        BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimg.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        //Editing each pixel of the BufferedImage
        for (int x = 0; x < bimg.getWidth(); x++) {
            for (int y = 0; y < bimg.getHeight(); y++) {
                //Get the alpha value of the pixel to keep transparency consistent
                int alpha = new Color(bimg.getRGB(x, y), true).getAlpha();
                
                //Create a new Color object with the RGB values of the specified recolour, 
                //but with the same transparency as the original image
                Color colorShift = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                
                //Recolouring
                bimg.setRGB(x, y, colorShift.getRGB());
            }
        }

        return new ImageIcon(bimg);
    }

    public static void main(String[] args) {
        //Loop in case the player wants to play again
        while (true) {
            //Initializing a new board
            int devCards[] = {14, 5, 2, 2, 2};
            Board b = new Board(7, devCards, 10);
            b.players.add(new Player(new Color(255, 0, 0)));
            b.players.add(new Player(new Color(0, 255, 0)));
            b.players.add(new Player(new Color(0, 0, 255)));
            
        //Make players place their starting settlements before playing
            b.setButtonsEnabled(false);

            //Go through in player-order to place first starting settlements
            for (; b.curPlayerIndex < b.players.size(); b.curPlayerIndex++) {
                b.updatePlayerDisplay();
                b.offerStartingBuild();
            }

            //Go through in reverse-player-order to place second starting settlements
            for (b.curPlayerIndex--; b.curPlayerIndex >= 0; b.curPlayerIndex--) {
                b.updatePlayerDisplay();
                b.offerStartingBuild();
                int row = b.recentBuild.getRow();
                int column = b.recentBuild.getColumn();
                b.getCurPlayer().addCorner(b.recentBuild);

                //The second starting settlement also gives the player some initial resources based on the tiles adjacent to it
                if (column % 2 == 0) { 
                    //The corner has two tiles under it and one above

                    //Tile above
                    try {
                        Tile t = b.getTile(row-1, column/2 - row%2);
                        if (t != null) 
                            b.getCurPlayer().addResource(t.getResource(), 1);
                    } catch (IndexOutOfBoundsException excpt) {}

                    //Tiles under
                    try {
                        Tile t = b.getTile(row, column/2);
                        if (t != null) 
                            b.getCurPlayer().addResource(t.getResource(), 1);
                    } catch (IndexOutOfBoundsException excpt) {}
                    try {
                        Tile t = b.getTile(row, column/2 - 1);
                        if (t != null)
                            b.getCurPlayer().addResource(t.getResource(), 1);
                    } catch (IndexOutOfBoundsException excpt) {}
                } else {
                    //The corner has two tiles above it and one under it

                    //Tile under
                    try {
                        Tile t = b.getTile(row, column/2);
                        if (t != null) 
                            b.getCurPlayer().addResource(t.getResource(), 1);
                    } catch (IndexOutOfBoundsException excpt) {}

                    //Tiles above
                    try {
                        Tile t = b.getTile(row-1, column/2 - row%2);
                        if (t != null) 
                            b.getCurPlayer().addResource(t.getResource(), 1);
                    } catch (IndexOutOfBoundsException excpt) {}
                    try {
                        Tile t = b.getTile(row-1, column/2 + 1 - row%2);
                        if (t != null) 
                            b.getCurPlayer().addResource(t.getResource(), 1);
                    } catch (IndexOutOfBoundsException excpt) {}
                }
            }

            //Once starting settlements are done being placed, 
            //set the current player to Player 1 and enable the buttons to start the game
            b.curPlayerIndex = -1;
            b.nextPlayer();
            b.rollDiceButton.setEnabled(true);
            
            //This semaphore specifically waits for the "Play Again" button to be pressed, after which the program will loop. 
            try {
                playAgainSemaphore.acquire();
            } catch (InterruptedException excpt) {
                excpt.printStackTrace();
                break;
            }
            
        }
    }

}