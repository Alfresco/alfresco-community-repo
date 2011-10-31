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
package org.alfresco.web.bean.spaces;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.description.UIDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean responsible for the create space wizard
 *
 * @author gavinc
 */
public class CreateSpaceWizard extends BaseWizardBean
{
   private static final long serialVersionUID = 2917623558917193097L;
   
   public static final String DEFAULT_SPACE_ICON_NAME = "space-icon-default";
   public static final String DEFAULT_SPACE_ICON_PATH = "";
   public static final String DEFAULT_SPACE_TYPE_ICON_PATH = "/images/icons/space.gif";

   private static Log logger = LogFactory.getLog(CreateSpaceWizard.class);
   
   protected static final String CREATEFROM_TEMPLATE = "template";
   protected static final String CREATEFROM_EXISTING = "existing";
   protected static final String CREATEFROM_SCRATCH = "scratch";

   protected String spaceType;
   protected String icon;
   protected String createFrom;
   protected NodeRef existingSpaceId;
   protected String templateSpaceId;
   protected String copyPolicy;
   protected String name;
   protected String title;
   protected String description;
   protected String templateName;
   protected boolean saveAsTemplate;
   protected List<SelectItem> templates;
   protected List<UIListItem> folderTypes;
   protected List<UIDescription> folderTypeDescriptions;

   // the NodeRef of the node created during finish
   protected NodeRef createdNode;

   // ------------------------------------------------------------------------------
   // Wizard implementation

