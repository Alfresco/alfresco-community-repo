package org.alfresco.web.bean.dialog;

import java.util.List;
import java.util.Map;

import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;

/**
 * Interface that defines the contract for a dialog backing bean
 * 
 * @author gavinc
 */
public interface IDialogBean
{
   /**
    * Initialises the dialog bean
    * 
    * @param parameters Map of parameters for the dialog
    */
   public void init(Map<String, String> parameters);
   
   /**
    * Called when the dialog is restored after a nested dialog is closed
    */
   public void restored();
   
   /**
    * Method handler called when the cancel button of the dialog is pressed
    * 
    * @return The outcome to return
    */
   public String cancel();
   
   /**
    * Method handler called when the finish button of the dialog is pressed
    * 
    * @return The outcome to return
    */
   public String finish();
   
   /**
    * Returns a list of additional buttons to display in the dialog.
    * 
    * @return List of button configurations, null if there are no buttons
    */
   public List<DialogButtonConfig> getAdditionalButtons();
   
   /**
    * Returns the label to use for the cancel button
    * 
    * @return The cancel button label
    */
   public String getCancelButtonLabel();
   
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
   
   /**
    * Returns the title to be used for the dialog
    * <p>If this returns null the DialogManager will
    * lookup the title via the dialog configuration</p>
    * 
    * @return The title or null if the title is to be acquired via configuration
    */
   public String getContainerTitle();
   
   /**
    * Returns the description to be used for the dialog
    * <p>If this returns null the DialogManager will
    * lookup the description via the dialog configuration</p>
    * 
    * @return The title or null if the title is to be acquired via configuration
    */
   public String getContainerDescription();
}
