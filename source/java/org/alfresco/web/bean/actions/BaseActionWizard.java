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
package org.alfresco.web.bean.actions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.actions.handlers.MailHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;

/**
 * Base class for the "Run Action" and "Create Rule" wizards.
 * 
 * @author gavinc
 */
public abstract class BaseActionWizard extends BaseWizardBean
{
   protected static final String PROP_ACTION_NAME = "actionName";
   protected static final String PROP_ACTION_SUMMARY = "actionSummary";
   protected static final String PROP_ACTION_EMAIL_RECIPIENTS= "actionEmailRecipients";
   protected static final String NO_PARAMS_MARKER = "noParamsMarker"; 
   protected static final String ERROR_ACTION_CANNOT_BE_EXECUTE_REPEATEDLY = "action_cannot_be_execute_repeatedly";
   
   transient private ActionService actionService;
   transient private MimetypeService mimetypeService;
   transient private PersonService personService;
   transient private AuthorityService authorityService;
   
   protected List<SelectItem> actions;
   protected List<SelectItem> transformers;
   protected List<SelectItem> imageTransformers;
   protected List<SelectItem> commonAspects;
   protected List<SelectItem> removableAspects;
   protected List<SelectItem> addableAspects;
   protected List<SelectItem> testableAspects;
   protected List<SelectItem> users;
   protected List<SelectItem> encodings;
   protected List<SelectItem> objectTypes;
   transient protected DataModel allActionsDataModel;
   transient protected DataModel currentEmailRecipientsDataModel;
   
   protected boolean editingAction;
   protected String action;
   protected String usingTemplate = null;
   protected String returnViewId = null;
   
   protected Map<String, Serializable> currentActionProperties;
   protected List<Map<String, Serializable>> allActionsProperties;
   
   protected Map<String, IHandler> actionHandlers;
   
   private static final Log logger = LogFactory.getLog(BaseActionWizard.class);
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.action = null;
      this.users = null;
      this.actions = null;
      this.currentEmailRecipientsDataModel = null;
      this.usingTemplate = null;
      
      this.allActionsProperties = new ArrayList<Map<String, Serializable>>();
      this.currentActionProperties = new HashMap<String, Serializable>(3);
      
      this.editingAction = false;
      
      initialiseActionHandlers();
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * @return Returns the selected action
    */
   public String getAction()
   {
      return this.action;
   }

   /**
    * @param action Sets the selected action
    */
   public void setAction(String action)
   {
      this.action = action;
   }
   
   /**
    * @return Returns if a template has been inserted by a user for email body.
    */
   public String getUsingTemplate()
   {
      return this.usingTemplate;
   }

   /**
    * @param usingTemplate Template that has been inserted by a user for the email body.
    */
   public void setUsingTemplate(String usingTemplate)
   {
      this.usingTemplate = usingTemplate;
   }
   
   /**
    * Returns the properties for all the actions as a JSF DataModel
    * 
    * @return JSF DataModel representing the action properties
    */
   public DataModel getAllActionsDataModel()
   {
      if (this.allActionsDataModel == null)
      {
         this.allActionsDataModel = new ListDataModel();
      }
      
      this.allActionsDataModel.setWrappedData(this.allActionsProperties);
      
      return this.allActionsDataModel;
   }
   
   /**
    * Returns the properties for email recipients JSF DataModel
    * 
    * @return JSF DataModel wrapping the current email recipients
    */
   public DataModel getEmailRecipientsDataModel()
   {
      if (this.currentEmailRecipientsDataModel == null)
      {
         this.currentEmailRecipientsDataModel = new ListDataModel();
      }

      this.currentEmailRecipientsDataModel.setWrappedData(getEmailRecipients(this.currentActionProperties));
      
      return this.currentEmailRecipientsDataModel;
   }
   
   /**
    * @return Gets the action settings
    */
   public Map<String, Serializable> getActionProperties()
   {
      return this.currentActionProperties;
   }
   
