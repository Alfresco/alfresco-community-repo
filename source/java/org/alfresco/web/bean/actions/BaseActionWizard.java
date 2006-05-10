package org.alfresco.web.bean.actions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
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

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.ImageTransformActionExecuter;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuter;
import org.alfresco.repo.action.executer.ScriptActionExecutor;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for the "Run Action" and "Create Rule" wizards.
 * 
 * @author gavinc
 */
public abstract class BaseActionWizard extends BaseWizardBean
{
   public static final String PROP_ACTION_NAME = "actionName";
   public static final String PROP_ACTION_SUMMARY = "actionSummary";
   public static final String PROP_CATEGORY = "category";
   public static final String PROP_ASPECT = "aspect";
   public static final String PROP_DESTINATION = "destinationLocation";
   public static final String PROP_APPROVE_STEP_NAME = "approveStepName";
   public static final String PROP_APPROVE_ACTION = "approveAction";
   public static final String PROP_APPROVE_FOLDER = "approveFolder";
   public static final String PROP_REJECT_STEP_PRESENT = "rejectStepPresent";
   public static final String PROP_REJECT_STEP_NAME = "rejectStepName";
   public static final String PROP_REJECT_ACTION = "rejectAction";
   public static final String PROP_REJECT_FOLDER = "rejectFolder";
   public static final String PROP_CHECKIN_DESC = "checkinDescription";
   public static final String PROP_CHECKIN_MINOR = "checkinMinorChange";
   public static final String PROP_TRANSFORMER = "transformer";
   public static final String PROP_IMAGE_TRANSFORMER = "imageTransformer";
   public static final String PROP_TRANSFORM_OPTIONS = "transformOptions";
   public static final String PROP_ENCODING = "encoding";
   public static final String PROP_MESSAGE = "message";
   public static final String PROP_SUBJECT = "subject";
   public static final String PROP_TO = "to";
   public static final String PROP_FROM = "from";
   public static final String PROP_TEMPLATE = "template";
   public static final String PROP_OBJECT_TYPE = "objecttype";
   public static final String PROP_PROPERTY = "property";
   public static final String PROP_CONTAINS_TEXT = "containstext";
   public static final String PROP_MODEL_TYPE = "modeltype";
   public static final String PROP_MIMETYPE = "mimetype";
   public static final String PROP_MODEL_ASPECT = "modelaspect";
   public static final String PROP_TYPE_OR_ASPECT = "typeoraspect";
   public static final String PROP_SCRIPT = "script";
   
   protected ActionService actionService;
   protected MimetypeService mimetypeService;
   protected PersonService personService;
   protected AuthorityService authorityService;
   
   protected List<SelectItem> actions;
   protected List<SelectItem> transformers;
   protected List<SelectItem> imageTransformers;
   protected List<SelectItem> aspects;
   protected List<SelectItem> users;
   protected List<SelectItem> encodings;
   protected List<SelectItem> objectTypes;
   protected List<RecipientWrapper> emailRecipients;
   
   protected DataModel allActionsDataModel;
   protected DataModel emailRecipientsDataModel;
   
   protected boolean editingAction;
   protected String action;
   protected String usingTemplate = null;
   protected String returnViewId = null;
   
   protected Map<String, Serializable> currentActionProperties;
   protected List<Map<String, Serializable>> allActionsProperties;
   
   protected static final String ACTION_PAGES_LOCATION = "/jsp/actions/";
   
   private static final Log logger = LogFactory.getLog(BaseActionWizard.class);
   private static final String IMPORT_ENCODING = "UTF-8";
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.action = null;
      this.users = null;
      this.actions = null;
      this.emailRecipientsDataModel = null;
      this.emailRecipients = new ArrayList<RecipientWrapper>(4);
      this.usingTemplate = null;
      
      this.allActionsProperties = new ArrayList<Map<String, Serializable>>();
      this.currentActionProperties = new HashMap<String, Serializable>(3);
      
      // default the approve and reject actions
      this.currentActionProperties.put(PROP_APPROVE_ACTION, "move");
      this.currentActionProperties.put(PROP_REJECT_STEP_PRESENT, "yes");
      this.currentActionProperties.put(PROP_REJECT_ACTION, "move");
      
