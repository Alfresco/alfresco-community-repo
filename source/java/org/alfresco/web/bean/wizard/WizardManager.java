/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.config.WizardsConfigElement.ConditionalPageConfig;
import org.alfresco.web.config.WizardsConfigElement.PageConfig;
import org.alfresco.web.config.WizardsConfigElement.StepConfig;
import org.alfresco.web.config.WizardsConfigElement.WizardConfig;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean that manages the wizard framework
 * 
 * @author gavinc
 */
public final class WizardManager
{
   /** public JSF bean name */
   public final static String BEAN_NAME = "WizardManager";
   
   private static Log logger = LogFactory.getLog(WizardManager.class);
   
   private WizardState currentWizardState; 
   private Map<String, String> paramsToApply;
   
   /**
    * Sets the parameters the next wizard will initialise with
    * 
    * @param params The parameters
    */
   public void setupParameters(Map<String, String> params)
   {
      this.paramsToApply = params;
   }
   
   /**
    * Action handler used to setup parameters for the wizard being launched
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
         this.paramsToApply = ((UIActionLink)component).getParameterMap();
         
         // make sure "null" parameters are actually null, this can occur
         // when null parameters are sent to the client and posted back
         for (String name : this.paramsToApply.keySet())
         {
            String value = this.paramsToApply.get(name);
            if (value != null && value.equalsIgnoreCase("null"))
            {
               this.paramsToApply.put(name, null);
            }
         }
      }
   }
   
   /**
    * Sets the current wizard
    * 
    * @param config The configuration for the wizard to set
    */
   public void setCurrentWizard(WizardConfig config)
   {
      // make sure the managed bean is present
      String beanName = config.getManagedBean();
      
      Object bean = FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), beanName);

      if (bean == null)
      {
         throw new AlfrescoRuntimeException("Failed to start wizard as managed bean '" + beanName + 
               "' has not been defined");
      }
      
      // make sure the bean implements the IWizardBean interface
      IWizardBean wizard = null;
      if (bean instanceof IWizardBean)
      {
         wizard = (IWizardBean)bean;
      }
      else
      {
         throw new AlfrescoRuntimeException("Failed to start wizard as managed bean '" + beanName + 
               "' does not implement the required IWizardBean interface");
      }
      
      // create the WizardState object
      this.currentWizardState = new WizardState(config, wizard);
      
      // initialise the managed bean
      wizard.init(this.paramsToApply);
      
      // reset the current parameters so subsequent wizards don't get them
      this.paramsToApply = null;
      
      // setup the first step
      determineCurrentPage();
   }
   
   /**
    * Returns the state of the currently active wizard
    * 
    * @return Current wizard's state
    */
   public WizardState getState()
   {
      return this.currentWizardState;
   }
   
   /**
    * Restores the wizard represented by the given WizardState object.
    * NOTE: The wizard's restored() method is also called during this
    * method.
    * 
    * @param state The WizardState for the wizard to restore
    */
   public void restoreState(WizardState state)
   {
      this.currentWizardState = state;
      
      // retrieve the wizard and call it's restored() method
      this.currentWizardState.getWizard().restored();
   }
   
   /**
    * Returns the config for the current wizard
    * 
    * @return The current wizard config
    */
   public WizardConfig getCurrentWizard()
   {
      return this.currentWizardState.getConfig();
   }
   
   /**
    * Returns the current wizard bean being managed
    * 
    * @return The current managed bean
    */
   public IWizardBean getBean()
   {
      return this.currentWizardState.getWizard();
   }
   
   /**
    * Returns the icon to use for the current wizard
    * 
    * @return The icon
    */
   public String getIcon()
   {
      return this.currentWizardState.getConfig().getIcon();
   }
   
   /**
    * Returns the error message to use in error conditions
    * 
    * @return The error message
    */
   public String getErrorMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), 
            this.currentWizardState.getConfig().getErrorMessageId());
   }
   
   /**
    * Returns the resolved title to use for the wizard
    * 
    * @return The title
    */
   public String getTitle()
   {
      // try and get the title directly from the wizard
      String title = this.currentWizardState.getWizard().getContainerTitle();
      
      if (title == null)
      {
         // try and get the title via a message bundle key
         title = this.currentWizardState.getConfig().getTitleId();
         
         if (title != null)
         {
            title = Application.getMessage(FacesContext.getCurrentInstance(), title);
         }
         else
         {
            // try and get the title from the configuration
            title = this.currentWizardState.getConfig().getTitle();
         }
      }
      
      return title;
   }
   
   /**
    * Returns the resolved subtitle to use for the wizard
    * 
    * @return The subtitle
    */
   public String getSubTitle()
   {
      // try and get the title directly from the wizard
      String subTitle = this.currentWizardState.getWizard().getContainerSubTitle();
      
      if (subTitle == null)
      {
         // try and get the title via a message bundle key
         subTitle = this.currentWizardState.getConfig().getSubTitleId();
         
         if (subTitle != null)
         {
            subTitle = Application.getMessage(FacesContext.getCurrentInstance(), subTitle);
         }
         else
         {
            // try and get the title from the configuration
            subTitle = this.currentWizardState.getConfig().getSubTitle();
         }
      }
      
      return subTitle;
   }
   
   /**
    * Returns the resolved description to use for the wizard
    * 
    * @return The description
    */
   public String getDescription()
   {
      // try and get the description directly from the dialog
      String desc = this.currentWizardState.getWizard().getContainerDescription();
      if (desc == null)
      {
         // try and get the description via a message bundle key
         desc = this.currentWizardState.getConfig().getDescriptionId();
         
         if (desc != null)
         {
            desc = Application.getMessage(FacesContext.getCurrentInstance(), desc);
         }
         else
         {
            // try and get the description from the configuration
            desc = this.currentWizardState.getConfig().getDescription();
         }
      }
      
      return desc;
   }
   
   /**
    * Returns the current step position
    * 
    * @return Current step position
    */
   public int getCurrentStep()
   {
      return this.currentWizardState.getCurrentStep();
   }
   
   /**
    * Returns the current step position as a string for use in the UI
    * 
    * @return Current step position as a string
    */
   public String getCurrentStepAsString()
   {
      return Integer.toString(this.currentWizardState.getCurrentStep());
   }
   
   /**
    * Returns the name of the current step, wizards should use
    * the name of the step rather than the step number to discover
    * the position as extra steps can be added via configuration.
    * 
    * @return The name of the current step
    */
   public String getCurrentStepName()
   {
      return ((StepConfig)this.currentWizardState.getSteps().get(
            this.currentWizardState.getCurrentStep()-1)).getName();
   }

   /**
    * Returns a list of UIListItems representing the steps of the wizard
    * 
    * @return List of steps to display in UI
    */
   public List<UIListItem> getStepItems()
   {
      List<UIListItem> items = new ArrayList<UIListItem>(this.currentWizardState.getSteps().size());
      
      for (int x = 0; x < this.currentWizardState.getSteps().size(); x++)
      {
         String uiStepNumber = Integer.toString(x + 1);
         StepConfig stepCfg = this.currentWizardState.getSteps().get(x);
         UIListItem item = new UIListItem();
         item.setValue(uiStepNumber);
         
         // get the title for the step
         String stepTitle = stepCfg.getTitleId();
         if (stepTitle != null)
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), stepTitle);
         }
         else
         {
            stepTitle = stepCfg.getTitle();
         }
         
         // get the tooltip for the step
         String stepTooltip = stepCfg.getDescriptionId();
         if (stepTooltip != null)
         {
            stepTooltip = Application.getMessage(FacesContext.getCurrentInstance(), stepTooltip);
         }
         else
         {
            stepTooltip = stepCfg.getDescription();
         }
         
         // set the label and tooltip
         item.setLabel(uiStepNumber + ". " + stepTitle);
         item.setTooltip(stepTooltip);
         
         items.add(item);
      }
      
      return items;
   }
   
   /**
    * Returns the current page of the wizard (depends on the current step position)
    * 
    * @return The page
    */
   public String getPage()
   {
      return this.currentWizardState.getCurrentPageCfg().getPath();
   }
   
   /**
    * Returns the title of the current step
    * 
    * @return The step title
    */
   public String getStepTitle()
   {
      String result = this.currentWizardState.getWizard().getStepTitle();
      if (result == null)
      {
         result = this.currentWizardState.getCurrentPageCfg().getTitleId();
         result = (result != null
                   ? Application.getMessage(FacesContext.getCurrentInstance(), result)
                   : this.currentWizardState.getCurrentPageCfg().getTitle());
      }
      return result;
   }
   
   /**
    * Returns the description of the current step
    * 
    * @return The step description
    */
   public String getStepDescription()
   {
      String result = this.currentWizardState.getWizard().getStepDescription();
      if (result == null)
      {
         result = this.currentWizardState.getCurrentPageCfg().getDescriptionId();
         result = (result != null
                   ? Application.getMessage(FacesContext.getCurrentInstance(), result)
                   : this.currentWizardState.getCurrentPageCfg().getDescription());
      }
      return result;
   }
   
   /**
    * Returns the instructions for the current step
    * 
    * @return The step instructions
    */
   public String getStepInstructions()
   {
      String instruction = this.currentWizardState.getCurrentPageCfg().getInstructionId();
      
      if (instruction != null)
      {
         instruction = Application.getMessage(FacesContext.getCurrentInstance(), instruction);
      }
      else
      {
         instruction = this.currentWizardState.getCurrentPageCfg().getInstruction();
      }
      
      return instruction;
   }
   
   /**
    * Returns the label to use for the next button
    * 
    * @return The next button label
    */
   public String getNextButtonLabel()
   {
      return this.currentWizardState.getWizard().getNextButtonLabel();
   }
   
   /**
    * Determines whether the next button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getNextButtonDisabled()
   {
      return (this.currentWizardState.getCurrentStep() == this.currentWizardState.getSteps().size() ||
              this.currentWizardState.getWizard().getNextButtonDisabled());
   }
   
   /**
    * Returns the label to use for the back button
    * 
    * @return The back button label
    */
   public String getBackButtonLabel()
   {
      return this.currentWizardState.getWizard().getBackButtonLabel();
   }
   
   /**
    * Determines whether the back button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getBackButtonDisabled()
   {
      return this.currentWizardState.getCurrentStep() == 1;
   }
   
   /**
    * Returns the label to use for the cancel button
    * 
    * @return The cancel button label
    */
   public String getCancelButtonLabel()
   {
      return this.currentWizardState.getWizard().getCancelButtonLabel();
   }
   
   /**
    * Returns the label to use for the finish button
    * 
    * @return The finish button label
    */
   public String getFinishButtonLabel()
   {
      return this.currentWizardState.getWizard().getFinishButtonLabel();
   }
   
   /**
    * Determines whether the finish button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getFinishButtonDisabled()
   {
      return (this.currentWizardState.getCurrentStep() != this.currentWizardState.getSteps().size() &&
              this.currentWizardState.getWizard().getFinishButtonDisabled());
   }
   
   /**
    * Method handler called when the next button of the wizard is pressed
    */
   public void next()
   {
      // calculate next step number and update wizard state
      int currentStep = this.currentWizardState.getCurrentStep();
      int noSteps = this.currentWizardState.getSteps().size();
      currentStep++;
      
      // check we haven't gone past the end (this can happen if the next 
      // button is pressed too many times)
      if (currentStep > noSteps)
      {
         currentStep = noSteps;
      }
      
      this.currentWizardState.setCurrentStep(currentStep);
      
      if (logger.isDebugEnabled())
         logger.debug("next called, current step is now: " + this.currentWizardState.getCurrentStep());
      
      // tell the wizard the next button has been pressed
      this.currentWizardState.getWizard().next();
      
      determineCurrentPage();
   }
   
   /**
    * Method handler called when the back button of the wizard is pressed
    */
   public void back()
   {
      // calculate next step number and update wizard state
      int currentStep = this.currentWizardState.getCurrentStep();
      currentStep--;
      
      // check we haven't gone past the beginning (this can 
      // happen if the back button is pressed too many times)
      if (currentStep < 1)
      {
         currentStep = 1;
      }
      
      this.currentWizardState.setCurrentStep(currentStep);
      
      if (logger.isDebugEnabled())
         logger.debug("back called, current step is now: " + this.currentWizardState.getCurrentStep());
      
      // tell the wizard the back button has been pressed
      this.currentWizardState.getWizard().back();
      
      determineCurrentPage();
   }
   
   /**
    * Method handler called when the finish button of the wizard is pressed
    * 
    * @return The outcome
    */
   public String finish()
   {
      return this.currentWizardState.getWizard().finish();
   }
   
   /**
    * Method handler called when the cancel button of the wizard is pressed
    * 
    * @return The outcome
    */
   public String cancel()
   {
      return this.currentWizardState.getWizard().cancel();
   }
   
   /**
    * Sets up the current page to show in the wizard
    */
   protected void determineCurrentPage()
   {
      // reset the current page config in the state object
      this.currentWizardState.setCurrentPageCfg(null);
      
      PageConfig currentPageCfg = null;
      
      // get the config for the current step position
      StepConfig stepCfg = this.currentWizardState.getSteps().get(
            this.currentWizardState.getCurrentStep()-1);
      
      // is the step conditional?
      if (stepCfg.hasConditionalPages())
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // test each conditional page in turn
         List<ConditionalPageConfig> pages = stepCfg.getConditionalPages();
         
         for (ConditionalPageConfig pageCfg : pages)
         {
            String condition = pageCfg.getCondition();
            
            if (logger.isDebugEnabled())
               logger.debug("Evaluating condition: " + condition);
            
            ValueBinding vb = context.getApplication().createValueBinding(condition);
            Object obj = vb.getValue(context);
            if (obj instanceof Boolean && ((Boolean)obj).booleanValue())
            {
               currentPageCfg = pageCfg;
               break;
            }
         }
      }
      
      // if none of the conditions passed use the default page
      if (currentPageCfg == null)
      {
         currentPageCfg = stepCfg.getDefaultPage();
      }
      
      if (currentPageCfg == null)
      {
         throw new AlfrescoRuntimeException("Failed to determine page for step '" + stepCfg.getName() +
               "'. Make sure a default page is configured.");
      }
      
      // save the current page config in the state object
      this.currentWizardState.setCurrentPageCfg(currentPageCfg);
      
      if (logger.isDebugEnabled())
         logger.debug("Config for current page: " + this.currentWizardState.getCurrentPageCfg());
   }
}
