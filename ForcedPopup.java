package Catan; 

import javax.swing.*;

public class ForcedPopup extends JPopupMenu {
    private boolean isHideAllowed = false;

    public ForcedPopup(){
        super();
    }

    @Override
    public void setVisible(boolean visibility){
        if(isHideAllowed && !visibility)
            super.setVisible(false);
        else if(!isHideAllowed && visibility)
            super.setVisible(true);
    }

    public void closePopup(){
        this.isHideAllowed = true;
        this.setVisible(false);
    }
}