      // default the checkin minor change
      this.currentActionProperties.put(PROP_CHECKIN_MINOR, new Boolean(true));
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
      if (this.emailRecipientsDataModel == null)
      {
         this.emailRecipientsDataModel = new ListDataModel();
      }
      
      this.emailRecipientsDataModel.setWrappedData(this.emailRecipients);
      
      return this.emailRecipientsDataModel;
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
         List<ActionDefinition> ruleActions = this.actionService.getActionDefinitions();
         this.actions = new ArrayList<SelectItem>();
         for (ActionDefinition ruleActionDef : ruleActions)
         {
            this.actions.add(new SelectItem(ruleActionDef.getName(), ruleActionDef.getTitle()));
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
    * Returns the aspects that are available
    * 
    * @return List of SelectItem objects representing the available aspects
    */
   public List<SelectItem> getAspects()
   {
      if (this.aspects == null)
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement aspectsCfg = wizardCfg.getConfigElement("aspects");
            if (aspectsCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               this.aspects = new ArrayList<SelectItem>();
               for (ConfigElement child : aspectsCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));

                  // try and get the display label from config
                  String label = Utils.getDisplayLabel(context, child);

                  // if there wasn't a client based label try and get it from the dictionary
                  if (label == null)
                  {
                     AspectDefinition aspectDef = this.dictionaryService.getAspect(idQName);
                     if (aspectDef != null)
                     {
                        label = aspectDef.getTitle();
                     }
                     else
                     {
                        label = idQName.getLocalName();
                     }
                  }
                  
                  this.aspects.add(new SelectItem(idQName.toString(), label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.aspects, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find aspects configuration element");
            }
         }
         else
         {
            logger.warn("Could not find Action Wizards configuration section");
         }
      }
      
      return this.aspects;
   }
   
   /**
    * @return Returns a list of object types to allow the user to select from
    */
   public List<SelectItem> getObjectTypes()
   {
      if (this.objectTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // add the well known object type to start with
         this.objectTypes = new ArrayList<SelectItem>(5);
         this.objectTypes.add(new SelectItem(ContentModel.TYPE_CONTENT.toString(), 
               Application.getMessage(context, "content")));
         
         // add any configured content sub-types to the list
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Custom Content Types");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("content-types");
            if (typesCfg != null)
            {               
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));
                  TypeDefinition typeDef = this.dictionaryService.getType(idQName);
                  
                  if (typeDef != null &&
                      this.dictionaryService.isSubClass(typeDef.getName(), ContentModel.TYPE_CONTENT))
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
            }
            else
            {
               logger.warn("Could not find 'content-types' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Custom Content Types' configuration section");
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
               this.nodeService,
               this.searchService);
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
      if (this.transformers == null)
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement transformersCfg = wizardCfg.getConfigElement("transformers");
            if (transformersCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               Map<String, String> mimeTypes = this.mimetypeService.getDisplaysByMimetype();
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
                  
                  this.transformers.add(new SelectItem(id, label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.transformers, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find transformers configuration element");
            }
         }
         else
         {
            logger.warn("Could not find Action Wizards configuration section");
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
      if (this.imageTransformers == null)
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement transformersCfg = wizardCfg.getConfigElement("image-transformers");
            if (transformersCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               Map<String, String> mimeTypes = this.mimetypeService.getDisplaysByMimetype();
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

                  this.imageTransformers.add(new SelectItem(id, label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.imageTransformers, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find image-transformers configuration element");
            }
         }
         else
         {
            logger.warn("Could not find Action Wizards configuration section");
         }
      }
      
      return this.imageTransformers;
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
      String viewId = calculateActionViewId(this.action);
      
      HashMap<String, Serializable> actionProps = new HashMap<String, Serializable>(3);
      actionProps.put(PROP_ACTION_NAME, this.action);
      this.currentActionProperties = actionProps;
      
      // determine whether the action being added has any parameters
      ActionDefinition actionDef = this.actionService.getActionDefinition(this.action);
      if (actionDef.hasParameterDefinitions())
      {
         // setup any defaults for the UI and override the viewId if necessary
         String overridenViewId = setupUIDefaultsForAction(actionProps);
         if (overridenViewId != null)
         {
            viewId = overridenViewId;
         }
      }
      else
      {
         // just add the action to the list and use the title as the summary
         actionProps.put(PROP_ACTION_SUMMARY, actionDef.getTitle());
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
      Map actionToEdit = (Map)this.allActionsDataModel.getRowData();
      this.action = (String)actionToEdit.get(PROP_ACTION_NAME);
      this.currentActionProperties = actionToEdit;
      
      // set the flag to show we are editing an action
      this.editingAction = true;
      
      // remember the page we're on
      FacesContext context = FacesContext.getCurrentInstance();
      this.returnViewId = context.getViewRoot().getViewId();
      
      // refresh the wizard
      goToPage(context, calculateActionViewId(this.action));
   }
   
   /**
    * Adds the action just setup by the user to the list of actions for the rule
    */
   public void addAction()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String summary = buildActionSummary();
      
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
      Map actionToRemove = (Map)this.allActionsDataModel.getRowData();
      this.allActionsProperties.remove(actionToRemove);
      
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
         for (String authority : results)
         {
            // first check the authority has not already been added to the list
            boolean alreadyAdded = false;
            for (int i=0; i<emailRecipients.size(); i++)
            {
               RecipientWrapper wrapper = emailRecipients.get(i);
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
               this.emailRecipients.add(wrapper);
            }
         }
      }
   }
   
