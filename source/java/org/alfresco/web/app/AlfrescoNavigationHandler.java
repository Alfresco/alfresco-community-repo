/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.app;

import java.util.Stack;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.dialog.DialogManager;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.WizardManager;
import org.alfresco.web.config.DialogsConfigElement;
import org.alfresco.web.config.NavigationConfigElement;
import org.alfresco.web.config.NavigationElementReader;
import org.alfresco.web.config.NavigationResult;
import org.alfresco.web.config.WizardsConfigElement;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;
import org.alfresco.web.config.WizardsConfigElement.WizardConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author gavinc
 */
public class AlfrescoNavigationHandler extends NavigationHandler
{
   public final static String OUTCOME_SEPARATOR = ":";
   public final static String DIALOG_PREXIX = "dialog" + OUTCOME_SEPARATOR;
   public final static String WIZARD_PREFIX = "wizard" + OUTCOME_SEPARATOR;
   public final static String CLOSE_DIALOG_OUTCOME = DIALOG_PREXIX + "close";
   public final static String CLOSE_WIZARD_OUTCOME = WIZARD_PREFIX + "close";
   
   protected final static String CONFIG_NAV_BEAN = "NavigationBean";
   protected final static String CONFIG_DIALOGS = "Dialogs";
   protected final static String CONFIG_WIZARDS = "Wizards";
   
   protected String dialogContainer = null;
   protected String wizardContainer = null;
   
   private final static Log logger = LogFactory.getLog(AlfrescoNavigationHandler.class);
   private final static String VIEW_STACK = "_alfViewStack";
   
   // The original navigation handler
   private NavigationHandler origHandler;
   
   /**
    * Default constructor
    * 
    * @param origHandler The original navigation handler
    */
   public AlfrescoNavigationHandler(NavigationHandler origHandler)
   {
      super();
      this.origHandler = origHandler;
   }

