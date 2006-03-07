/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
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
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.cache.ExpiringValueCache;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base handler class containing common code used by the New Space Wizard and New Action Wizard
 * 
 * @author gavinc kevinr
 */
public abstract class BaseActionWizard extends AbstractWizardBean
{
   // parameter names for actions
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
   
   private static final Log logger = LogFactory.getLog(BaseActionWizard.class);
   private static final String IMPORT_ENCODING = "UTF-8";
   
   /** no selection marker for SelectItem lists */
   private static final String NO_SELECTION = "none";
   
   // new rule/action wizard specific properties
   protected boolean multiActionMode = false;
   protected String action;
   protected ActionService actionService;
   protected DictionaryService dictionaryService;
   protected MimetypeService mimetypeService;
   protected List<SelectItem> actions;
   protected List<SelectItem> transformers;
   protected List<SelectItem> imageTransformers;
   protected List<SelectItem> aspects;
   protected List<SelectItem> users;
   protected List<SelectItem> encodings;
   protected Map<String, String> actionDescriptions;
   protected Map<String, Serializable> currentActionProperties;
   protected List<SelectItem> objectTypes;
   /** cache of email templates that last 10 seconds - enough for a couple of page refreshes */
   protected ExpiringValueCache<List<SelectItem>> cachedTemplates = new ExpiringValueCache<List<SelectItem>>(1000*10);
   
   /**
    * Initialises the wizard
    */
   public void init()
   {
      super.init();
      
      this.action = "add-features";
      this.users = null;
      this.actions = null;
      this.actionDescriptions = null;
      
      this.currentActionProperties = new HashMap<String, Serializable>(3);
      
      // default the approve and reject actions
      this.currentActionProperties.put(PROP_APPROVE_ACTION, "move");
      this.currentActionProperties.put(PROP_REJECT_STEP_PRESENT, "yes");
      this.currentActionProperties.put(PROP_REJECT_ACTION, "move");
      
      // default the checkin minor change
      this.currentActionProperties.put(PROP_CHECKIN_MINOR, new Boolean(true));
   }
   
