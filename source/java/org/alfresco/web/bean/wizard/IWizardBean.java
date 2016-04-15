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
    * Called when the next button is pressed by the user
    * 
    * @return Reserved for future use
    */
   public String next();
   
   /**
    * Called when the back button is pressed by the user
    * 
    * @return Reserved for future use
    */
   public String back();
   
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
   
   /**
    * Determines whether the next button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getNextButtonDisabled();
   
   /**
    * Returns the title to be used for the current step
    * <p>If this returns <tt>null</tt> the WizardManager will
    * lookup the title via the dialog configuration</p>
    * 
    * @return The title or <tt>null</tt> if the title is to be acquired via configuration
    */
   public String getStepTitle();
   
   /**
    * Returns the description to be used for the current step
    * <p>If this returns <tt>null</tt> the WizardManager will
    * lookup the description via the dialog configuration</p>
    * 
    * @return The decsription or <tt>null</tt> if the title is to be acquired via configuration
    */
   public String getStepDescription();
}
