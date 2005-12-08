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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.description.UIDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Handler class used by the New Space Wizard 
 * 
 * @author gavinc
 */
public class NewSpaceWizard extends AbstractWizardBean
{
   public static final String SPACE_ICON_DEFAULT = "space-icon-default";
   public static final String FORUMS_ICON_DEFAULT = "forums_large";

   private static Log logger = LogFactory.getLog(NewSpaceWizard.class);
   
   // TODO: retrieve these from the config service
   private static final String WIZARD_TITLE_ID = "new_space_title";
   private static final String WIZARD_DESC_ID = "new_space_desc";
   private static final String STEP1_TITLE_ID = "new_space_step1_title";
   private static final String STEP1_DESCRIPTION_ID = "new_space_step1_desc";
   private static final String STEP2_TITLE_ID = "new_space_step2_title";
   private static final String STEP2_DESCRIPTION_ID = "new_space_step2_desc";
   private static final String STEP3_TITLE_ID = "new_space_step3_title";
   private static final String STEP3_DESCRIPTION_ID = "new_space_step3_desc";
   private static final String FINISH_INSTRUCTION_ID = "new_space_finish_instruction";
   
   private static final String ERROR = "error_space";
   private static final String DEFAULT_SPACE_TYPE_ICON = "/images/icons/space.gif";

   // new space wizard specific properties
   private SearchService searchService;
   private DictionaryService dictionaryService;
   
   protected String spaceType;
   protected String icon;
   protected String createFrom;
   protected NodeRef existingSpaceId;
   protected String templateSpaceId;
   protected String copyPolicy;
   protected String name;
   protected String description;
   protected String templateName;
   protected boolean saveAsTemplate;
   protected List<SelectItem> templates;
   protected List<UIListItem> folderTypes;
   protected List<UIListItem> genericIcons;
   protected List<UIListItem> forumsIcons;
   protected List<UIDescription> folderTypeDescriptions;
   
   // the NodeRef of the node created during finish
   protected NodeRef createdNode;
   
   /**
    * Deals with the finish button being pressed
    * 
    * @return outcome
    */
   public String finish()
   {
      String outcome = FINISH_OUTCOME;
      
      UserTransaction tx = null;
   
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();

         if (this.editMode)
         {
            // update the existing node in the repository
            Node currentSpace = this.browseBean.getActionSpace();
            NodeRef nodeRef = currentSpace.getNodeRef();
            
            // rename if necessary
            fileFolderService.rename(nodeRef, this.name);
            
            // update the properties
            Map<QName, Serializable> properties = this.nodeService.getProperties(nodeRef);
            properties.put(ContentModel.PROP_NAME, this.name);
            properties.put(ContentModel.PROP_ICON, this.icon);
            properties.put(ContentModel.PROP_DESCRIPTION, this.description);
            
            // apply properties
            this.nodeService.setProperties(nodeRef, properties);
         }
         else
         {
            String newSpaceId = null;
            
            if (this.createFrom.equals("scratch"))
            {
               // create the space (just create a folder for now)
               NodeRef parentNodeRef;
               String nodeId = getNavigator().getCurrentNodeId();
               if (nodeId == null)
               {
                  parentNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
               }
               else
               {
                  parentNodeRef = new NodeRef(Repository.getStoreRef(), nodeId);
               }
               
               FileInfo fileInfo = fileFolderService.create(
                     parentNodeRef,
                     this.name,
                     Repository.resolveToQName(this.spaceType));
               NodeRef nodeRef = fileInfo.getNodeRef();
               newSpaceId = nodeRef.getId();
               
               if (logger.isDebugEnabled())
                  logger.debug("Created folder node with name: " + this.name);

               // apply the uifacets aspect - icon, title and description props
               Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
               uiFacetsProps.put(ContentModel.PROP_ICON, this.icon);
               uiFacetsProps.put(ContentModel.PROP_TITLE, this.name);
               uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
               this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, uiFacetsProps);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);
               
               // remember the created node
               this.createdNode = nodeRef;
            }
            else if (this.createFrom.equals("existing"))
            {
               // copy the selected space and update the name, description and icon
               NodeRef sourceNode = this.existingSpaceId;
               NodeRef parentSpace = new NodeRef(Repository.getStoreRef(), getNavigator().getCurrentNodeId());
               
               // copy from existing
               NodeRef copiedNode = this.fileFolderService.copy(sourceNode, parentSpace, this.name).getNodeRef(); 
               
               // also need to set the new description and icon properties
               this.nodeService.setProperty(copiedNode, ContentModel.PROP_DESCRIPTION, this.description);
               this.nodeService.setProperty(copiedNode, ContentModel.PROP_ICON, this.icon);
               
               newSpaceId = copiedNode.getId();
                  
               if (logger.isDebugEnabled())
                  logger.debug("Copied space with id of " + sourceNode.getId() + " to " + this.name);
               
               // remember the created node
               this.createdNode = copiedNode;
            }
            else if (this.createFrom.equals("template"))
            {
               // copy the selected space and update the name, description and icon
               NodeRef sourceNode = new NodeRef(Repository.getStoreRef(), this.templateSpaceId);
               NodeRef parentSpace = new NodeRef(Repository.getStoreRef(), getNavigator().getCurrentNodeId());
               // copy from the template
               NodeRef copiedNode = this.fileFolderService.copy(sourceNode, parentSpace, this.name).getNodeRef();
               // also need to set the new description and icon properties
               this.nodeService.setProperty(copiedNode, ContentModel.PROP_DESCRIPTION, this.description);
               this.nodeService.setProperty(copiedNode, ContentModel.PROP_ICON, this.icon);
               
               newSpaceId = copiedNode.getId();
               
               if (logger.isDebugEnabled())
                  logger.debug("Copied template space with id of " + sourceNode.getId() + " to " + this.name);
               
               // remember the created node
               this.createdNode = copiedNode;
            }
            