   /**
    * Initialises the wizard
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      // clear the cached query results
      if (this.templates != null)
      {
         this.templates.clear();
         this.templates = null;
      }

      // reset all variables
      this.createFrom = CREATEFROM_SCRATCH;
      this.spaceType = ContentModel.TYPE_FOLDER.toString();
      this.icon = null;
      this.copyPolicy = "contents";
      this.existingSpaceId = null;
      this.templateSpaceId = null;
      this.name = null;
      this.title = null;
      this.description = null;
      this.templateName = null;
      this.saveAsTemplate = false;
   }

   public String next()
   {
      // if the user has chosen to create the space from an existing
      // space or from a template we need to find it's type to show
      // the current set of icons.
      if (this.createFrom.equals(CREATEFROM_EXISTING) && this.existingSpaceId != null)
      {
         this.spaceType = this.getNodeService().getType(this.existingSpaceId).toString();
      }
      else if (this.createFrom.equals(CREATEFROM_TEMPLATE) && this.templateSpaceId != null &&
               !this.templateSpaceId.equals("none"))
      {
         NodeRef templateNode = new NodeRef(Repository.getStoreRef(), this.templateSpaceId);
         this.spaceType = this.getNodeService().getType(templateNode).toString();
      }

      return null;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      String newSpaceId = null;

      if (this.createFrom.equals(CREATEFROM_SCRATCH))
      {
         // create the space (just create a folder for now)
         NodeRef parentNodeRef;
         String nodeId = this.navigator.getCurrentNodeId();
         if (nodeId == null)
         {
            parentNodeRef = this.getNodeService().getRootNode(Repository.getStoreRef());
         }
         else
         {
            parentNodeRef = new NodeRef(Repository.getStoreRef(), nodeId);
         }

         FileInfo fileInfo = getFileFolderService().create(
               parentNodeRef,
               this.name,
               Repository.resolveToQName(this.spaceType));
         NodeRef nodeRef = fileInfo.getNodeRef();
         newSpaceId = nodeRef.getId();

         if (logger.isDebugEnabled())
            logger.debug("Created folder node with name: " + this.name);

         // apply the uifacets aspect - icon, title and description props
         Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
         uiFacetsProps.put(ApplicationModel.PROP_ICON, this.icon);
         uiFacetsProps.put(ContentModel.PROP_TITLE, this.title);
         uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
         this.getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

         if (logger.isDebugEnabled())
            logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);

         // remember the created node
         this.createdNode = nodeRef;
      }
      else if (this.createFrom.equals(CREATEFROM_EXISTING))
      {
         // copy the selected space and update the name, description and icon
         NodeRef sourceNode = this.existingSpaceId;
         NodeRef parentSpace = new NodeRef(Repository.getStoreRef(), this.navigator.getCurrentNodeId());

         // copy from existing
         NodeRef copiedNode = this.getFileFolderService().copy(sourceNode, parentSpace, this.name).getNodeRef();

         // also need to set the new title, description and icon properties
         this.getNodeService().setProperty(copiedNode, ContentModel.PROP_TITLE, this.title);
         this.getNodeService().setProperty(copiedNode, ContentModel.PROP_DESCRIPTION, this.description);
         this.getNodeService().setProperty(copiedNode, ApplicationModel.PROP_ICON, this.icon);

         newSpaceId = copiedNode.getId();

         if (logger.isDebugEnabled())
            logger.debug("Copied space with id of " + sourceNode.getId() + " to " + this.name);

         // remember the created node
         this.createdNode = copiedNode;
      }
      else if (this.createFrom.equals(CREATEFROM_TEMPLATE))
      {
         // copy the selected space and update the name, description and icon
         NodeRef sourceNode = new NodeRef(Repository.getStoreRef(), this.templateSpaceId);
         NodeRef parentSpace = new NodeRef(Repository.getStoreRef(), this.navigator.getCurrentNodeId());
         // copy from the template
         NodeRef copiedNode = this.getFileFolderService().copy(sourceNode, parentSpace, this.name).getNodeRef();
         // also need to set the new title, description and icon properties
         this.getNodeService().setProperty(copiedNode, ContentModel.PROP_TITLE, this.title);
         this.getNodeService().setProperty(copiedNode, ContentModel.PROP_DESCRIPTION, this.description);
         this.getNodeService().setProperty(copiedNode, ApplicationModel.PROP_ICON, this.icon);

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

         NodeRef rootNodeRef = this.getNodeService().getRootNode(Repository.getStoreRef());
         List<NodeRef> templateNodeList = this.getSearchService().selectNodes(
               rootNodeRef,
               xpath, null, namespacePrefixResolver, false);
         if (templateNodeList.size() == 1)
         {
            // get the first item in the list as we from test above there is only one!
            NodeRef templateNode = templateNodeList.get(0);
            NodeRef sourceNode = new NodeRef(Repository.getStoreRef(), newSpaceId);
            // copy this to the template location
            getFileFolderService().copy(sourceNode, templateNode, this.templateName);
         }
      }

      return outcome;
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

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
      this.name = name.trim();
   }

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
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
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      String summaryCreateType = null;
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

      if (this.createFrom.equals(CREATEFROM_SCRATCH))
      {
         summaryCreateType = bundle.getString(CREATEFROM_SCRATCH);
      }
      else if (this.createFrom.equals(CREATEFROM_EXISTING))
      {
         summaryCreateType = bundle.getString("an_existing_space");
      }
      else if (this.createFrom.equals(CREATEFROM_TEMPLATE))
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
            new String[] {spaceTypeLabel, Utils.encode(this.name), Utils.encode(this.description), summaryCreateType});
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
         NodeRef rootNodeRef = this.getNodeService().getRootNode(Repository.getStoreRef());
         List<NodeRef> results = this.getSearchService().selectNodes(rootNodeRef, xpath, null, this.getNamespaceService(), false);

         if (results.size() != 0)
         {
            // show templates of the type relating to the space we are creating
            QName spaceType = QName.createQName(this.spaceType);
            for (NodeRef assocRef : results)
            {
               Node childNode = new Node(assocRef);
               if (this.getDictionaryService().isSubClass(childNode.getType(), spaceType))
               {
                  this.templates.add(new SelectItem(childNode.getId(), childNode.getName()));
               }
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
      if ((this.folderTypes == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
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
         defaultItem.setImage(DEFAULT_SPACE_TYPE_ICON_PATH);
         this.folderTypes.add(defaultItem);

         UIDescription defaultDesc = new UIDescription();
         defaultDesc.setControlValue(ContentModel.TYPE_FOLDER.toString());
         defaultDesc.setText(Application.getMessage(context, "container_desc"));
         this.folderTypeDescriptions.add(defaultDesc);

         // add any configured content sub-types to the list
         Config wizardCfg = Application.getConfigService(FacesContext.getCurrentInstance()).
               getConfig("Space Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("folder-types");
            if (typesCfg != null)
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));
                  if (idQName != null)
                  {
                     TypeDefinition typeDef = this.getDictionaryService().getType(idQName);
   
                     if (typeDef != null)
                     {
                        if (this.getDictionaryService().isSubClass(typeDef.getName(), ContentModel.TYPE_FOLDER))
                        {
                           // try and get the label from config
                           String label = Utils.getDisplayLabel(context, child);
   
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
                           String description = Utils.getDescription(context, child);
   
                           // if we don't have a local description just use the label
                           if (description == null)
                           {
                              description = label;
                           }
   
                           // extract the icon to use from the config
                           String icon = child.getAttribute("icon");
                           if (icon == null || icon.length() == 0)
                           {
                              icon = DEFAULT_SPACE_TYPE_ICON_PATH;
                           }
   
                           UIListItem item = new UIListItem();
                           item.setValue(idQName.toString());
                           item.setLabel(label);
                           item.setTooltip(label);
                           item.setImage(icon);
                           this.folderTypes.add(item);
   
                           UIDescription desc = new UIDescription();
                           desc.setControlValue(idQName.toString());
                           desc.setText(description);
                           this.folderTypeDescriptions.add(desc);
                        }
                        else
                        {
                           logger.warn("Failed to add '" + child.getAttribute("name") +
                                 "' to the list of folder types as the type is not a subtype of cm:folder");
                        }
                     }
                     else
                     {
                        logger.warn("Failed to add '" + child.getAttribute("name") +
                              "' to the list of folder types as the type is not recognised");
                     }
                  }
                  else
                  {
                     logger.warn("Failed to add '" + child.getAttribute("name") +
                              "' to the list of folder types as the prefix can not be resolved");
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
            logger.warn("Could not find 'Space Wizards' configuration section");
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
      if ((this.folderTypeDescriptions == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
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
      // NOTE: we can't cache this list as it depends on the space type
      //       which the user can change during the advanced space wizard

      List<UIListItem> icons = null;
      List<String> iconNames = new ArrayList<String>(8);

      QName type = QName.createQName(this.spaceType);
      String typePrefixForm = type.toPrefixString(this.getNamespaceService());

      Config config = Application.getConfigService(FacesContext.getCurrentInstance()).
            getConfig(typePrefixForm + " icons");
      if (config != null)
      {
         ConfigElement iconsCfg = config.getConfigElement("icons");
         if (iconsCfg != null)
         {
            boolean first = true;
            for (ConfigElement icon : iconsCfg.getChildren())
            {
               String iconName = icon.getAttribute("name");
               String iconPath = icon.getAttribute("path");

               if (iconName != null && iconPath != null)
               {
                  if (first)
                  {
                     // if this is the first icon create the list and make
                     // the first icon in the list the default

                     icons = new ArrayList<UIListItem>(iconsCfg.getChildCount());
                     if (this.icon == null)
                     {
                        // set the default if it is not already
                        this.icon = iconName;
                     }
                     first = false;
                  }

                  UIListItem item = new UIListItem();
                  item.setValue(iconName);
                  item.setImage(iconPath);
                  icons.add(item);
                  iconNames.add(iconName);
               }
            }
         }
      }

      // if we didn't find any icons display one default choice
      if (icons == null)
      {
         icons = new ArrayList<UIListItem>(1);
         this.icon = DEFAULT_SPACE_ICON_NAME;

         UIListItem item = new UIListItem();
         item.setValue(DEFAULT_SPACE_ICON_NAME);
         item.setImage("/images/icons/space-icon-default.gif");
         icons.add(item);
         iconNames.add(DEFAULT_SPACE_ICON_NAME);
      }

      // make sure the current value for the icon is valid for the
      // current list of icons about to be displayed
      if (iconNames.contains(this.icon) == false)
      {
         this.icon = iconNames.get(0);
      }

      return icons;
   }

   // ------------------------------------------------------------------------------
   // Helper methods

   /**
    * Formats the error message to display if an error occurs during finish processing
    *
    * @param exception The exception
    * @return The formatted message
    */
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS),
               ((FileExistsException)exception).getName());
      }
      else
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), "error_space"),
               exception.getMessage());
      }
   }
}