   /**
    * Build the param map for the current Action instance
    * 
    * @return param map
    */
   protected Map<String, Serializable> buildActionParams()
   {
      // set up parameters maps for the action
      Map<String, Serializable> actionParams = new HashMap<String, Serializable>();
      
      if (this.action.equals(AddFeaturesActionExecuter.NAME))
      {
         QName aspect = Repository.resolveToQName((String)this.currentActionProperties.get(PROP_ASPECT));
         actionParams.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspect);
      }
      else if (this.action.equals(CopyActionExecuter.NAME))
      {
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         actionParams.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the copy
         // is performed
         actionParams.put(CopyActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         actionParams.put(CopyActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
      }
      else if (this.action.equals(MoveActionExecuter.NAME))
      {
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         actionParams.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the move
         // is performed
         actionParams.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         actionParams.put(MoveActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "move"));
      }
      else if (this.action.equals(SimpleWorkflowActionExecuter.NAME))
      {
         // add the approve step name
         actionParams.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP,
               (String)this.currentActionProperties.get(PROP_APPROVE_STEP_NAME));
         
         // add whether the approve step will copy or move the content
         boolean approveMove = true;
         String approveAction = (String)this.currentActionProperties.get(PROP_APPROVE_ACTION);
         if (approveAction != null && approveAction.equals("copy"))
         {
            approveMove = false;
         }
         
         actionParams.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE, Boolean.valueOf(approveMove));
         
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
         actionParams.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER, approveDestNodeRef);
         
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
            actionParams.put(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP,
                  (String)this.currentActionProperties.get(PROP_REJECT_STEP_NAME));
         
            // add whether the reject step will copy or move the content
            boolean rejectMove = true;
            String rejectAction = (String)this.currentActionProperties.get(PROP_REJECT_ACTION);
            if (rejectAction != null && rejectAction.equals("copy"))
            {
               rejectMove = false;
            }
            
            actionParams.put(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE, Boolean.valueOf(rejectMove));
            
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
            actionParams.put(SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER, rejectDestNodeRef);
         }
      }
      else if (this.action.equals(LinkCategoryActionExecuter.NAME))
      {
         // add the classifiable aspect
         actionParams.put(LinkCategoryActionExecuter.PARAM_CATEGORY_ASPECT,
               ContentModel.ASPECT_GEN_CLASSIFIABLE);
         
         // put the selected category in the action params
         NodeRef catNodeRef = (NodeRef)this.currentActionProperties.get(PROP_CATEGORY);
         actionParams.put(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE, 
               catNodeRef);
      }
      else if (this.action.equals(CheckOutActionExecuter.NAME))
      {
         // specify the location the checked out working copy should go
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         actionParams.put(CheckOutActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the 
         // check out is performed
         actionParams.put(CheckOutActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         actionParams.put(CheckOutActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "checkout"));
      }
      else if (this.action.equals(CheckInActionExecuter.NAME))
      {
         // add the description for the checkin to the action params
         actionParams.put(CheckInActionExecuter.PARAM_DESCRIPTION, 
               this.currentActionProperties.get(PROP_CHECKIN_DESC));
         
         // add the minor change flag
         actionParams.put(CheckInActionExecuter.PARAM_MINOR_CHANGE,
               this.currentActionProperties.get(PROP_CHECKIN_MINOR));
      }
      else if (this.action.equals(TransformActionExecuter.NAME))
      {
         // add the transformer to use
         actionParams.put(TransformActionExecuter.PARAM_MIME_TYPE,
               this.currentActionProperties.get(PROP_TRANSFORMER));
         
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         actionParams.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the copy
         // is performed
         actionParams.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         actionParams.put(TransformActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
      }
      else if (this.action.equals(ImageTransformActionExecuter.NAME))
      {
         // add the transformer to use
         actionParams.put(ImageTransformActionExecuter.PARAM_MIME_TYPE,
               this.currentActionProperties.get(PROP_IMAGE_TRANSFORMER));
         
         // add the options
         actionParams.put(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND, 
               this.currentActionProperties.get(PROP_TRANSFORM_OPTIONS));
         
         // add the destination space id to the action properties
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         actionParams.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
         
         // add the type and name of the association to create when the copy
         // is performed
         actionParams.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
               ContentModel.ASSOC_CONTAINS);
         actionParams.put(TransformActionExecuter.PARAM_ASSOC_QNAME, 
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
      }
      else if (this.action.equals(MailActionExecuter.NAME))
      {
         // add the actual email text to send
         actionParams.put(MailActionExecuter.PARAM_TEXT, 
               this.currentActionProperties.get(PROP_MESSAGE));
            
         // add the person it's going to
         actionParams.put(MailActionExecuter.PARAM_TO, 
               this.currentActionProperties.get(PROP_TO));
         
         // add the subject for the email
         actionParams.put(MailActionExecuter.PARAM_SUBJECT,
               this.currentActionProperties.get(PROP_SUBJECT));
         
         // add the from address
         String from = Application.getClientConfig(FacesContext.getCurrentInstance()).getFromEmailAddress();
         actionParams.put(MailActionExecuter.PARAM_FROM, from);
         
         // add the template if one was selected by the user
         String template = (String)this.currentActionProperties.get(PROP_TEMPLATE);
         if (template != null && template.equals(NO_SELECTION) == false)
         {
            actionParams.put(MailActionExecuter.PARAM_TEMPLATE, new NodeRef(Repository.getStoreRef(), template));
         }
      }
      else if (this.action.equals(ImporterActionExecuter.NAME))
      {
         // add the encoding
         actionParams.put(ImporterActionExecuter.PARAM_ENCODING, IMPORT_ENCODING);
         
         // add the destination for the import
         NodeRef destNodeRef = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
         actionParams.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
      }
      else if (this.action.equals(SpecialiseTypeActionExecuter.NAME) == true)
      {
          // add the specialisation type
          String objectType = (String)this.currentActionProperties.get(PROP_OBJECT_TYPE);
          actionParams.put(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, QName.createQName(objectType));
      }
      
      return actionParams;
   }
   
   /**
    * Populate the actionProperties member variable with correct props for the current action
    * using the supplied property map.
    * 
    * @param actionProps Map to retrieve props appropriate to the current action from
    */
   protected void populateActionFromProperties(Map<String, Serializable> actionProps)
   {
      if (this.action.equals(AddFeaturesActionExecuter.NAME))
      {
         QName aspect = (QName)actionProps.get(AddFeaturesActionExecuter.PARAM_ASPECT_NAME);
         this.currentActionProperties.put(PROP_ASPECT, aspect.toString());
      }
      else if (this.action.equals(CopyActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(MoveActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(SimpleWorkflowActionExecuter.NAME))
      {
         String approveStep = (String)actionProps.get(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP);
         Boolean approveMove = (Boolean)actionProps.get(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE);
         NodeRef approveFolderNode = (NodeRef)actionProps.get(
               SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER);
         
         String rejectStep = (String)actionProps.get(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP);
         Boolean rejectMove = (Boolean)actionProps.get(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE);
         NodeRef rejectFolderNode = (NodeRef)actionProps.get(
               SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER);
         
         this.currentActionProperties.put(PROP_APPROVE_STEP_NAME, approveStep);
         this.currentActionProperties.put(PROP_APPROVE_ACTION, approveMove ? "move" : "copy");
         this.currentActionProperties.put(PROP_APPROVE_FOLDER, approveFolderNode);
         
         if (rejectStep == null && rejectMove == null && rejectFolderNode == null)
         {
            this.currentActionProperties.put(PROP_REJECT_STEP_PRESENT, "no");
         }
         else
         {
            this.currentActionProperties.put(PROP_REJECT_STEP_PRESENT, "yes");
            this.currentActionProperties.put(PROP_REJECT_STEP_NAME, rejectStep);
            this.currentActionProperties.put(PROP_REJECT_ACTION, rejectMove ? "move" : "copy");
            this.currentActionProperties.put(PROP_REJECT_FOLDER, rejectFolderNode);
         }
      }
      else if (this.action.equals(LinkCategoryActionExecuter.NAME))
      {
         NodeRef catNodeRef = (NodeRef)actionProps.get(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE);
         this.currentActionProperties.put(PROP_CATEGORY, catNodeRef);
      }
      else if (this.action.equals(CheckOutActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(CheckOutActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(CheckInActionExecuter.NAME))
      {
         String checkDesc = (String)actionProps.get(CheckInActionExecuter.PARAM_DESCRIPTION);
         this.currentActionProperties.put(PROP_CHECKIN_DESC, checkDesc);
         
         Boolean minorChange = (Boolean)actionProps.get(CheckInActionExecuter.PARAM_MINOR_CHANGE);
         this.currentActionProperties.put(PROP_CHECKIN_MINOR, minorChange);
      }
      else if (this.action.equals(TransformActionExecuter.NAME))
      {
         String transformer = (String)actionProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
         this.currentActionProperties.put(PROP_TRANSFORMER, transformer);
         
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(ImageTransformActionExecuter.NAME))
      {
         String transformer = (String)actionProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
         this.currentActionProperties.put(PROP_IMAGE_TRANSFORMER, transformer);
         
         String options = (String)actionProps.get(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND);
         this.currentActionProperties.put(PROP_TRANSFORM_OPTIONS, options != null ? options : "");
         
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(MailActionExecuter.NAME))
      {
         String subject = (String)actionProps.get(MailActionExecuter.PARAM_SUBJECT);
         this.currentActionProperties.put(PROP_SUBJECT, subject);
         
         String message = (String)actionProps.get(MailActionExecuter.PARAM_TEXT);
         this.currentActionProperties.put(PROP_MESSAGE, message);
         
         String to = (String)actionProps.get(MailActionExecuter.PARAM_TO);
         this.currentActionProperties.put(PROP_TO, to);
      }
      else if (this.action.equals(ImporterActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(ImporterActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(SpecialiseTypeActionExecuter.NAME) == true)
      {
          QName specialiseType = (QName)actionProps.get(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME);
          this.currentActionProperties.put(PROP_OBJECT_TYPE, specialiseType.toString());
      }
   }

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
    * Sets the action service
    * 
    * @param actionRegistration  the action service
    */
   public void setActionService(ActionService actionService)
   {
	  this.actionService = actionService;
   }
   
   /**
    * Sets the dictionary service
    * 
    * @param dictionaryService  the dictionary service
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }
   
   /**
    * Sets the mimetype service
    * 
    * @param mimetypeService  The mimetype service
    */
   public void setMimetypeService(MimetypeService mimetypeService)
   {
      this.mimetypeService = mimetypeService;
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
      }
      
      return this.actions;
   }
   
   /**
    * @return Returns a map of all the action descriptions 
    */
   public Map<String, String> getActionDescriptions()
   {
      if (this.actionDescriptions == null)
      {
         List<ActionDefinition> ruleActions = this.actionService.getActionDefinitions();
         this.actionDescriptions = new HashMap<String, String>();
         for (ActionDefinition ruleActionDef : ruleActions)
         {
            this.actionDescriptions.put(ruleActionDef.getName(), ruleActionDef.getDescription());
         }
      }
      
      return this.actionDescriptions;
   }

   /**
    * @return Gets the action settings
    */
   public Map<String, Serializable> getActionProperties()
   {
      return this.currentActionProperties;
   }
   
   /**
    * Returns a list of encodings the import and export actions can use
    * 
    * @return List of SelectItem objects representing the available encodings
    */
   public List<SelectItem> getEncodings()
   {
      if (this.encodings == null)
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement encodingsCfg = wizardCfg.getConfigElement("encodings");
            if (encodingsCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               this.encodings = new ArrayList<SelectItem>();
               for (ConfigElement child : encodingsCfg.getChildren())
               {
                  String id = child.getAttribute("name");
                  
                  // look for a client localized string
                  String label = null;
                  String msgId = child.getAttribute("displayLabelId");
                  if (msgId != null)
                  {
                     label = Application.getMessage(context, msgId);
                  }
                  
                  // if there wasn't an externalized string look for one in the config
                  if (label == null)
                  {
                     label = child.getAttribute("displayLabel");
                  }
                  
                  this.encodings.add(new SelectItem(id, label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.encodings, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find encodings configuration element");
            }
         }
         else
         {
            logger.warn("Could not find Action Wizards configuration section");
         }
      }
      
      return this.encodings;
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
                  
                  // look for a client localized string
                  String label = null;
                  String msgId = child.getAttribute("displayLabelId");
                  if (msgId != null)
                  {
                     label = Application.getMessage(context, msgId);
                  }
                  
                  // if there wasn't an externalized string look for one in the config
                  if (label == null)
                  {
                     label = child.getAttribute("displayLabel");
                  }

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
                  
                  // look for a client localized string
                  String label = null;
                  String msgId = child.getAttribute("displayLabelId");
                  if (msgId != null)
                  {
                     label = Application.getMessage(context, msgId);
                  }
                  
                  // if there wasn't an externalized string look for one in the config
                  if (label == null)
                  {
                     label = child.getAttribute("displayLabel");
                  }

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

                  // look for a client localized string
                  String label = null;
                  String msgId = child.getAttribute("displayLabelId");
                  if (msgId != null)
                  {
                     label = Application.getMessage(context, msgId);
                  }
                  
                  // if there wasn't an externalized string look for one in the config
                  if (label == null)
                  {
                     label = child.getAttribute("displayLabel");
                  }

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
                     // look for a client localized string
                     String label = null;
                     String msgId = child.getAttribute("displayLabelId");
                     if (msgId != null)
                     {
                        label = Application.getMessage(context, msgId);
                     }
                     
                     // if there wasn't an externalized string look for one in the config
                     if (label == null)
                     {
                        label = child.getAttribute("displayLabel");
                     }
   
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
    * @return the list of available Content Templates that can be applied to the current document.
    */
   public List<SelectItem> getTemplates()
   {
      List<SelectItem> templates = cachedTemplates.get();
      if (templates == null)
      {
         // get the template from the special Content Templates folder
         FacesContext context = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(context) + "/" + 
               Application.getGlossaryFolderName(context) + "/" +
               Application.getEmailTemplatesFolderName(context) + "//*";
         try
         {
            NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
            NamespaceService resolver = Repository.getServiceRegistry(context).getNamespaceService();
            List<NodeRef> results = this.searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
            
            templates = new ArrayList<SelectItem>(results.size() + 1);
            if (results.size() != 0)
            {
               DictionaryService dd = Repository.getServiceRegistry(context).getDictionaryService();
               for (NodeRef ref : results)
               {
                  if (nodeService.exists(ref) == true)
                  {
                     Node childNode = new Node(ref);
                     if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT))
                     {
                        templates.add(new SelectItem(childNode.getId(), childNode.getName()));
                     }
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(templates, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
         }
         catch (AccessDeniedException accessErr)
         {
            // ignore the result if we cannot access the root
         }
         
         // add an entry (at the start) to instruct the user to select a template
         if (templates == null)
         {
            templates = new ArrayList<SelectItem>(1);
         }
         templates.add(0, new SelectItem(NO_SELECTION, Application.getMessage(FacesContext.getCurrentInstance(), "select_a_template")));
         
         cachedTemplates.put(templates);
      }
      
      return templates;
   }
}
