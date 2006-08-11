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
public class WizardManager
{
   private static Log logger = LogFactory.getLog(WizardManager.class);
   
   protected int currentStep = 1;
   protected PageConfig currentPageCfg;
   protected WizardConfig currentWizardConfig;
   protected IWizardBean currentWizard;
   protected List<StepConfig> steps;
   protected Map<String, String> currentWizardParams;
   
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
         this.currentWizardParams = ((UIActionLink)component).getParameterMap();
      }
   }
   
   /**
    * Sets the current wizard
    * 
    * @param config The configuration for the wizard to set
    */
   public void setCurrentWizard(WizardConfig config)
   {
      this.currentStep = 1;
      this.currentWizardConfig = config;
      
      String beanName = this.currentWizardConfig.getManagedBean();
      this.currentWizard = (IWizardBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), beanName);
      
      if (this.currentWizard == null)
      {
         throw new AlfrescoRuntimeException("Failed to find managed bean '" + beanName + "'");
      }
      
      // initialise the managed bean
      this.currentWizard.init(this.currentWizardParams);
      
      // reset the current parameters so subsequent wizards don't get them
      this.currentWizardParams = null;
      
      // get the steps for the wizard
      this.steps = this.currentWizardConfig.getStepsAsList();
      
      // setup the first step
      determineCurrentPage();
   }
   
   /**
    * Returns the config for the current wizard
    * 
    * @return The current wizard config
    */
   public WizardConfig getCurrentWizard()
   {
      return this.currentWizardConfig;
   }
   
   /**
    * Returns the current wizard bean being managed
    * 
    * @return The current managed bean
    */
   public IWizardBean getBean()
   {
      return this.currentWizard;
   }
   
   /**
    * Returns the icon to use for the current wizard
    * 
    * @return The icon
    */
   public String getIcon()
   {
      return this.currentWizardConfig.getIcon();
   }
   
   /**
    * Returns the error message to use in error conditions
    * 
    * @return The error message
    */
   public String getErrorMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), 
            this.currentWizardConfig.getErrorMessageId());
   }
   
   /**
    * Returns the resolved title to use for the wizard
    * 
    * @return The title
    */
   public String getTitle()
   {
      String title = this.currentWizardConfig.getTitleId();
      
      if (title != null)
      {
         title = Application.getMessage(FacesContext.getCurrentInstance(), title);
      }
      else
      {
         title = this.currentWizardConfig.getTitle();
      }
      
      return title;
   }
   
   /**
    * Returns the resolved description to use for the wizard
    * 
    * @return The description
    */
   public String getDescription()
   {
      String desc = this.currentWizardConfig.getDescriptionId();
      
      if (desc != null)
      {
         desc = Application.getMessage(FacesContext.getCurrentInstance(), desc);
      }
      else
      {
         desc = this.currentWizardConfig.getDescription();
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
      return this.currentStep;
   }
   
   /**
    * Returns the current step position as a string for use in the UI
    * 
    * @return Current step position as a string
    */
   public String getCurrentStepAsString()
   {
      return Integer.toString(this.currentStep);
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
      return ((StepConfig)this.steps.get(this.currentStep-1)).getName();
   }

   /**
    * Returns a list of UIListItems representing the steps of the wizard
    * 
    * @return List of steps to display in UI
    */
   public List<UIListItem> getStepItems()
   {
      List<UIListItem> items = new ArrayList<UIListItem>(this.steps.size());
      
      for (int x = 0; x < this.steps.size(); x++)
      {
         String uiStepNumber = Integer.toString(x + 1);
         StepConfig stepCfg = this.steps.get(x);
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
      return this.currentPageCfg.getPath();
   }
   
   /**
    * Returns the title of the current step
    * 
    * @return The step title
    */
   public String getStepTitle()
   {
      String title = this.currentPageCfg.getTitleId();
      
      if (title != null)
      {
         title = Application.getMessage(FacesContext.getCurrentInstance(), title);
      }
      else
      {
         title = this.currentPageCfg.getTitle();
      }
      
      return title;
   }
   
   /**
    * Returns the description of the current step
    * 
    * @return The step description
    */
   public String getStepDescription()
   {
      String desc = this.currentPageCfg.getDescriptionId();
      
      if (desc != null)
      {
         desc = Application.getMessage(FacesContext.getCurrentInstance(), desc);
      }
      else
      {
         desc = this.currentPageCfg.getDescription();
      }
      
      return desc;
   }
   
   /**
    * Returns the instructions for the current step
    * 
    * @return The step instructions
    */
   public String getStepInstructions()
   {
      String instruction = this.currentPageCfg.getInstructionId();
      
      if (instruction != null)
      {
         instruction = Application.getMessage(FacesContext.getCurrentInstance(), instruction);
      }
      else
      {
         instruction = this.currentPageCfg.getInstruction();
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
      return this.currentWizard.getNextButtonLabel();
   }
   
   /**
    * Determines whether the next button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getNextButtonDisabled()
   {
      if (this.currentStep == this.steps.size())
      {
         return true;
      }
      else
      {
         return this.currentWizard.getNextButtonDisabled();
      }
   }
   
   /**
    * Returns the label to use for the back button
    * 
    * @return The back button label
    */
   public String getBackButtonLabel()
   {
      return this.currentWizard.getBackButtonLabel();
   }
   
   /**
    * Determines whether the back button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getBackButtonDisabled()
   {
      if (this.currentStep == 1)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Returns the label to use for the cancel button
    * 
    * @return The cancel button label
    */
   public String getCancelButtonLabel()
   {
      return this.currentWizard.getCancelButtonLabel();
   }
   
   /**
    * Returns the label to use for the finish button
    * 
    * @return The finish button label
    */
   public String getFinishButtonLabel()
   {
      return this.currentWizard.getFinishButtonLabel();
   }
   
   /**
    * Determines whether the finish button on the wizard should be disabled
    * 
    * @return true if the button should be disabled
    */
   public boolean getFinishButtonDisabled()
   {
      if (this.currentStep == this.steps.size())
      {
         return false;
      }
      else
      {
         return this.currentWizard.getFinishButtonDisabled();
      }
   }
   
   /**
    * Method handler called when the next button of the wizard is pressed
    * 
    * @return The outcome
    */
   public void next()
   {
      this.currentStep++;
      
      if (logger.isDebugEnabled())
         logger.debug("next called, current step is now: " + this.currentStep);
      
      // tell the wizard the next button has been pressed
      this.currentWizard.next();
      
      determineCurrentPage();
   }
   
   /**
    * Method handler called when the back button of the wizard is pressed
    * 
    * @return The outcome
    */
   public void back()
   {
      this.currentStep--;
      
      if (logger.isDebugEnabled())
         logger.debug("back called, current step is now: " + this.currentStep);
      
      // tell the wizard the back button has been pressed
      this.currentWizard.back();
      
      determineCurrentPage();
   }
   
   /**
    * Method handler called when the finish button of the wizard is pressed
    * 
    * @return The outcome
    */
   public String finish()
   {
      return this.currentWizard.finish();
   }
   
   /**
    * Method handler called when the cancel button of the wizard is pressed
    * 
    * @return The outcome
    */
   public String cancel()
   {
      return this.currentWizard.cancel();
   }
   
   /**
    * Sets up the current page to show in the wizard
    */
   protected void determineCurrentPage()
   {
      this.currentPageCfg = null;
      
      // get the config for the current step position
      StepConfig stepCfg = this.steps.get(this.currentStep-1);
      
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
               this.currentPageCfg = pageCfg;
               break;
            }
         }
      }
      
      // if none of the conditions passed use the default page
      if (this.currentPageCfg == null)
      {
         this.currentPageCfg = stepCfg.getDefaultPage();
      }
      
      if (this.currentPageCfg == null)
      {
         throw new AlfrescoRuntimeException("Failed to determine page for step '" + stepCfg.getName() +
               "'. Make sure a default page is configured.");
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Config for current page: " + this.currentPageCfg);
   }
}
