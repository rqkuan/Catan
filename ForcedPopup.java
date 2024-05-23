package Catan; 

import javax.swing.*;

public class ForcedPopup extends JPopupMenu {
    private boolean hide = false;

    public ForcedPopup() {
        super();
    }

    @Override
    public void setVisible(boolean visibility){
        if(hide && !visibility)
            super.setVisible(false);
        else if(!hide && visibility)
            super.setVisible(true);
    }

    public void closePopup() {
        hide = true;
        setVisible(false);
    }

    public void allowShowPopup() {
        hide = false;
    }
}