package org.alfresco.web.bean.dialog;

/**
 * Interface that defines the contract for a dialog backing bean
 * 
 * @author gavinc
 */
public interface IDialogBean
{
   /**
    * Initialises the dialog bean
    */
   public void init();
   
   /**
    * Method handler called when the finish button of the dialog is pressed
    * 
    * @return The outcome to return (normally dialog:close)
    */
   public String finish();
   
   /**
    * Method handler called when the cancel button of the dialog is pressed
    * 
    * @return The outcome to return (normally dialog:close)
    */
   public String cancel();
   
   /**
    * Returns the label to use for the finish button
    * 
    * @return The finish button label
    */
   public String getFinishButtonLabel();
   
   /**
    * Determines whether the finish button on the dialog should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getFinishButtonDisabled();
}
