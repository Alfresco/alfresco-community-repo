package org.alfresco.web.bean.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Bean that manages the dialog framework
 * 
 * @author gavinc
 */
public class DialogManager
{
   protected IDialogBean currentDialog;
   protected DialogConfig currentDialogConfig;
   protected Map<String, String> currentDialogParams;
   
   /**
    * Action handler used to setup parameters for the dialog being launched
    * 
    * @param event The event containing the parameters
    */
   public void setupParameters(ActionEvent event)
   {
      // check the component the event come from was an action link
      UIComponent component = event.getComponent();
      if (component instanceof UIActionLink)
      {
         // store the parameters
         this.currentDialogParams = ((UIActionLink)component).getParameterMap();
      }
   }
   
   /**
    * Sets the current dialog
    * 
    * @param config The configuration for the dialog to set
    */
   public void setCurrentDialog(DialogConfig config)
   {
      this.currentDialogConfig = config;
      
      String beanName = this.currentDialogConfig.getManagedBean();
      this.currentDialog = (IDialogBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), beanName);
      
      if (this.currentDialog == null)
      {
         throw new AlfrescoRuntimeException("Failed to find managed bean '" + beanName + "'");
      }
      
      // initialise the managed bean
      this.currentDialog.init(this.currentDialogParams);
      
      // reset the current parameters so subsequent dialogs don't get them
      this.currentDialogParams = null;
   }
   
   /**
    * Returns the config for the current dialog
    * 
    * @return The current dialog config
    */
   public DialogConfig getCurrentDialog()
   {
      return this.currentDialogConfig;
   }
   
   /**
    * Returns the current dialog bean being managed
    * 
    * @return The current managed bean
    */
   public IDialogBean getBean()
   {
      return this.currentDialog;
   }
   
   /**
    * Returns the icon to use for the current dialog
    * 
    * @return The icon
    */
   public String getIcon()
   {
      return this.currentDialogConfig.getIcon();
   }
   
   /**
    * Returns the error message to use in error conditions
    * 
    * @return The error message
    */
   public String getErrorMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), 
            this.currentDialogConfig.getErrorMessageId());
   }
   
   /**
    * Returns the resolved title to use for the dialog
    * 
    * @return The title
    */
   public String getTitle()
   {
      String title = this.currentDialogConfig.getTitleId();
      
      if (title != null)
      {
         title = Application.getMessage(FacesContext.getCurrentInstance(), title);
      }
      else
      {
         title = this.currentDialogConfig.getTitle();
      }
      
      return title;
   }
   
   /**
    * Returns the resolved description to use for the dialog
    * 
    * @return The description
    */
   public String getDescription()
   {
      String desc = this.currentDialogConfig.getDescriptionId();
      
      if (desc != null)
      {
         desc = Application.getMessage(FacesContext.getCurrentInstance(), desc);
      }
      else
      {
         desc = this.currentDialogConfig.getDescription();
      }
      
      return desc;
   }
   
   /**
    * Returns the id of a configured action group representing the actions to
    * display for the dialog.
    * 
    * @return The action group id
    */
   public String getActions()
   {
      return this.currentDialogConfig.getActionsConfigId();
   }
   
   /**
    * Returns the page the dialog will use
    * 
    * @return The page
    */
   public String getPage()
   {
      return this.currentDialogConfig.getPage();
   }
   
   /**
    * Determines whether the current dialog's OK button is visible
    * 
    * @return true if the OK button is visible, false if it's not
    */
   public boolean isOKButtonVisible()
   {
      return this.currentDialogConfig.isOKButtonVisible();
   }
   
   /**
    * Returns a list of additional buttons to display in the dialog
    * 
    * @return List of button configurations
    */
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      List<DialogButtonConfig> buttons = null;
      
      // get a list of buttons to display from the configuration
      List<DialogButtonConfig> cfgButtons = this.currentDialogConfig.getButtons();
      
      // get a list of buttons added dynamically by the dialog
      List<DialogButtonConfig> dynButtons = this.currentDialog.getAdditionalButtons();

      if (cfgButtons != null && dynButtons != null)
      {
         // combine the two lists
         buttons = new ArrayList<DialogButtonConfig>(
               cfgButtons.size() + dynButtons.size());
         buttons.addAll(cfgButtons);
         buttons.addAll(dynButtons);
      }
      else if (cfgButtons != null && dynButtons == null)
      {
         buttons = cfgButtons;
      }
      else if (cfgButtons == null && dynButtons != null)
      {
         buttons = dynButtons;
      }
      
      return buttons;
   }
   
   /**
    * Returns the label to use for the cancel button
    * 
    * @return The cancel button label
    */
   public String getCancelButtonLabel()
   {
      return this.currentDialog.getCancelButtonLabel();
   }
   
   /**
    * Returns the label to use for the finish button
    * 
    * @return The finish button label
    */
   public String getFinishButtonLabel()
   {
      return this.currentDialog.getFinishButtonLabel();
   }
   
   /**
    * Determines whether the finish button on the dialog should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getFinishButtonDisabled()
   {
      return this.currentDialog.getFinishButtonDisabled();
   }
   
   /**
    * Method handler called when the finish button of the dialog is pressed
    * 
    * @return The outcome
    */
   public String finish()
   {
      return this.currentDialog.finish();
   }
   
   /**
    * Method handler called when the cancel button of the dialog is pressed
    * 
    * @return The outcome
    */
   public String cancel()
   {
      return this.currentDialog.cancel();
   }
}