   /**
    * @see javax.faces.application.NavigationHandler#handleNavigation(javax.faces.context.FacesContext, java.lang.String, java.lang.String)
    */
   @Override
   @SuppressWarnings("unchecked")
   public void handleNavigation(FacesContext context, String fromAction, String outcome)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("handleNavigation (fromAction=" + fromAction + ", outcome=" + outcome + ")");
         logger.debug("Current view id: " + context.getViewRoot().getViewId());
      }

      boolean isDialog = isDialog(outcome);
      if (isDialog || isWizard(outcome))
      {
         boolean dialogWizardClosing = isDialogOrWizardClosing(outcome);
         outcome = stripPrefix(outcome);
         
         if (dialogWizardClosing)
         {
            handleDialogOrWizardClose(context, fromAction, outcome, isDialog);
         }
         else
         {
            if (isDialog)
            {
               handleDialogOpen(context, fromAction, outcome);
            }
            else
            {
               handleWizardOpen(context, fromAction, outcome);
            }
         }
      }
      else
      {
         if (isWizardStep(fromAction))
         {
            goToView(context, getWizardContainer(context));
         }
         else
         {
            handleDispatch(context, fromAction, outcome);
         }
      }
      
      if (logger.isDebugEnabled())
         logger.debug("view stack: " + getViewStack(context));
   }
   
   /**
    * Determines whether the given outcome is dialog related
    * 
    * @param outcome The outcome to test
    * @return true if outcome is dialog related i.e. starts with dialog:
    */
   protected boolean isDialog(String outcome)
   {
      boolean dialog = false;
      
      if (outcome != null && outcome.startsWith(DIALOG_PREXIX))
      {
         dialog = true;
      }
      
      return dialog;
   }
   
   /**
    * Determines whether the given outcome is wizard related
    * 
    * @param outcome The outcome to test
    * @return true if outcome is wizard related 
    * i.e. starts with create-wizard: or edit-wizard:
    */
   protected boolean isWizard(String outcome)
   {
      boolean wizard = false;
      
      if (outcome != null && outcome.startsWith(WIZARD_PREFIX))
      {
         wizard = true;
      }
      
      return wizard;
   }
   
   /**
    * Determines whether the given outcome represents a dialog or wizard closing
    * 
    * @param outcome The outcome to test
    * @return true if the outcome represents a closing dialog or wizard
    */
   protected boolean isDialogOrWizardClosing(String outcome)
   {
      boolean closing = false;
      
      if (outcome != null && 
          (outcome.startsWith(CLOSE_DIALOG_OUTCOME) ||
          outcome.startsWith(CLOSE_WIZARD_OUTCOME)))
      {
         closing = true;
      }
      
      return closing;
   }
   
   /**
    * Determines whether the given fromAction represents a step in the wizard
    * i.e. next or back
    * 
    * @param fromAction The fromAction
    * @return true if the from action represents a wizard step
    */
   protected boolean isWizardStep(String fromAction)
   {
      boolean wizardStep = false;
      
      if (fromAction != null && 
          (fromAction.equals("#{WizardManager.next}") || fromAction.equals("#{WizardManager.back}")))
      {
         wizardStep = true;
      }
      
      return wizardStep;
   }
   
   /**
    * Removes the dialog or wizard prefix from the given outcome
    * 
    * @param outcome The outcome to remove the prefix from
    * @return The remaining outcome
    */
   protected String stripPrefix(String outcome)
   {
      String newOutcome = outcome;
      
      if (outcome != null)
      {
         int idx = outcome.indexOf(OUTCOME_SEPARATOR);
         if (idx != -1)
         {
            newOutcome = outcome.substring(idx+1);
         }
      }
      
      return newOutcome;
   }
   
   /**
    * Returns the overridden outcome.
    * Used by dialogs and wizards to go to a particular page after it closes
    * rather than back to the page it was launched from.
    * 
    * @param outcome The current outcome
    * @return The overridden outcome or null if there isn't an override
    */
   protected String getOutcomeOverride(String outcome)
   {
      String override = null;
      
      if (outcome != null)
      {
         int idx = outcome.indexOf(OUTCOME_SEPARATOR);
         if (idx != -1)
         {
            override = outcome.substring(idx+1);
         }
      }
      
      return override;
   }
   
   /**
    * Returns the dialog configuration object for the given dialog name.
    * If there is a node in the dispatch context a lookup is performed using
    * the node. If this doesn't return any config or there is no dispatch
    * context node a 'global' dialog lookup is performed.
    * 
    * 
    * @param name The name of dialog being launched
    * @param dispatchContext The node being acted upon
    * @return The DialogConfig for the dialog or null if no config could be found
    */
   protected DialogConfig getDialogConfig(FacesContext context, String name, Node dispatchContext)
   {
      DialogConfig dialogConfig = null;
      ConfigService configSvc = Application.getConfigService(context);
      
      if (dispatchContext != null)
      {
         Config config = configSvc.getConfig(dispatchContext);
         if (config != null)
         {
            DialogsConfigElement dialogsCfg = (DialogsConfigElement)config.getConfigElement(
                  DialogsConfigElement.CONFIG_ELEMENT_ID);
            if (dialogsCfg != null)
            {
               dialogConfig = dialogsCfg.getDialog(name);
            }
         }
      }
      
      // if we didn't find a dialog via the dispatch look it up in the 'global' dialogs config
      if (dialogConfig == null)
      {
         Config config = configSvc.getConfig(CONFIG_DIALOGS);
         if (config != null)
         {
            DialogsConfigElement dialogsCfg = (DialogsConfigElement)config.getConfigElement(
                  DialogsConfigElement.CONFIG_ELEMENT_ID);
            if (dialogsCfg != null)
            {
               dialogConfig = dialogsCfg.getDialog(name);
            }
         }
      }
      
      return dialogConfig;
   }
   
   /**
    * Returns the wizard configuration object for the given wizard name.
    * If there is a node in the dispatch context a lookup is performed using
    * the node otherwise a 'global' wizard lookup is performed.
    * 
    * @param name The name of wizard being launched
    * @param dispatchContext The node being acted upon
    * @return The WizardConfig for the wizard or null if no config could be found
    */
   protected WizardConfig getWizardConfig(FacesContext context, String name, Node dispatchContext)
   {
      WizardConfig wizardConfig = null;
      ConfigService configSvc = Application.getConfigService(context);
      
      if (dispatchContext != null)
      {
         Config config = configSvc.getConfig(dispatchContext);
         if (config != null)
         {
            WizardsConfigElement wizardsCfg = (WizardsConfigElement)config.getConfigElement(
                  WizardsConfigElement.CONFIG_ELEMENT_ID);
            if (wizardsCfg != null)
            {
               wizardConfig = wizardsCfg.getWizard(name);
            }
         }
      }
      
      // if we didn't find a dialog via the dispatch look it up in the 'global' wizards config
      if (wizardConfig == null)
      {
         Config config = configSvc.getConfig(CONFIG_WIZARDS);
         if (config != null)
         {
            WizardsConfigElement wizardsCfg = (WizardsConfigElement)config.getConfigElement(
                  WizardsConfigElement.CONFIG_ELEMENT_ID);
            if (wizardsCfg != null)
            {
               wizardConfig = wizardsCfg.getWizard(name);
            }
         }
      }
      
      return wizardConfig;
   }
   
   /**
    * Retrieves the configured dialog container page
    * 
    * @param context FacesContext
    * @return The container page
    */
   protected String getDialogContainer(FacesContext context)
   {
      if (this.dialogContainer == null)
      {
         ConfigService configSvc = Application.getConfigService(context);
         Config dialogsConfig = configSvc.getConfig(CONFIG_DIALOGS);
         
         if (dialogsConfig != null)
         {
            this.dialogContainer = dialogsConfig.getConfigElement("dialog-container").getValue();
         }
      }
      
      return this.dialogContainer;
   }
   
   /**
    * Retrieves the configured wizard container page
    * 
    * @param context FacesContext
    * @return The container page
    */
   protected String getWizardContainer(FacesContext context)
   {
      if (this.wizardContainer == null)
      {
         ConfigService configSvc = Application.getConfigService(context);
         Config wizardsConfig = configSvc.getConfig(CONFIG_WIZARDS);
         
         if (wizardsConfig != null)
         {
            this.wizardContainer = wizardsConfig.getConfigElement("wizard-container").getValue();
         }
      }
      
      return this.wizardContainer;
   }
   
   /**
    * Returns the node currently in the dispatch context
    * 
    * @return The node currently in the dispatch context or null if 
    * the dispatch context is empty
    */
   protected Node getDispatchContextNode(FacesContext context)
   {
      Node dispatchNode = null;
      
      NavigationBean navBean = (NavigationBean)context.getExternalContext().
            getSessionMap().get(CONFIG_NAV_BEAN);

      if (navBean != null)
      {
         dispatchNode = navBean.getDispatchContextNode();
      }
      
      return dispatchNode;
   }
   
   /**
    * Processes any dispatching that may need to occur
    * 
    * @param node The node in the current dispatch context
    * @param viewId The current view id
    * @param outcome The outcome
    */
   protected void handleDispatch(FacesContext context, String fromAction, String outcome)
   {
      Node dispatchNode = getDispatchContextNode(context);

      if (dispatchNode != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Found node with type '" + dispatchNode.getType().toString() + 
                         "' in dispatch context");
   
         // get the current view id
         String viewId = context.getViewRoot().getViewId();
            
         // see if there is any navigation config for the node type
         ConfigService configSvc = Application.getConfigService(context);
         Config nodeConfig = configSvc.getConfig(dispatchNode);
         NavigationConfigElement navigationCfg = (NavigationConfigElement)nodeConfig.
               getConfigElement(NavigationElementReader.ELEMENT_NAVIGATION);
         
         if (navigationCfg != null)
         {
            // see if there is config for the current view state
            NavigationResult navResult = navigationCfg.getOverride(viewId, outcome);
            
            if (navResult != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Found navigation config: " + navResult);
               
               if (navResult.isOutcome())
               {
                  navigate(context, fromAction, navResult.getResult());
               }
               else
               {
                  String newViewId = navResult.getResult();
                  
                  if (newViewId.equals(viewId) == false)
                  {
                     if (logger.isDebugEnabled())
                        logger.debug("Dispatching to new view id: " + newViewId);
                  
                     goToView(context, newViewId);
                  }
                  else
                  {
                     if (logger.isDebugEnabled())
                        logger.debug("New view id is the same as the current one so setting outcome to null");
                     
                     navigate(context, fromAction, null);
                  }
               }
            }
            else
            {
               if (logger.isDebugEnabled())
                  logger.debug("No override configuration found for current view or outcome");
               
               navigate(context, fromAction, outcome);
            }
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("No navigation configuration found for node");
            
            navigate(context, fromAction, outcome);
         }
         
         // reset the dispatch context
         ((NavigationBean)context.getExternalContext().getSessionMap().
               get(CONFIG_NAV_BEAN)).resetDispatchContext();
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("No dispatch context found");
            
         // pass off to the original handler
         navigate(context, fromAction, outcome);
      }
   }
   
   /**
    * Opens a dialog
    * 
    * @param context FacesContext
    * @param fromAction The fromAction
    * @param name The name of the dialog to open
    */
   protected void handleDialogOpen(FacesContext context, String fromAction, String name)
   {
      if (logger.isDebugEnabled())
         logger.debug("Opening dialog '" + name + "'");
         
      // firstly add the current view to the stack so we know where to go back to
      addCurrentViewToStack(context);
      
      DialogConfig config = getDialogConfig(context, name, getDispatchContextNode(context));
      if (config != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Found config for dialog '" + name + "': " + config);
         
         // set the dialog manager up with the retrieved config
         DialogManager dialogManager = Application.getDialogManager();
         dialogManager.setCurrentDialog(config);
         
         // retrieve the container page and navigate to it
         goToView(context, getDialogContainer(context));
      }
      else
      {
         //logger.warn("Failed to find configuration for dialog '" + name + "'");
         
         // send the dialog name as the outcome to the original handler
         handleDispatch(context, fromAction, name);
      }
   }
   
   /**
    * Opens a wizard
    * 
    * @param context FacesContext
    * @param fromAction The fromAction
    * @param name The name of the wizard to open
    */
   protected void handleWizardOpen(FacesContext context, String fromAction, String name)
   {
      if (logger.isDebugEnabled())
         logger.debug("Opening wizard '" + name + "'");
      
      // firstly add the current view to the stack so we know where to go back to
      addCurrentViewToStack(context);
      
      WizardConfig wizard = getWizardConfig(context, name, getDispatchContextNode(context));
      if (wizard != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Found config for wizard '" + name + "': " + wizard);
            
         // set the wizard manager up with the retrieved config
         WizardManager wizardManager = Application.getWizardManager();
         wizardManager.setCurrentWizard(wizard);
         
         // retrieve the container page and navigate to it
         goToView(context, getWizardContainer(context));
      }
      else
      {
         //logger.warn("Failed to find configuration for wizard '" + name + "'");
         
         // send the dialog name as the outcome to the original handler
         handleDispatch(context, fromAction, name);
      }
   }
   
   /**
    * Closes the current dialog or wizard
    * 
    * @param context FacesContext
    * @param fromAction The fromAction
    * @param outcome The outcome
    * @param dialog true if a dialog is being closed, false if a wizard is being closed
    */
   protected void handleDialogOrWizardClose(FacesContext context, String fromAction, String outcome, boolean dialog)
   {
      String closingItem = dialog ? "dialog" : "wizard";
      
      // if we are closing a wizard or dialog take the view off the 
      // top of the stack then decide whether to use the view
      // or any overridden outcome that may be present
      if (getViewStack(context).empty() == false)
      {
         String newViewId = (String)getViewStack(context).pop();
      
         // is there an overidden outcome?
         String overriddenOutcome = getOutcomeOverride(outcome);
         if (overriddenOutcome == null)
         {
            // there isn't an overidden outcome so go back to the previous view
            if (logger.isDebugEnabled())
               logger.debug("Closing " + closingItem + ", going back to view id: " + newViewId);
         
            goToView(context, newViewId);
         }
         else
         {
            // we also need to empty the dialog stack if we have been given
            // an overidden outcome as we could be going anywhere in the app
            getViewStack(context).clear();
            
            if (logger.isDebugEnabled())
               logger.debug("Closing " + closingItem + " with an overridden outcome of '" + overriddenOutcome + "'");
            
            // if the override is calling another dialog or wizard come back through
            // the navigation handler from the beginning
            if (isDialog(overriddenOutcome) || isWizard(overriddenOutcome))
            {               
               this.handleNavigation(context, fromAction, overriddenOutcome);
            }
            else
            {
               navigate(context, fromAction, overriddenOutcome);
            }
         }
      }
      else
      {
         // we are trying to close a dialog when one hasn't been opened!
         // log a warning and return a null outcome to stay on the same page
         if (logger.isWarnEnabled())
         {
            logger.warn("Attempting to close a " + closingItem + " with an empty view stack, returning null outcome");
         }
         
         navigate(context, fromAction, null);
      }
   }
   
   /**
    * Adds the current view to the stack (if required).
    * If the current view is already the top of the stack it is not added again
    * to stop the stack from growing and growing.
    * 
    * @param context FacesContext
    */
   protected void addCurrentViewToStack(FacesContext context)
   {
      // if we are opening a wizard or dialog push the current view
      // id on to the stack, but only if it is different than the 
      // current view at the top (you can't launch a dialog from
      // the same page 2 times in a row!)
      
      // TODO: This wouldn't happen if we could be sure a dialog is 
      //       ALWAYS exited properly, look into a way of ensuring
      //       dialogs get closed if a user navigates away from the page,
      //       would a PhaseListener help in any way??
      
      String viewId = context.getViewRoot().getViewId();
      
      if (getViewStack(context).empty() || 
          viewId.equals(getViewStack(context).peek()) == false)
      {
         getViewStack(context).push(viewId);
      
         if (logger.isDebugEnabled())
            logger.debug("Pushed current view to stack: " + viewId);
      }
      else
      {
         if (getViewStack(context).empty() == false && logger.isDebugEnabled())
         {
            logger.debug("current view is already top the view stack!");
         }
      }
   }
   
   /**
    * Navigates to the appropriate page using the original navigation handler
    * 
    * @param context FacesContext
    * @param fromAction The fromAction
    * @param outcome The outcome
    */
   private void navigate(FacesContext context, String fromAction, String outcome)
   {
      if (logger.isDebugEnabled())
         logger.debug("Passing outcome '" + outcome + "' to original navigation handler");
         
      this.origHandler.handleNavigation(context, fromAction, outcome);
   }
   
   /**
    * Dispatches to the given view id
    * 
    * @param context Faces context
    * @param viewId The view id to go to
    */
   private void goToView(FacesContext context, String viewId)
   {
      ViewHandler viewHandler = context.getApplication().getViewHandler();
      UIViewRoot viewRoot = viewHandler.createView(context, viewId);
      viewRoot.setViewId(viewId);
      context.setViewRoot(viewRoot);
      context.renderResponse();
   }
   
   /**
    * Returns the view stack for the current user.
    *  
    * @param context FacesContext
    * @return A Stack representing the views that have launched dialogs in
    *         the users session, will never be null
    */
   @SuppressWarnings("unchecked")
   private Stack<String> getViewStack(FacesContext context)
   {
      Stack<String> viewStack = (Stack)context.getExternalContext().getSessionMap().get(VIEW_STACK);
      
      if (viewStack == null)
      {
         viewStack = new Stack<String>();
         context.getExternalContext().getSessionMap().put(VIEW_STACK, viewStack);
      }
      
      return viewStack;
   }
}