   /**
    * Action handler called when the Remove icon is pressed to remove an email recipient
    */
   public void removeRecipient(ActionEvent event)
   {
      RecipientWrapper wrapper = (RecipientWrapper)this.emailRecipientsDataModel.getRowData();
      this.emailRecipients.remove(wrapper);
   }
   
   /**
    * Action handler called to insert a template as the email body
    */
   public void insertTemplate(ActionEvent event)
   {
      String template = (String)this.currentActionProperties.get(PROP_TEMPLATE);
      if (template != null && template.equals(TemplateSupportBean.NO_SELECTION) == false)
      {
         // get the content of the template so the user can get a basic preview of it
         try
         {
            NodeRef templateRef = new NodeRef(Repository.getStoreRef(), template);
            ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
            ContentReader reader = cs.getReader(templateRef, ContentModel.PROP_CONTENT);
            if (reader != null && reader.exists())
            {
               this.currentActionProperties.put(PROP_MESSAGE, reader.getContentString());
               
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
      this.currentActionProperties.put(PROP_MESSAGE, "");
      usingTemplate = null;
   }
   
   
   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * Sets the action service
    * 
    * @param actionRegistration  the action service
    */
   public void setActionService(ActionService actionService)
   {
     this.actionService = actionService;
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
   
   /**
    * @param personService      The personService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   /**
    * @param authorityService   The authorityService to set.
    */
   public void setAuthorityService(AuthorityService authorityService)
   {
      this.authorityService = authorityService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   protected String displayLabelForAuthority(String authority)
   {
       String label = authority;
       
       if (this.personService.personExists(authority))
       {
          // create the node ref, then our node representation
          NodeRef ref = personService.getPerson(authority);
          Node node = new Node(ref);
          
          // setup convience function for current user full name
          label = (String)node.getProperties().get(ContentModel.PROP_FIRSTNAME) + ' ' +
                  (String)node.getProperties().get(ContentModel.PROP_LASTNAME);
       }
       
       return label;
   }
   
   /**
    * Sets up any default state required by the UI for collecting the 
    * action settings. The view id to use for the action UI can also
    * be overridden by returing the path to the relevant JSP.
    * 
    * @props The map of properties being used for the current action
    * @return An optional overridden JSP to use for action settings collection
    */
   protected String setupUIDefaultsForAction(HashMap<String, Serializable> props)
   {
      String overridenViewId = null;
      
      if (SimpleWorkflowActionExecuter.NAME.equals(this.action))
      {
         this.currentActionProperties.put("approveAction", "move");
         this.currentActionProperties.put("rejectStepPresent", "yes");
         this.currentActionProperties.put("rejectAction", "move");
      }
      else if (CheckInActionExecuter.NAME.equals(this.action))
      {
         this.currentActionProperties.put(PROP_CHECKIN_MINOR, new Boolean(true));
      }
      
      return overridenViewId;
   }
   
   /**
    * Build the param map for the current Action instance.
    * <p>
    * Based on the params set by the UI, build the params needed to create the action.
    * 
    * @return param map
    */
   protected Map<String, Serializable> buildActionParams()
   {
      // set up parameters maps for the action
      Map<String, Serializable> repoParams = new HashMap<String, Serializable>();
      
      if (AddFeaturesActionExecuter.NAME.equals(this.action))
      {
         QName aspect = Repository.resolveToQName((String)this.currentActionProperties.get(PROP_ASPECT));
         repoParams.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspect);
      }
      else if (RemoveFeaturesActionExecuter.NAME.equals(this.action))
      {
         QName aspect = Repository.resolveToQName((String)this.currentActionProperties.get(PROP_ASPECT));
         repoParams.put(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME, aspect);
      }
      else if (CopyActionExecuter.NAME.equals(this.action))
      {
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         repoParams.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the copy
         // is performed
         repoParams.put(CopyActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         repoParams.put(CopyActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
      }
      else if (MoveActionExecuter.NAME.equals(this.action))
      {
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         repoParams.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the move
         // is performed
         repoParams.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         repoParams.put(MoveActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "move"));
      }
      else if (SimpleWorkflowActionExecuter.NAME.equals(this.action))
      {
         // add the approve step name
         repoParams.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP,
               (String)this.currentActionProperties.get(PROP_APPROVE_STEP_NAME));
         
         // add whether the approve step will copy or move the content
         boolean approveMove = true;
         String approveAction = (String)this.currentActionProperties.get(PROP_APPROVE_ACTION);
         if (approveAction != null && approveAction.equals("copy"))
         {
            approveMove = false;
         }
         
         repoParams.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE, Boolean.valueOf(approveMove));
         
         // add the destination folder of the content
         NodeRef approveDestNodeRef = null;
         Object approveDestNode = this.currentActionProperties.get(PROP_APPROVE_FOLDER);
         if (approveDestNode instanceof NodeRef)
         {
            approveDestNodeRef = (NodeRef)approveDestNode;
         }
         else if (approveDestNode instanceof String)
         {
            approveDestNodeRef = new NodeRef((String)approveDestNode);
         }
         repoParams.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER, approveDestNodeRef);
         
         // determine whether we have a reject step or not
         boolean requireReject = true;
         String rejectStepPresent = (String)this.currentActionProperties.get(PROP_REJECT_STEP_PRESENT);
         if (rejectStepPresent != null && rejectStepPresent.equals("no"))
         {
            requireReject = false;
         }

         if (requireReject)
         {
            // add the reject step name
            repoParams.put(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP,
                  (String)this.currentActionProperties.get(PROP_REJECT_STEP_NAME));
         
            // add whether the reject step will copy or move the content
            boolean rejectMove = true;
            String rejectAction = (String)this.currentActionProperties.get(PROP_REJECT_ACTION);
            if (rejectAction != null && rejectAction.equals("copy"))
            {
               rejectMove = false;
            }
            
            repoParams.put(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE, Boolean.valueOf(rejectMove));
            
            // add the destination folder of the content
            NodeRef rejectDestNodeRef = null;
            Object rejectDestNode = this.currentActionProperties.get(PROP_REJECT_FOLDER);
            if (rejectDestNode instanceof NodeRef)
            {
               rejectDestNodeRef = (NodeRef)rejectDestNode;
            }
            else if (rejectDestNode instanceof String)
            {
               rejectDestNodeRef = new NodeRef((String)rejectDestNode);
            }
            repoParams.put(SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER, rejectDestNodeRef);
         }
      }
      else if (LinkCategoryActionExecuter.NAME.equals(this.action))
      {
         // add the classifiable aspect
         repoParams.put(LinkCategoryActionExecuter.PARAM_CATEGORY_ASPECT,
               ContentModel.ASPECT_GEN_CLASSIFIABLE);
         
         // put the selected category in the action params
         NodeRef catNodeRef = (NodeRef)this.currentActionProperties.get(PROP_CATEGORY);
         repoParams.put(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE, 
               catNodeRef);
      }
      else if (CheckOutActionExecuter.NAME.equals(this.action))
      {
         // specify the location the checked out working copy should go
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         repoParams.put(CheckOutActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the 
         // check out is performed
         repoParams.put(CheckOutActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         repoParams.put(CheckOutActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "checkout"));
      }
      else if (CheckInActionExecuter.NAME.equals(this.action))
      {
         // add the description for the checkin to the action params
         repoParams.put(CheckInActionExecuter.PARAM_DESCRIPTION, 
               this.currentActionProperties.get(PROP_CHECKIN_DESC));
         
         // add the minor change flag
         repoParams.put(CheckInActionExecuter.PARAM_MINOR_CHANGE,
               this.currentActionProperties.get(PROP_CHECKIN_MINOR));
      }
      else if (TransformActionExecuter.NAME.equals(this.action))
      {
         // add the transformer to use
         repoParams.put(TransformActionExecuter.PARAM_MIME_TYPE,
               this.currentActionProperties.get(PROP_TRANSFORMER));
         
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         repoParams.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the copy
         // is performed
         repoParams.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         repoParams.put(TransformActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
      }
      else if (ImageTransformActionExecuter.NAME.equals(this.action))
      {
         // add the transformer to use
         repoParams.put(ImageTransformActionExecuter.PARAM_MIME_TYPE,
               this.currentActionProperties.get(PROP_IMAGE_TRANSFORMER));
         
         // add the options
         repoParams.put(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND, 
               this.currentActionProperties.get(PROP_TRANSFORM_OPTIONS));
         
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         repoParams.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the copy
         // is performed
         repoParams.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         repoParams.put(TransformActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
      }
      else if (MailActionExecuter.NAME.equals(this.action))
      {
         // add the person(s) it's going to as a list of authorities
         List<String> recipients = new ArrayList<String>(emailRecipients.size());
         for (int i=0; i<emailRecipients.size(); i++)
         {
            RecipientWrapper wrapper = emailRecipients.get(i);
            recipients.add(wrapper.authority);
         }
         
         repoParams.put(MailActionExecuter.PARAM_TO_MANY, (Serializable)recipients);
         
         // add the actual email text to send
         repoParams.put(MailActionExecuter.PARAM_TEXT, 
         this.currentActionProperties.get(PROP_MESSAGE));    
             
         // add the subject for the email
         repoParams.put(MailActionExecuter.PARAM_SUBJECT,
               this.currentActionProperties.get(PROP_SUBJECT));
         
         // add the from address
         String from = Application.getClientConfig(FacesContext.getCurrentInstance()).getFromEmailAddress();
         repoParams.put(MailActionExecuter.PARAM_FROM, from);
         
         // add the template if one was selected by the user
         if (this.usingTemplate != null)
         {
            repoParams.put(MailActionExecuter.PARAM_TEMPLATE, new NodeRef(Repository.getStoreRef(), this.usingTemplate));
         }
      }
      else if (ImporterActionExecuter.NAME.equals(this.action))
      {
         // add the encoding
         repoParams.put(ImporterActionExecuter.PARAM_ENCODING, IMPORT_ENCODING);
         
         // add the destination for the import
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         repoParams.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
      }
      else if (SpecialiseTypeActionExecuter.NAME.equals(this.action))
      {
         // add the specialisation type
         String objectType = (String)this.currentActionProperties.get(PROP_OBJECT_TYPE);
         repoParams.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, QName.createQName(objectType));
      }
      else if (ScriptActionExecutor.NAME.equals(this.action))
      {
         // add the selected script noderef to the action properties
         String id = (String)this.currentActionProperties.get(PROP_SCRIPT);
         NodeRef scriptRef = new NodeRef(Repository.getStoreRef(), id);
         repoParams.put(ScriptActionExecutor.PARAM_SCRIPTREF, scriptRef);
         repoParams.put(ScriptActionExecutor.PARAM_SPACEREF, this.navigator.getCurrentNode().getNodeRef());
      }
      
      return repoParams;
   }
   
   /**
    * Returns a summary string for the current action
    * 
    * @return The summary or null if a summary could not be built
    */
   protected String buildActionSummary()
   {
      String summary = null;
      FacesContext context = FacesContext.getCurrentInstance();
      
      if (AddFeaturesActionExecuter.NAME.equals(this.action))
      {
         String label = null;
         String aspect = (String)this.currentActionProperties.get(PROP_ASPECT);
            
         // find the label used by looking through the SelectItem list
         for (SelectItem item : this.getAspects())
         {
            if (item.getValue().equals(aspect))
            {
               label = item.getLabel();
               break;
            }
         }

         summary = MessageFormat.format(Application.getMessage(context, "action_add_features"),
               new Object[] {label});
      }
      else if (RemoveFeaturesActionExecuter.NAME.equals(this.action))
      {
         String label = null;
         String aspect = (String)this.currentActionProperties.get(PROP_ASPECT);
            
         // find the label used by looking through the SelectItem list
         for (SelectItem item : this.getAspects())
         {
            if (item.getValue().equals(aspect))
            {
               label = item.getLabel();
               break;
            }
         }

         summary = MessageFormat.format(Application.getMessage(context, "action_remove_features"),
               new Object[] {label});
      }
      else if (CopyActionExecuter.NAME.equals(this.action))
      {
         NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         String spaceName = Repository.getNameForNode(this.nodeService, space);
         
         summary = MessageFormat.format(Application.getMessage(context, "action_copy"),
               new Object[] {spaceName});
      }
      else if (MoveActionExecuter.NAME.equals(this.action))
      {
         NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         String spaceName = Repository.getNameForNode(this.nodeService, space);
         
         summary = MessageFormat.format(Application.getMessage(context, "action_move"),
               new Object[] {spaceName});
      }
      else if (SimpleWorkflowActionExecuter.NAME.equals(this.action))
      {
         String approveStepName = (String)this.currentActionProperties.get(PROP_APPROVE_STEP_NAME);
         String approveAction = (String)this.currentActionProperties.get(PROP_APPROVE_ACTION);
         NodeRef approveFolder = (NodeRef)this.currentActionProperties.get(PROP_APPROVE_FOLDER);
         String approveFolderName = Repository.getNameForNode(this.nodeService, approveFolder);
         String approveMsg = MessageFormat.format(Application.getMessage(context, "action_simple_workflow"), 
               new Object[] {Application.getMessage(context, approveAction), 
                             approveFolderName, approveStepName});
         
         String rejectMsg = null;
         String rejectStep = (String)this.currentActionProperties.get(PROP_REJECT_STEP_PRESENT);
         if (rejectStep != null && "yes".equals(rejectStep))
         {
            String rejectStepName = (String)this.currentActionProperties.get(PROP_REJECT_STEP_NAME);
            String rejectAction = (String)this.currentActionProperties.get(PROP_REJECT_ACTION);
            NodeRef rejectFolder = (NodeRef)this.currentActionProperties.get(PROP_REJECT_FOLDER);
            String rejectFolderName = Repository.getNameForNode(this.nodeService, rejectFolder);
            rejectMsg = MessageFormat.format(Application.getMessage(context, "action_simple_workflow"), 
               new Object[] {Application.getMessage(context, rejectAction),
                             rejectFolderName, rejectStepName});
         }
         
         StringBuilder builder = new StringBuilder(approveMsg);
         if (rejectMsg != null)
         {
            builder.append(" ");
            builder.append(rejectMsg);
         }
         
         summary = builder.toString();
      }
      else if (LinkCategoryActionExecuter.NAME.equals(this.action))
      {
         NodeRef cat = (NodeRef)this.currentActionProperties.get(PROP_CATEGORY);
         String name = Repository.getNameForNode(this.nodeService, cat);
         
         summary = MessageFormat.format(Application.getMessage(context, "action_link_category"),
               new Object[] {name});
      }
      else if (CheckOutActionExecuter.NAME.equals(this.action))
      {
         NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         String spaceName = Repository.getNameForNode(this.nodeService, space);
         
         summary = MessageFormat.format(Application.getMessage(context, "action_check_out"),
               new Object[] {spaceName});
      }
      else if (CheckInActionExecuter.NAME.equals(this.action))
      {
         String comment = (String)this.currentActionProperties.get(PROP_CHECKIN_DESC);
         Boolean minorChange = (Boolean)this.currentActionProperties.get(PROP_CHECKIN_MINOR);
         String change = null;
         if (minorChange != null && minorChange.booleanValue())
         {
            change = Application.getMessage(context, "minor_change");
         }
         else
         {
            change = Application.getMessage(context, "major_change");
         }
         
         summary = MessageFormat.format(Application.getMessage(context, "action_check_in"),
               new Object[] {change, comment});
      }
      else if (TransformActionExecuter.NAME.equals(this.action))
      {
         String label = null;
         NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         String name = Repository.getNameForNode(this.nodeService, space);
         String transformer = (String)this.currentActionProperties.get(PROP_TRANSFORMER);
         
         // find the label used by looking through the SelectItem list
         for (SelectItem item : this.getTransformers())
         {
            if (item.getValue().equals(transformer))
            {
               label = item.getLabel();
               break;
            }
         }
         
         summary = MessageFormat.format(Application.getMessage(context, "action_transform"),
               new Object[] {name, label});
      }
      else if (ImageTransformActionExecuter.NAME.equals(this.action))
      {
         String label = null;
         NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         String name = Repository.getNameForNode(this.nodeService, space);
         String transformer = (String)this.currentActionProperties.get(PROP_IMAGE_TRANSFORMER);
         String option = (String)this.currentActionProperties.get(PROP_TRANSFORM_OPTIONS);
         
         // find the label used by looking through the SelectItem list
         for (SelectItem item : this.getImageTransformers())
         {
            if (item.getValue().equals(transformer))
            {
               label = item.getLabel();
               break;
            }
         }
         
         summary = MessageFormat.format(Application.getMessage(context, "action_transform_image"),
               new Object[] {name, label, option});
      }
      else if (MailActionExecuter.NAME.equals(this.action))
      {
         String addresses = (String)this.currentActionProperties.get(PROP_TO);
         
         if (addresses == null || addresses.length() == 0)
         {
            if (this.emailRecipients.size() != 0)
            {
               StringBuilder builder = new StringBuilder();
               
               for (int i=0; i<this.emailRecipients.size(); i++)
               {
                  RecipientWrapper wrapper = this.emailRecipients.get(i);
                  if (i != 0)
                  {
                     builder.append(", ");
                  }
                  builder.append(wrapper.getName());
               }
               
               addresses = builder.toString();
            }
         }
         
         summary = MessageFormat.format(Application.getMessage(context, "action_mail"),
               new Object[] {addresses});
      }
      else if (ImporterActionExecuter.NAME.equals(this.action))
      {
         NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         String spaceName = Repository.getNameForNode(this.nodeService, space);
         
         summary = MessageFormat.format(Application.getMessage(context, "action_import"),
               new Object[] {spaceName});
      }
      else if (SpecialiseTypeActionExecuter.NAME.equals(this.action))
      {
         String label = null;
         String objectType = (String)this.currentActionProperties.get(PROP_OBJECT_TYPE);
         for (SelectItem item  : getObjectTypes())
         {
            if (item.getValue().equals(objectType) == true)
            {
               label = item.getLabel();
               break;
            }
         }
            
         summary = MessageFormat.format(Application.getMessage(context, "action_specialise_type"),
               new Object[] {label});
      }
      else if (ScriptActionExecutor.NAME.equals(this.action))
      {
         String id = (String)this.currentActionProperties.get(PROP_SCRIPT);
         NodeRef scriptRef = new NodeRef(Repository.getStoreRef(), id);
         String scriptName = Repository.getNameForNode(this.nodeService, scriptRef);
         
         summary = MessageFormat.format(Application.getMessage(context, "action_script"),
               new Object[] {scriptName});
      }
      else
      {
         // as the default case (i.e. for actions with no parameters) use the title
         ActionDefinition actionDef = this.actionService.getActionDefinition(this.action);
         summary = actionDef.getTitle();
      }
      
      return summary;
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
    * Calculates the viewId for the given action id
    * 
    * @param actionId The id of the action to generate the view id for
    * @return The view id
    */
   protected String calculateActionViewId(String actionId)
   {
      return ACTION_PAGES_LOCATION + actionId + ".jsp";
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Simple wrapper class for email recipient fields
    */
   public static class RecipientWrapper
   {
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
      
      private String name;
      private String authority;
   }
}
