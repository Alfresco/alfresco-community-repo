package org.alfresco.web.bean.wizard;

import org.alfresco.web.bean.dialog.IDialogBean;

/**
 * Interface that defines the contract for a wizard backing bean
 * 
 * @author gavinc
 */
public interface IWizardBean extends IDialogBean
{
   /**
    * Method handler called when the next button of the wizard is pressed
    * 
    * @return The outcome to return
    */
//   public String next();
   
   /**
    * Method handler called when the back button of the wizard is pressed
    * 
    * @return The outcome to return
    */
//   public String back();
      
   /**
    * Determines whether the next button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getNextButtonDisabled();
   
   /**
    * Determines whether the back button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getBackButtonDisabled();
   
   /**
    * Returns the label to use for the next button
    * 
    * @return The next button label
    */
   public String getNextButtonLabel();
   
   /**
    * Returns the label to use for the back button
    * 
    * @return The back button label
    */
   public String getBackButtonLabel();
}
