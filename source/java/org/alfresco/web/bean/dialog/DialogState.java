package org.alfresco.web.bean.dialog;

import java.io.Serializable;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;

/**
 * Object responsible for holding the current state of an active dialog.
 * 
 * @author gavinc
 */
public final class DialogState implements Serializable
{
   private static final long serialVersionUID = -5007635589636930602L;
   
   private DialogConfig config;
   private IDialogBean dialog;
   
   /**
    * Default constructor
    * 
    * @param config The configuration for the dialog
    * @param dialog The dialog bean instance
    */
   public DialogState(DialogConfig config, IDialogBean dialog)
   {
      this.config = config;
      this.dialog = dialog;
   }
   
   /**
    * Returns the configuration for the dialog
    * 
    * @return The dialog configuration
    */
   public DialogConfig getConfig()
   {
      return config;
   }
   
   /**
    * Returns the bean representing the dialog instance
    * 
    * @return The dialog bean instance
    */
   public IDialogBean getDialog()
   {
      return dialog;
   }

   @Override
   public String toString()
   {
      return AlfrescoNavigationHandler.DIALOG_PREFIX + this.config.getName();
   }
}
