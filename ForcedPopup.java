package Catan; 

import javax.swing.*;

/** ForcedPopup
 * The ForcedPopup class is used to create JPopupMenus that the player is forced to interact with. 
 * The popup will not close until the closePopup function is called (which will typically only be when a menu item is selected). 
 */
public class ForcedPopup extends JPopupMenu {
    private boolean hide = false;

    public ForcedPopup() {
        super();
    }

    @Override
    /** setVisible
     * This function has been overridden to check for a boolean condition before calling the super function. 
     * This is done so that the popup menu cannot automatically hide itself, and the closePopup function must be called instead. 
     */
    public void setVisible(boolean visibility){
        if(hide && !visibility)
            super.setVisible(false);
        else if(!hide && visibility)
            super.setVisible(true);
    }

    /** closePopup
     * This function allows the popup to be closed (and does not allow it to be shown),
     * and then calls the setVisible function to close it. 
     */
    public void closePopup() {
        hide = true;
        setVisible(false);
    }

    /** allowShowPopup
     * This function allows the popup to be shown (and does not allow it to be hidden). 
     * (This function does not show the popup, since that is handled by the JPopupMenu.show(Component, int, int) function)
     */
    public void allowShowPopup() {
        hide = false;
    }
}