   /**
    * @return Returns the list of selectable actions
    */
   public List<SelectItem> getActions()
   {
      if (this.actions == null)
      {
         List<ActionDefinition> ruleActions = this.getActionService().getActionDefinitions();
         this.actions = new ArrayList<SelectItem>();
         for (ActionDefinition ruleActionDef : ruleActions)
         {
            String title = ruleActionDef.getTitle();
            if (title == null || title.length() == 0)
            {
               title = ruleActionDef.getName();
            }
            this.actions.add(new SelectItem(ruleActionDef.getName(), title));
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.actions, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         // add the select an action item at the start of the list
         this.actions.add(0, new SelectItem("null", 
               Application.getMessage(FacesContext.getCurrentInstance(), "select_an_action")));
      }
      
      return this.actions;
   }
   
   /**
    * Returns a list of aspects that can be removed
    * 
    * @return List of SelectItem objects representing the aspects that can be removed
    */
   public List<SelectItem> getRemovableAspects()
   {
      if ((this.removableAspects == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         // get the list of common aspects
         this.removableAspects = new ArrayList<SelectItem>();
         this.removableAspects.addAll(getCommonAspects());
         
         // get those aspects configured to appear only in the remove aspect action
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement aspectsCfg = wizardCfg.getConfigElement("aspects-remove");
            if (aspectsCfg != null)
            {
               List<SelectItem> aspects = readAspectsConfig(FacesContext.getCurrentInstance(), aspectsCfg);
               this.removableAspects.addAll(aspects);
            }
            else
            {
               logger.warn("Could not find 'aspects-remove' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.removableAspects, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      return this.removableAspects;
   }
   
   /**
    * Returns a list of aspects that can be added
    * 
    * @return List of SelectItem objects representing the aspects that can be added
    */
   public List<SelectItem> getAddableAspects()
   {
      if ((this.addableAspects == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         // get the list of common aspects
         this.addableAspects = new ArrayList<SelectItem>();
         this.addableAspects.addAll(getCommonAspects());
         
         // get those aspects configured to appear only in the remove aspect action
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement aspectsCfg = wizardCfg.getConfigElement("aspects-add");
            if (aspectsCfg != null)
            {
               List<SelectItem> aspects = readAspectsConfig(FacesContext.getCurrentInstance(), aspectsCfg);
               this.addableAspects.addAll(aspects);
            }
            else
            {
               logger.warn("Could not find 'aspects-add' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.addableAspects, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      return this.addableAspects;
   }
   
   /**
    * Returns a list of aspects that can be tested i.e. hasAspect
    * 
    * @return List of SelectItem objects representing the aspects that can be tested for
    */
   public List<SelectItem> getTestableAspects()
   {
      if ((this.testableAspects == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         // get the list of common aspects
         this.testableAspects = new ArrayList<SelectItem>();
         this.testableAspects.addAll(getCommonAspects());
         
         // get those aspects configured to appear only in the remove aspect action
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement aspectsCfg = wizardCfg.getConfigElement("aspects-test");
            if (aspectsCfg != null)
            {
               List<SelectItem> aspects = readAspectsConfig(FacesContext.getCurrentInstance(), aspectsCfg);
               this.testableAspects.addAll(aspects);
            }
            else
            {
               logger.warn("Could not find 'aspects-test' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.testableAspects, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      return this.testableAspects;
   }
   
   /**
    * @return Returns a list of object types to allow the user to select from
    */
   public List<SelectItem> getObjectTypes()
   {
      if ((this.objectTypes == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // add the well known object type to start with
         this.objectTypes = new ArrayList<SelectItem>(5);
         
         // add any configured content or folder sub-types to the list
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("specialise-types");
            if (typesCfg != null)
            {               
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));
                  TypeDefinition typeDef = this.getDictionaryService().getType(idQName);
                  
                  // make sure the type is a subtype of content or folder but not 
                  // the content or folder type itself
                  if (typeDef != null &&
                      typeDef.getName().equals(ContentModel.TYPE_CONTENT) == false &&
                      typeDef.getName().equals(ContentModel.TYPE_FOLDER) == false &&
                      (this.getDictionaryService().isSubClass(typeDef.getName(), ContentModel.TYPE_CONTENT) ||
                      this.getDictionaryService().isSubClass(typeDef.getName(), ContentModel.TYPE_FOLDER)))
                  {
                     // try and get the display label from config
                     String label = Utils.getDisplayLabel(context, child);
   
                     // if there wasn't a client based label try and get it from the dictionary
                     if (label == null)
                     {
                        label = typeDef.getTitle();
                     }
                     
                     // finally, just use the localname
                     if (label == null)
                     {
                        label = idQName.getLocalName();
                     }
                     
                     this.objectTypes.add(new SelectItem(idQName.toString(), label));
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.objectTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
               
               // add the select an action item at the start of the list
               this.objectTypes.add(0, new SelectItem("null", 
                     Application.getMessage(FacesContext.getCurrentInstance(), "select_a_type")));
            }
            else
            {
               logger.warn("Could not find 'specialise-types' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
         
      }
      
      return this.objectTypes;
   }
   
   /**
    * @return the List of users in the system wrapped in SelectItem objects
    */
   public List<SelectItem> getUsers()
   {
      if (this.users == null)
      {
         List<Node> userNodes = Repository.getUsers(
               FacesContext.getCurrentInstance(),
               this.getNodeService(),
               this.getSearchService());
         this.users = new ArrayList<SelectItem>();
         for (Node user : userNodes)
         {
            String email = (String)user.getProperties().get("email");
            if (email != null && email.length() > 0)
            {
               this.users.add(new SelectItem(email, (String)user.getProperties().get("fullName")));
            }
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.users, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      return this.users;
   }
   
   /**
    * Returns the transformers that are available
    * 
    * @return List of SelectItem objects representing the available transformers
    */
   public List<SelectItem> getTransformers()
   {
      if ((this.transformers == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement transformersCfg = wizardCfg.getConfigElement("transformers");
            if (transformersCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               Map<String, String> mimeTypes = this.getMimetypeService().getDisplaysByMimetype();
               this.transformers = new ArrayList<SelectItem>();
               for (ConfigElement child : transformersCfg.getChildren())
               {
                  String id = child.getAttribute("name");
                  
                  // try and get the display label from config
                  String label = Utils.getDisplayLabel(context, child);

                  // if there wasn't a client based label get it from the mime type service
                  if (label == null)
                  {
                     label = mimeTypes.get(id);
                  }
                  
                  // if there is still no label use the raw mimetype
                  if (label == null)
                  {
                      label = id;
                  }
                  
                  this.transformers.add(new SelectItem(id, label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.transformers, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find 'transformers' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
      }
      
      return this.transformers;
   }
   
   /**
    * Returns the image transformers that are available
    * 
    * @return List of SelectItem objects representing the available image transformers
    */
   public List<SelectItem> getImageTransformers()
   {
      if ((this.imageTransformers == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement transformersCfg = wizardCfg.getConfigElement("image-transformers");
            if (transformersCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               Map<String, String> mimeTypes = this.getMimetypeService().getDisplaysByMimetype();
               this.imageTransformers = new ArrayList<SelectItem>();
               for (ConfigElement child : transformersCfg.getChildren())
               {
                  String id = child.getAttribute("name");
                  
                  // try and get the display label from config
                  String label = Utils.getDisplayLabel(context, child);

                  // if there wasn't a client based label get it from the mime type service
                  if (label == null)
                  {
                     label = mimeTypes.get(id);
                  }
                  
                  // if there is still no label use the raw mimetype
                  if (label == null)
                  {
                      label = id;
                  }

                  this.imageTransformers.add(new SelectItem(id, label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.imageTransformers, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find 'image-transformers' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
      }
      
      return this.imageTransformers;
   }
   
   /**
    * Returns the current list of email recipients
    * 
    * @return List of email recipients
    */
   public List<RecipientWrapper> getEmailRecipients(Map<String, Serializable> actionProperties)
   {
      List<RecipientWrapper> currentEmailRecipients = (List<RecipientWrapper>) actionProperties.get(PROP_ACTION_EMAIL_RECIPIENTS);
      if (currentEmailRecipients == null)
      {
         currentEmailRecipients = new ArrayList<RecipientWrapper>(4);
         actionProperties.put(PROP_ACTION_EMAIL_RECIPIENTS, (Serializable)currentEmailRecipients);
      }
      return currentEmailRecipients;
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Displays the settings page for the current action being added
    */
   public void promptForActionValues()
   {
      // set the flag to show we are creating a new action
      this.editingAction = false;
      
      FacesContext context = FacesContext.getCurrentInstance();
      this.returnViewId = context.getViewRoot().getViewId();
      String viewId = null;
      
      HashMap<String, Serializable> actionProps = new HashMap<String, Serializable>(3);
      actionProps.put(PROP_ACTION_NAME, this.action);
      this.currentActionProperties = actionProps;
      this.currentEmailRecipientsDataModel = null;
      
      // get the handler for the action, if there isn't one we presume it
      // is a no-parameter action
      IHandler handler = this.actionHandlers.get(this.action);
      
      if (handler != null && !handler.isAllowMultiple() && isActionPresent(this.action))
      {
         Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), ERROR_ACTION_CANNOT_BE_EXECUTE_REPEATEDLY));
         return;
      }
      
      if (handler != null)
      {
         // setup any UI defaults the action may have and get the location of
         // the JSP used to collect the parameters
         handler.setupUIDefaults(actionProps);
         viewId = handler.getJSPPath();
      }
      else
      {
         // just add the action to the list and use the title as the summary
         ActionDefinition actionDef = this.getActionService().getActionDefinition(this.action);
         actionProps.put(PROP_ACTION_SUMMARY, actionDef.getTitle());
         // add the no params marker so we can disable the edit action
         actionProps.put(NO_PARAMS_MARKER, "no-params");
         this.allActionsProperties.add(actionProps);
         
         // come back to the same page we're on now as there are no params to collect
         viewId = this.returnViewId;
      }
      
      if (logger.isDebugEnabled())
            logger.debug("Added '" + this.action + "' action to list");
      
      // go to the page to collect the settings
      goToPage(context, viewId);
   }
   
   /**
    * Sets up the context for editing existing action values 
    */
   @SuppressWarnings("unchecked")
   public void editAction()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      int index = this.allActionsDataModel.getRowIndex();
      this.currentActionProperties = this.allActionsProperties.get(index);
      this.action = (String)this.currentActionProperties.get(PROP_ACTION_NAME);
      this.currentEmailRecipientsDataModel = null;
      
      // set the flag to show we are editing an action
      this.editingAction = true;
      
      // remember the page we're on
      FacesContext context = FacesContext.getCurrentInstance();
      this.returnViewId = context.getViewRoot().getViewId();
      
      // go to the action page (as there is an edit option visible,
      // there must be a handler for the action so we don't check)
      goToPage(context, this.actionHandlers.get(this.action).getJSPPath());
   }
   
   /**
    * Adds the action just setup by the user to the list of actions for the rule
    */
   public void addAction()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      // this is called from the actions page so there must be a handler
      // present so there's no need to check for null
      String summary = this.actionHandlers.get(this.action).generateSummary(
            context, this, this.currentActionProperties);
      
      if (summary != null)
      {
         this.currentActionProperties.put(PROP_ACTION_SUMMARY, summary);
      }
      
      if (this.editingAction == false)
      {
         this.allActionsProperties.add(this.currentActionProperties);
      }
      
      // reset the action drop down
      this.action = null;
      
      // refresh the wizard
      goToPage(context, this.returnViewId);
   }
   
   /**
    * Removes the requested action from the list
    */
   public void removeAction()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      @SuppressWarnings("unchecked")
      int index = this.allActionsDataModel.getRowIndex();
      this.allActionsProperties.remove(index);
      
      // reset the action drop down
      this.action = null;
      
      // refresh the wizard
      FacesContext context = FacesContext.getCurrentInstance();
      goToPage(context, context.getViewRoot().getViewId());
   }
   
   /**
    * Cancels the addition of the action
    */
   public void cancelAddAction()
   {
      if (this.editingAction == false)
      {
         this.currentActionProperties.clear();
      }
      
      // reset the action drop down
      this.action = null;
      
      // refresh the wizard
      goToPage(FacesContext.getCurrentInstance(), this.returnViewId);
   }
   
   /**
    * Action handler called when the Add button is pressed to add an email recipient
    */
   public void addRecipient(ActionEvent event)
   {
      UIGenericPicker picker = (UIGenericPicker)event.getComponent();
      String[] results = picker.getSelectedResults();
      if (results != null && results.length != 0)
      {
         List<RecipientWrapper> currentEmailRecipients = getEmailRecipients(this.currentActionProperties);
         for (String authority : results)
         {
            // first check the authority has not already been added to the list
            boolean alreadyAdded = false;
            for (int i=0; i<currentEmailRecipients.size(); i++)
            {
               RecipientWrapper wrapper = currentEmailRecipients.get(i);
               if (wrapper.getAuthority().equals(authority))
               {
                  alreadyAdded = true;
                  break;
               }
            }
            
            if (alreadyAdded == false)
            {
               // find a display label for the authority if it is a known Person
               String name = displayLabelForAuthority(authority);
               
               // add the recipient to the list
               RecipientWrapper wrapper = new RecipientWrapper(name, authority);
               currentEmailRecipients.add(wrapper);
            }
         }
      }
   }
   
   /**
    * Action handler called when the Remove icon is pressed to remove an email recipient
    */
   public void removeRecipient(ActionEvent event)
   {
      List<RecipientWrapper> currentEmailRecipients = getEmailRecipients(this.currentActionProperties);
      int index = this.currentEmailRecipientsDataModel.getRowIndex();
      currentEmailRecipients.remove(index);
   }
   
   /**
    * Action handler called to insert a template as the email body
    */
   public void insertTemplate(ActionEvent event)
   {
      String template = (String)this.currentActionProperties.get(MailHandler.PROP_TEMPLATE);
      if (template != null && template.equals(TemplateSupportBean.NO_SELECTION) == false)
      {
         // get the content of the template so the user can get a basic preview of it
         try
         {
            NodeRef templateRef = new NodeRef(Repository.getStoreRef(), template);
            ContentService cs = Repository.getServiceRegistry(
                  FacesContext.getCurrentInstance()).getContentService();
            ContentReader reader = cs.getReader(templateRef, ContentModel.PROP_CONTENT);
            if (reader != null && reader.exists())
            {
               this.currentActionProperties.put(MailHandler.PROP_MESSAGE, 
                     reader.getContentString());
               
               usingTemplate = template;
            }
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         }
      }
   }
   
   /**
    * Action handler called to discard the template from the email body
    */
   public void discardTemplate(ActionEvent event)
   {
      this.currentActionProperties.put(MailHandler.PROP_MESSAGE, "");
      usingTemplate = null;
   }
   
   
   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * Sets the action service
    * 
    * @param actionService  the action service
    */
   public void setActionService(ActionService actionService)
   {
     this.actionService = actionService;
   }
   
   protected ActionService getActionService()
   {
      if (actionService == null)
      {
         actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
   }

   /**
    * Sets the mimetype service
    * 
    * @param mimetypeService    The mimetype service
    */
   public void setMimetypeService(MimetypeService mimetypeService)
   {
      this.mimetypeService = mimetypeService;
   }
   
   protected MimetypeService getMimetypeService()
   {
      if (mimetypeService == null)
      {
         mimetypeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMimetypeService();
      }
      return mimetypeService;
   }
   
   /**
    * @param personService      The personService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   protected PersonService getPersonService()
   {
      if (personService == null)
      {
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      }
      return personService;
   }
   
   /**
    * @param authorityService   The authorityService to set.
    */
   public void setAuthorityService(AuthorityService authorityService)
   {
      this.authorityService = authorityService;
   }
   
   protected AuthorityService getAuthorityService()
   {
      if (authorityService == null)
      {
         authorityService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthorityService();
      }
      return authorityService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   public String displayLabelForAuthority(String authority)
   {
       String label = authority;
       
       if (this.getPersonService().personExists(authority))
       {
          // create the node ref, then our node representation
          NodeRef ref = getPersonService().getPerson(authority);
          Node node = new Node(ref);
          
          // setup convience function for current user full name
          label = (String)node.getProperties().get(ContentModel.PROP_FIRSTNAME) + ' ' +
                  (String)node.getProperties().get(ContentModel.PROP_LASTNAME);
       }
       
       return label;
   }
   
   /**
    * Navigates to the given page, used to go back and forth between the
    * wizard and the actions settings pages
    * 
    * @param context FacesContext
    * @param viewId The viewId to go to
    */
   protected void goToPage(FacesContext context, String viewId)
   {
      ViewHandler viewHandler = context.getApplication().getViewHandler();
      UIViewRoot viewRoot = viewHandler.createView(context, viewId);
      viewRoot.setViewId(viewId);
      context.setViewRoot(viewRoot);
      context.renderResponse();
   }
   
   /**
    * Initialises the action handlers from the current configuration.
    */
   protected void initialiseActionHandlers()
   {
      if ((this.actionHandlers == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement actionHandlerCfg = wizardCfg.getConfigElement("action-handlers");
            if (actionHandlerCfg != null)
            {
               this.actionHandlers = new HashMap<String, IHandler>(20);
               
               // instantiate each handler and store in the map
               for (ConfigElement child : actionHandlerCfg.getChildren())
               {
                  String actionName = child.getAttribute("name");
                  String handlerClass = child.getAttribute("class");
                  
                  if (actionName != null && actionName.length() > 0 &&
                      handlerClass != null && handlerClass.length() > 0)
                  {
                     try
                     {
                        @SuppressWarnings("unchecked")
                        Class klass = Class.forName(handlerClass);
                        IHandler handler = (IHandler)klass.newInstance();
                        this.actionHandlers.put(actionName, handler);
                     }
                     catch (Exception e)
                     {
                        throw new AlfrescoRuntimeException("Failed to setup action handler for '" + 
                              actionName + "'", e);
                     }
                  }
               }
            }
            else
            {
               logger.warn("Could not find 'action-handlers' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
      }
   }
   
   /**
    * Returns the aspects that are available in all scenarios i.e. add, remove and test
    * 
    * @return List of SelectItem objects representing the available aspects
    */
   protected List<SelectItem> getCommonAspects()
   {
      if ((this.commonAspects == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement aspectsCfg = wizardCfg.getConfigElement("aspects");
            if (aspectsCfg != null)
            {
               this.commonAspects = readAspectsConfig(FacesContext.getCurrentInstance(), aspectsCfg);
            }
            else
            {
               logger.warn("Could not find 'aspects' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
      }
      
      return this.commonAspects;
   }
   
   
   protected List<SelectItem> readAspectsConfig(FacesContext context, ConfigElement aspectsCfg)
   {
      List<SelectItem> aspects = new ArrayList<SelectItem>();

      for (ConfigElement child : aspectsCfg.getChildren())
      {
         QName idQName = Repository.resolveToQName(child.getAttribute("name"));

         if (idQName != null)
         {
            // try and get the display label from config
            String label = Utils.getDisplayLabel(context, child);

            // if there wasn't a client based label try and get it from the dictionary
            if (label == null)
            {
               AspectDefinition aspectDef = this.getDictionaryService().getAspect(idQName);
               if (aspectDef != null)
               {
                  label = aspectDef.getTitle();
               }
               else
               {
                  label = idQName.getLocalName();
               }
            }
            
            aspects.add(new SelectItem(idQName.toString(), label));
         }
         else
         {
            logger.warn("Failed to resolve aspect '" + child.getAttribute("name") + "'");
         }
      }
      
      return aspects;
   }
   
   public boolean isFinishButtonDisabled()
   {
      List<RecipientWrapper> currentEmailRecipients = (List<RecipientWrapper>) this.currentActionProperties.get(PROP_ACTION_EMAIL_RECIPIENTS);
      if (currentEmailRecipients != null)
      {
         return currentEmailRecipients.isEmpty();
      }
      return true; 
   }
   
   protected boolean isActionPresent(String actionName)
   {
      for(Map<String, Serializable> prop : allActionsProperties)
      {
         Serializable name = prop.get(PROP_ACTION_NAME);
         if (name != null)
         {
            if (name.equals(actionName))
            {
               return true;
            }
         }
      }
      
      return false;
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Simple wrapper class for email recipient fields
    */
   public static class RecipientWrapper implements Serializable
   {
      private static final long serialVersionUID = -3331836277440957711L;
      
      public RecipientWrapper(String name, String authority)
      {
         this.name = name;
         this.authority = authority;
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public String getAuthority()
      {
         return this.authority;
      }
      
      public boolean equals(Object obj)
      {
         if (obj instanceof RecipientWrapper)
         {
            return this.authority.equals( ((RecipientWrapper)obj).getAuthority() );
         }
         else
         {
            return false;
         }
      }
      
      public int hashCode()
      {
         return authority.hashCode();
      }
      
      private String name;
      private String authority;
   }
}