            // if the user selected to save the space as a template space copy the new
            // space to the templates folder
            if (this.saveAsTemplate)
            {
               // get hold of the Templates node
               DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
               namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);
               
               String xpath = Application.getRootPath(FacesContext.getCurrentInstance()) + "/" +
                     Application.getGlossaryFolderName(FacesContext.getCurrentInstance()) + "/" +
                     Application.getSpaceTemplatesFolderName(FacesContext.getCurrentInstance());
               
               NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
               List<NodeRef> templateNodeList = this.searchService.selectNodes(
                     rootNodeRef,
                     xpath, null, namespacePrefixResolver, false);
               if (templateNodeList.size() == 1)
               {
                  // get the first item in the list as we from test above there is only one!
                  NodeRef templateNode = templateNodeList.get(0);
                  NodeRef sourceNode = new NodeRef(Repository.getStoreRef(), newSpaceId);
                  // copy this to the template location
                  fileFolderService.copy(sourceNode, templateNode, this.templateName);
               }
            }
         }
         
         // give subclasses a chance to perform custom processing before committing
         performCustomProcessing(context);
         
         // commit the transaction
         tx.commit();
         
         // now we know the new details are in the repository, reset the
         // client side node representation so the new details are retrieved
         String statusMsg = null;
         if (this.editMode)
         {
            this.browseBean.getActionSpace().reset();
            statusMsg = MessageFormat.format(Application.getMessage(context, "status_space_updated"), 
                  new Object[]{this.name});
         }
         else
         {
            // add a message to inform the user that the creation was OK
            statusMsg = MessageFormat.format(Application.getMessage(context, "status_space_created"), 
                  new Object[]{this.name});
         }
         
         // add the status message
         Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, statusMsg);
      }
      catch (FileExistsException e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         // print status message  
         String statusMsg = MessageFormat.format(
               Application.getMessage(
                     FacesContext.getCurrentInstance(), "error_exists"), 
                     e.getExisting().getName());
         Utils.addErrorMessage(statusMsg);
         // no outcome
         outcome = null;
      }
      catch (Exception e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardDescription()
    */
   public String getWizardDescription()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_DESC_ID);
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardTitle()
    */
   public String getWizardTitle()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_TITLE_ID);
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepDescription()
    */
   public String getStepDescription()
   {
      String stepDesc = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), STEP1_DESCRIPTION_ID);
            break;
         }
         case 2:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), STEP2_DESCRIPTION_ID);
            break;
         }
         case 3:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), STEP3_DESCRIPTION_ID);
            break;
         }
         case 4:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), SUMMARY_DESCRIPTION_ID);
            break;
         }
         default:
         {
            stepDesc = "";
         }
      }
      
      return stepDesc;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepTitle()
    */
   public String getStepTitle()
   {
      String stepTitle = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP1_TITLE_ID);
            break;
         }
         case 2:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP2_TITLE_ID);
            break;
         }
         case 3:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP3_TITLE_ID);
            break;
         }
         case 4:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), SUMMARY_TITLE_ID);
            break;
         }
         default:
         {
            stepTitle = "";
         }
      }
      
      return stepTitle;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepInstructions()
    */
   public String getStepInstructions()
   {
      String stepInstruction = null;
      
      switch (this.currentStep)
      {
         case 4:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), FINISH_INSTRUCTION_ID);
            break;
         }
         default:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), DEFAULT_INSTRUCTION_ID);
         }
      }
      
      return stepInstruction;
   }
   
   /**
    * Initialises the wizard
    */
   public void init()
   {
      super.init();
      
      // clear the cached query results
      if (this.templates != null)
      {
         this.templates.clear();
         this.templates = null;
      }
      
      // reset all variables
      this.createFrom = "scratch";
      this.spaceType = ContentModel.TYPE_FOLDER.toString();
      this.icon = SPACE_ICON_DEFAULT;
      this.copyPolicy = "contents";
      this.existingSpaceId = null;
      this.templateSpaceId = null;
      this.name = null;
      this.description = "";
      this.templateName = null;
      this.saveAsTemplate = false;  
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#populate()
    */
   public void populate()
   {
      // get hold of the current node and populate the appropriate values
      Node currentSpace = browseBean.getActionSpace();
      Map<String, Object> props = currentSpace.getProperties();
      
      this.name = (String)props.get("name");
      this.description = (String)props.get("description");
      this.icon = (String)props.get("app:icon");
   }

   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      String summaryCreateType = null;
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      if (this.createFrom.equals("scratch"))
      {
         summaryCreateType = bundle.getString("scratch");
      }
      else if (this.createFrom.equals("existing"))
      {
         summaryCreateType = bundle.getString("an_existing_space");
      }
      else if (this.createFrom.equals("template"))
      {
         summaryCreateType = bundle.getString("a_template");
      }
      
//      String summarySaveAsTemplate = this.saveAsTemplate ? bundle.getString("yes") : bundle.getString("no");
//      bundle.getString("save_as_template"), bundle.getString("template_name")},
//      summarySaveAsTemplate, this.templateName
      
      String spaceTypeLabel = null;
      for (UIListItem item : this.getFolderTypes())
      {
         if (item.getValue().equals(this.spaceType))
         {
            spaceTypeLabel = item.getLabel();
            break;
         }
      }
      
      return buildSummary(
            new String[] {bundle.getString("space_type"), bundle.getString("name"), 
                          bundle.getString("description"), bundle.getString("creating_from")},
            new String[] {spaceTypeLabel, this.name, this.description, summaryCreateType});
   }
   
   /**
    * @return Returns a list of template spaces currently in the system
    */
   public List<SelectItem> getTemplateSpaces()
   {      
      if (this.templates == null)
      {
         this.templates = new ArrayList<SelectItem>();
         
         FacesContext context = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(context) + "/" + Application.getGlossaryFolderName(context) +
               "/" + Application.getSpaceTemplatesFolderName(context) + "/*";
         NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
         NamespaceService resolver = Repository.getServiceRegistry(context).getNamespaceService();
         List<NodeRef> results = this.searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
         
         if (results.size() > 0)
         {
            for (NodeRef assocRef : results)
            {
               Node childNode = new Node(assocRef);
               this.templates.add(new SelectItem(childNode.getId(), childNode.getName()));
            }
            
            // make sure the list is sorted by the label
            QuickSort sorter = new QuickSort(this.templates, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
            sorter.sort();
         }
         
         // add an entry (at the start) to instruct the user to select a template
         this.templates.add(0, new SelectItem("none", Application.getMessage(FacesContext.getCurrentInstance(), "select_a_template")));
      }
      
      return this.templates;
   }

   /**
    * Returns a list of UIListItem objects representing the folder types 
    * and also constructs the list of descriptions for each type
    * 
    * @return List of UIListItem components
    */
   @SuppressWarnings("unchecked")
   public List<UIListItem> getFolderTypes()
   {
      if (this.folderTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         this.folderTypes = new ArrayList<UIListItem>(2);
         this.folderTypeDescriptions = new ArrayList<UIDescription>(2);
         
         // add the well known 'container space' type to start with
         UIListItem defaultItem = new UIListItem();
         String defaultLabel = Application.getMessage(context, "container");
         defaultItem.setValue(ContentModel.TYPE_FOLDER.toString());
         defaultItem.setLabel(defaultLabel);
         defaultItem.setTooltip(defaultLabel);
         defaultItem.getAttributes().put("image", DEFAULT_SPACE_TYPE_ICON);
         this.folderTypes.add(defaultItem);
         
         UIDescription defaultDesc = new UIDescription();
         defaultDesc.setControlValue(ContentModel.TYPE_FOLDER.toString());
         defaultDesc.setText(Application.getMessage(context, "container_desc"));
         this.folderTypeDescriptions.add(defaultDesc);
         
         // add any configured content sub-types to the list
         ConfigService svc = (ConfigService)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(Application.BEAN_CONFIG_SERVICE);
         Config wizardCfg = svc.getConfig("Custom Folder Types");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("folder-types");
            if (typesCfg != null)
            {               
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));
                  TypeDefinition typeDef = this.dictionaryService.getType(idQName);
                  
                  if (typeDef != null &&
                      this.dictionaryService.isSubClass(typeDef.getName(), ContentModel.TYPE_FOLDER))
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
                     
                     // finally use the localname if we still haven't found a label
                     if (label == null)
                     {
                        label = idQName.getLocalName();
                     }
                     
                     // resolve a description string for the type
                     String description = null;
                     msgId = child.getAttribute("descriptionMsgId");
                     if (msgId != null)
                     {
                        description = Application.getMessage(context, msgId);
                     }
                     
                     if (description == null)
                     {
                        description = child.getAttribute("description");
                     }
                     
                     // if we don't have a local description just use the label
                     if (description == null)
                     {
                        description = label;
                     }
                     
                     // extract the icon to use from the config
                     String icon = child.getAttribute("icon");
                     if (icon == null || icon.length() == 0)
                     {
                        icon = DEFAULT_SPACE_TYPE_ICON;
                     }
                     
                     UIListItem item = new UIListItem();
                     item.getAttributes().put("value", idQName.toString());
                     item.getAttributes().put("label", label);
                     item.getAttributes().put("tooltip", label);
                     item.getAttributes().put("image", icon);
                     this.folderTypes.add(item);
                     
                     UIDescription desc = new UIDescription();
                     desc.setControlValue(idQName.toString());
                     desc.setText(description);
                     this.folderTypeDescriptions.add(desc);
                  }
               }
            }
            else
            {
               logger.warn("Could not find 'folder-types' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Custom Folder Types' configuration section");
         }
         
      }
      
      return this.folderTypes;
   }
   
   /**
    * Returns a list of UIDescription objects for the folder types
    * 
    * @return A list of UIDescription objects
    */
   public List<UIDescription> getFolderTypeDescriptions()
   {
      if (this.folderTypeDescriptions == null)
      {
         // call the getFolderType method to construct the list
         getFolderTypes();
      }
      
      return this.folderTypeDescriptions;
   }
   
   /**
    * Returns a list of icons to allow the user to select from.
    * The list can change according to the type of space being created.
    * 
    * @return A list of icons
    */
   @SuppressWarnings("unchecked")
   public List<UIListItem> getIcons()
   {
      // TODO: Drive the list of icons to show for each space type from the config
      //       this will then remove the dependency on forums from this generic 
      //       class
      
      List<UIListItem> icons = null;
      
      if (this.spaceType.equals(ForumModel.TYPE_FORUMS.toString()))
      {
         // return the various forum icons
         if (this.forumsIcons == null)
         {
            this.forumsIcons = new ArrayList<UIListItem>(2);
            
            // change default icon to be forums
            this.icon = FORUMS_ICON_DEFAULT;
            
            UIListItem item = new UIListItem();
            item.setValue(FORUMS_ICON_DEFAULT);
            item.getAttributes().put("image", "/images/icons/forums_large.gif");
            this.forumsIcons.add(item);
         }
         
         icons = this.forumsIcons;
      }
      else
      {
         // return the generic space icons
         if (this.genericIcons == null)
         {
            this.genericIcons = new ArrayList<UIListItem>(6);
            
            // change default icon
            this.icon = SPACE_ICON_DEFAULT;
            
            UIListItem item = new UIListItem();
            item.setValue("space-icon-default");
            item.getAttributes().put("image", "/images/icons/space-icon-default.gif");
            this.genericIcons.add(item);
            
            item = new UIListItem();
            item.setValue("space-icon-star");
            item.getAttributes().put("image", "/images/icons/space-icon-star.gif");
            this.genericIcons.add(item);
            
            item = new UIListItem();
            item.setValue("space-icon-doc");
            item.getAttributes().put("image", "/images/icons/space-icon-doc.gif");
            this.genericIcons.add(item);
            
            item = new UIListItem();
            item.setValue("space-icon-pen");
            item.getAttributes().put("image", "/images/icons/space-icon-pen.gif");
            this.genericIcons.add(item);
            
            item = new UIListItem();
            item.setValue("space-icon-cd");
            item.getAttributes().put("image", "/images/icons/space-icon-cd.gif");
            this.genericIcons.add(item);
            
            item = new UIListItem();
            item.setValue("space-icon-image");
            item.getAttributes().put("image", "/images/icons/space-icon-image.gif");
            this.genericIcons.add(item);
         }
         
         icons = this.genericIcons;
      }
      
      return icons;
   }
   
   /**
    * @return Returns the searchService.
    */
   public SearchService getSearchService()
   {
      return searchService;
   }

   /**
    * @param searchService The searchService to set.
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
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
    * @return Returns the copyPolicy.
    */
   public String getCopyPolicy()
   {
      return copyPolicy;
   }

   /**
    * @param copyPolicy The copyPolicy to set.
    */
   public void setCopyPolicy(String copyPolicy)
   {
      this.copyPolicy = copyPolicy;
   }
   
   /**
    * @return Returns the createFrom.
    */
   public String getCreateFrom()
   {
      return createFrom;
   }

   /**
    * @param createFrom The createFrom to set.
    */
   public void setCreateFrom(String createFrom)
   {
      this.createFrom = createFrom;
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   } 

   /**
    * @return Returns the existingSpaceId.
    */
   public NodeRef getExistingSpaceId()
   {
      return existingSpaceId;
   }
   
   /**
    * @param existingSpaceId The existingSpaceId to set.
    */
   public void setExistingSpaceId(NodeRef existingSpaceId)
   {
      this.existingSpaceId = existingSpaceId;
   }
   
   /**
    * @return Returns the icon.
    */
   public String getIcon()
   {
      return icon;
   }
   
   /**
    * @param icon The icon to set.
    */
   public void setIcon(String icon)
   {
      this.icon = icon;
   }
   
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the saveAsTemplate.
    */
   public boolean isSaveAsTemplate()
   {
      return saveAsTemplate;
   }
   
   /**
    * @param saveAsTemplate The saveAsTemplate to set.
    */
   public void setSaveAsTemplate(boolean saveAsTemplate)
   {
      this.saveAsTemplate = saveAsTemplate;
   }

   /**
    * @return Returns the spaceType.
    */
   public String getSpaceType()
   {
      return spaceType;
   }
   
   /**
    * @param spaceType The spaceType to set.
    */
   public void setSpaceType(String spaceType)
   {
      this.spaceType = spaceType;
   }
   
   /**
    * @return Returns the templateName.
    */
   public String getTemplateName()
   {
      return templateName;
   }
   
   /**
    * @param templateName The templateName to set.
    */
   public void setTemplateName(String templateName)
   {
      this.templateName = templateName;
   }
   
   /**
    * @return Returns the templateSpaceId.
    */
   public String getTemplateSpaceId()
   {
      return templateSpaceId;
   }
   
   /**
    * @param templateSpaceId The templateSpaceId to set.
    */
   public void setTemplateSpaceId(String templateSpaceId)
   {
      this.templateSpaceId = templateSpaceId;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#determineOutcomeForStep(int)
    */
   protected String determineOutcomeForStep(int step)
   {
      String outcome = null;
      
      switch(step)
      {
         case 1:
         {
            outcome = "create-from";
            break;
         }
         case 2:
         {
            if (createFrom.equalsIgnoreCase("scratch"))
            {
               outcome = "from-scratch";
            }
            else if (createFrom.equalsIgnoreCase("existing"))
            {
               outcome = "from-existing";
            }
            else if (createFrom.equalsIgnoreCase("template"))
            {
               outcome = "from-template";
            }
            
            break;
         }
         case 3:
         {
            outcome = "details";
            break;
         }
         case 4:
         {
            outcome = "summary";
            break;
         }
         default:
         {
            outcome = CANCEL_OUTCOME;
         }
      }
      
      return outcome;
   }
   
   /**
    * Performs any processing sub classes may wish to do before commit is called
    * 
    * @param context Faces context
    */
   protected void performCustomProcessing(FacesContext context)
   {
      // used by subclasses if necessary
   }
}
