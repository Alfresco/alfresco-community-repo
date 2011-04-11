/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.app;

import java.util.Stack;

import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.dialog.DialogManager;
import org.alfresco.web.bean.dialog.DialogState;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.WizardManager;
import org.alfresco.web.bean.wizard.WizardState;
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
   public final static String OUTCOME_BROWSE = "browse";
   public final static String DIALOG_PREFIX = "dialog" + OUTCOME_SEPARATOR;
   public final static String WIZARD_PREFIX = "wizard" + OUTCOME_SEPARATOR;
   public final static String CLOSE_DIALOG_OUTCOME = DIALOG_PREFIX + "close";
   public final static String CLOSE_WIZARD_OUTCOME = WIZARD_PREFIX + "close";
   public final static String CLOSE_MULTIPLE_START = "[";
   public final static String CLOSE_MULTIPLE_END = "]";
   public final static String EXTERNAL_CONTAINER_SESSION = "externalDialogContainer";
   
   protected String dialogContainer = null;
   protected String wizardContainer = null;
   protected String plainDialogContainer = null;
   protected String plainWizardContainer = null;
   
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
      
      // reset the dispatch context
      Object bean = FacesHelper.getManagedBean(context, NavigationBean.BEAN_NAME);
      if (bean instanceof NavigationBean)
      {
         ((NavigationBean)bean).resetDispatchContext();
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
      
      if (outcome != null && outcome.startsWith(DIALOG_PREFIX))
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
   
   protected int getNumberToClose(String outcome)
   {
      int toClose = 1;
      
      int idxStart = outcome.indexOf(CLOSE_MULTIPLE_START);
      if (outcome != null && idxStart != -1)
      {
         int idxEnd = outcome.indexOf(CLOSE_MULTIPLE_END);
         if (idxEnd != -1)
         {
            String closeNum = outcome.substring(idxStart+1, idxEnd);
            try
            {
               toClose = Integer.parseInt(closeNum);
            }
            catch (NumberFormatException nfe)
            {
               if (logger.isWarnEnabled())
                  logger.warn("Could not determine number of containers to close, defaulting to 1");
            }
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Closing " + toClose + " levels of container");
      }
      
      return toClose;
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
    * the node otherwise the global config section is used.
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
      
      Config config = null;
      
      if (dispatchContext != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Using dispatch context for dialog lookup: " + 
                  dispatchContext.getType().toString());
         
         // use the node to perform the lookup (this will include the global section)
         config = configSvc.getConfig(dispatchContext);
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Looking up dialog in global config");
            
         // just use the global 
         config = configSvc.getGlobalConfig();
      }

      if (config != null)
      {
         DialogsConfigElement dialogsCfg = (DialogsConfigElement)config.getConfigElement(
               DialogsConfigElement.CONFIG_ELEMENT_ID);
         if (dialogsCfg != null)
         {
            dialogConfig = dialogsCfg.getDialog(name);
         }
      }
      
      return dialogConfig;
   }
   
   /**
    * Returns the wizard configuration object for the given wizard name.
    * If there is a node in the dispatch context a lookup is performed using
    * the node otherwise the global config section is used.
    * 
    * @param name The name of wizard being launched
    * @param dispatchContext The node being acted upon
    * @return The WizardConfig for the wizard or null if no config could be found
    */
   protected WizardConfig getWizardConfig(FacesContext context, String name, Node dispatchContext)
   {
      WizardConfig wizardConfig = null;
      ConfigService configSvc = Application.getConfigService(context);
      
      Config config = null;
      
      if (dispatchContext != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Using dispatch context for wizard lookup: " + 
                  dispatchContext.getType().toString());
         
         // use the node to perform the lookup (this will include the global section)
         config = configSvc.getConfig(dispatchContext);
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Looking up wizard in global config");
         
         // just use the global 
         config = configSvc.getGlobalConfig();
      }

      if (config != null)
      {
         WizardsConfigElement wizardsCfg = (WizardsConfigElement)config.getConfigElement(
               WizardsConfigElement.CONFIG_ELEMENT_ID);
         if (wizardsCfg != null)
         {
            wizardConfig = wizardsCfg.getWizard(name);
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
      String container;
      
      // determine which kind of container we need to return, if the
      // external session flag is set then use the plain container
      Object obj = context.getExternalContext().getSessionMap().get(EXTERNAL_CONTAINER_SESSION);
      
      if (obj != null && obj instanceof Boolean && ((Boolean)obj).booleanValue())
      {
         if ((this.plainDialogContainer == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
         {
            ConfigService configSvc = Application.getConfigService(context);
            Config globalConfig = configSvc.getGlobalConfig();
            
            if (globalConfig != null)
            {
               this.plainDialogContainer = globalConfig.getConfigElement("plain-dialog-container").getValue();
            }
         }
         
         container = this.plainDialogContainer;
      }
      else
      {
         if ((this.dialogContainer == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
         {
            ConfigService configSvc = Application.getConfigService(context);
            Config globalConfig = configSvc.getGlobalConfig();
            
            if (globalConfig != null)
            {
               this.dialogContainer = globalConfig.getConfigElement("dialog-container").getValue();
            }
         }
         
         container = this.dialogContainer;
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Using dialog container: " + container);
      
      return container;
   }
   
   /**
    * Retrieves the configured wizard container page
    * 
    * @param context FacesContext
    * @return The container page
    */
   protected String getWizardContainer(FacesContext context)
   {
      String container;
      
      // determine which kind of container we need to return, if the
      // external session flag is set then use the plain container
      Object obj = context.getExternalContext().getSessionMap().get(EXTERNAL_CONTAINER_SESSION);
      
      if (obj != null && obj instanceof Boolean && ((Boolean)obj).booleanValue())
      {
         if ((this.plainWizardContainer == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
         {
            ConfigService configSvc = Application.getConfigService(context);
            Config globalConfig = configSvc.getGlobalConfig();
            
            if (globalConfig != null)
            {
               this.plainWizardContainer = globalConfig.getConfigElement("plain-wizard-container").getValue();
            }
         }
         
         container = this.plainWizardContainer;
      }
      else
      {
         if ((this.wizardContainer == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
         {
            ConfigService configSvc = Application.getConfigService(context);
            Config globalConfig = configSvc.getGlobalConfig();
            
            if (globalConfig != null)
            {
               this.wizardContainer = globalConfig.getConfigElement("wizard-container").getValue();
            }
         }
         
         container = this.wizardContainer;
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Using wizard container: " + container);
      
      return container;
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
            getSessionMap().get(NavigationBean.BEAN_NAME);

      if (navBean != null)
      {
         dispatchNode = navBean.getDispatchContextNode();
      }
      
      return dispatchNode;
   }
   
   protected void handleBrowseDispatch(FacesContext context, String fromAction, String outcome)
   {
      Node dispatchNode = null;

      NavigationBean navBean = (NavigationBean) context.getExternalContext().getSessionMap()
              .get(NavigationBean.BEAN_NAME);

      if (navBean != null)
      {
         dispatchNode = navBean.getCurrentNode();
      }

      handleDispatch(context, fromAction, outcome, dispatchNode);
   }

   /**
    * Processes any dispatching that may need to occur
    * 
    * @param context Faces context
    * @param fromAction The from action
    * @param outcome The outcome
    */
   protected void handleDispatch(FacesContext context, String fromAction, String outcome)
   {
      Node dispatchNode = getDispatchContextNode(context);
      handleDispatch(context, fromAction, outcome, dispatchNode);
   }
   
   private void handleDispatch(FacesContext context, String fromAction, String outcome, Node dispatchNode)
   {
      if (dispatchNode != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Found node with type '" + dispatchNode.getType().toString() + 
                         "' in dispatch context");
   
         // get the current view id
         String viewId = context.getViewRoot().getViewId();
            
         // see if there is any navigation config for the node type
         ConfigService configSvc = Application.getConfigService(context);
         NavigationConfigElement navigationCfg = null;
         try
         {
         Config nodeConfig = configSvc.getConfig(dispatchNode);
             navigationCfg = (NavigationConfigElement)nodeConfig.
               getConfigElement(NavigationElementReader.ELEMENT_NAVIGATION);
         }
         catch (InvalidNodeRefException e)
         {
             if (logger.isDebugEnabled()) logger.debug("Invalid node reference: " + dispatchNode);
         } 
         
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
               get(NavigationBean.BEAN_NAME)).resetDispatchContext();
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
         // is there an overidden outcome?
         String overriddenOutcome = getOutcomeOverride(outcome);
         if (overriddenOutcome == null)
         {
            // there isn't an overidden outcome so go back to the previous view
            if (logger.isDebugEnabled())
               logger.debug("Closing " + closingItem);

            // determine how many levels of dialog we need to close
            int numberToClose = getNumberToClose(outcome);
            
            Object stackObject = null;
            if (numberToClose == 1)
            {
               // just closing one dialog so get the item from the top of the stack
               stackObject = getViewStack(context).pop();
               
               if (logger.isDebugEnabled())
                  logger.debug("Popped item from the top of the view stack: " + stackObject);
            }
            else
            {
               // check there are enough items on the stack, if there
               // isn't just get the last one (effectively going back
               // to the beginning)
               Stack viewStack = getViewStack(context);
               int itemsOnStack = viewStack.size();
               if (itemsOnStack < numberToClose)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Returning to first item on the view stack as there aren't " +
                              numberToClose + " containers to close!");
                  
                  numberToClose = itemsOnStack;
               }
               
               // pop the right object from the stack
               for (int x = 1; x <= numberToClose; x++)
               {
                  stackObject = viewStack.pop();
               }
               
               if (logger.isDebugEnabled())
                  logger.debug("Popped item from the stack: " + stackObject);
            }
            
            // get the appropriate view id for the stack object
            String newViewId = getViewIdFromStackObject(context, stackObject);
            
            // go to the appropraite page
            goToView(context, newViewId);
         }
         else
         {
            // we also need to empty the dialog stack if we have been given
            // an overidden outcome as we could be going anywhere in the app.
            // grab the current top item first though in case we need to open
            // another dialog or wizard
            if (OUTCOME_BROWSE.equals(overriddenOutcome) == false)
            {
               String previousViewId = getViewIdFromStackObject(context, getViewStack(context).peek());
               getViewStack(context).clear();
               
               if (logger.isDebugEnabled())
                  logger.debug("Closing " + closingItem + " with an overridden outcome of '" + overriddenOutcome + "'");
               
               // if the override is calling another dialog or wizard come back through
               // the navigation handler from the beginning
               if (isDialog(overriddenOutcome) || isWizard(overriddenOutcome))
               {
                  // set the view id to the page at the top of the stack so when
                  // the new dialog or wizard closes it goes back to the correct page
                  context.getViewRoot().setViewId(previousViewId);

                  if (logger.isDebugEnabled())
                  {
                     logger.debug("view stack: " + getViewStack(context));
                     logger.debug("Opening '" + overriddenOutcome + "' after " + closingItem + 
                                  " close using view id: " + previousViewId);
                  }
                  
                  this.handleNavigation(context, fromAction, overriddenOutcome);
               }
               else
               {
                  navigate(context, fromAction, overriddenOutcome);
               }
            }
            else
            {
               handleBrowseDispatch(context, fromAction, overriddenOutcome);
               
               // ALF-6661, always clear the view stack with an overridden outcome
               getViewStack(context).clear();
            }
         }
      }
      else
      {
         // we are trying to close a dialog when one hasn't been opened!
         // return to the main page of the app (print warning if debug is enabled)
         if (logger.isDebugEnabled())
         {
            logger.debug("Attempting to close a " + closingItem + " with an empty view stack, returning 'browse' outcome");
         }
         
         navigate(context, fromAction, OUTCOME_BROWSE);
      }
   }
   
   /**
    * Returns the view id of the given item retrieved from the view stack.
    * 
    * @param context FacesContext
    * @param topOfStack The object retrieved from the view stack
    * @return The view id
    */
   protected String getViewIdFromStackObject(FacesContext context, Object topOfStack)
   {
      String viewId = null;
      
      // if the top of the stack is not a dialog or wizard just get the
      // view id and navigate back to it.
      
      // if the top of the stack is a dialog or wizard retrieve the state
      // and setup the appropriate manager with that state, then get the
      // appropriate container page and navigate to it.
      
      if (topOfStack instanceof String)
      {
         viewId = (String)topOfStack;
      }
      else if (topOfStack instanceof DialogState)
      {
         // restore the dialog state and get the dialog container viewId
         Application.getDialogManager().restoreState((DialogState)topOfStack);
         viewId = getDialogContainer(context);
      }
      else if (topOfStack instanceof WizardState)
      {
         // restore the wizard state and get the wizard container viewId
         Application.getWizardManager().restoreState((WizardState)topOfStack);
         viewId = getWizardContainer(context);
      }
      else
      {
         if (logger.isWarnEnabled())
            logger.warn("Invalid object found on view stack: " + topOfStack);
      }
      
      return viewId;
   }
   
   /**
    * Adds the current view to the stack (if required).
    * If the current view is already the top of the stack it is not added again
    * to stop the stack from growing and growing.
    * 
    * @param context FacesContext
    */
   @SuppressWarnings("unchecked")
   protected void addCurrentViewToStack(FacesContext context)
   {
      // if the current viewId is either the dialog or wizard container page
      // we need to save the state of the current dialog or wizard to the stack
      
      // If the current view is a normal page and it is not the same as the 
      // view currently at the top of the stack (you can't launch a dialog from
      // the same page 2 times in a row so it must mean the user navigated away
      // from the first dialog) just add the viewId to the stack
      
      // work out what to add to the stack
      String viewId = context.getViewRoot().getViewId();
      String dialogContainer = getDialogContainer(context);
      String wizardContainer = getWizardContainer(context);
      Object objectForStack = null;
      if (viewId.equals(dialogContainer))
      {
         DialogManager dlgMgr = Application.getDialogManager();
         objectForStack = dlgMgr.getState();
      }
      else if (viewId.equals(wizardContainer))
      {
         WizardManager wizMgr = Application.getWizardManager();
         objectForStack = wizMgr.getState();
      }
      else
      {
         objectForStack = viewId;
      }

      // if the stack is currently empty add the item
      Stack stack = getViewStack(context);
      if (stack.empty())
      {
         stack.push(objectForStack);
      
         if (logger.isDebugEnabled())
            logger.debug("Pushed item to view stack: " + objectForStack);
      }
      else
      {
         // if the item to go on to the stack and the top of
         // stack are both Strings and equals to each other
         // don't add anything to the stack to stop it 
         // growing unecessarily
         Object topOfStack = stack.peek();
         if (objectForStack instanceof String && 
             topOfStack instanceof String &&
             topOfStack.equals(objectForStack))
         {
            if (logger.isDebugEnabled())
               logger.debug("current view is already top of the view stack!");
         }
         else
         {
            stack.push(objectForStack);
      
            if (logger.isDebugEnabled())
               logger.debug("Pushed item to view stack: " + objectForStack);
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
   private Stack getViewStack(FacesContext context)
   {
      Stack viewStack = (Stack)context.getExternalContext().getSessionMap().get(VIEW_STACK);
      
      if (viewStack == null)
      {
         viewStack = new Stack();
         context.getExternalContext().getSessionMap().put(VIEW_STACK, viewStack);
      }
      
      return viewStack;
   }
}
