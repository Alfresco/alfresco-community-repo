package org.alfresco.web.bean.content;

import java.io.File;
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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for the content related wizards and dialogs
 * 
 * @author gavinc
 */
public abstract class BaseContentWizard extends BaseWizardBean
{
   protected String fileName;
   protected String author;
   protected String title;
   protected String description;
   protected String mimeType;
   protected String objectType;
   protected boolean inlineEdit;
   protected boolean otherPropertiesChoiceVisible = true;
   protected boolean showOtherProperties = true;
   
   // the NodeRef of the node created during finish
   protected NodeRef createdNode;
   protected List<SelectItem> objectTypes;
   protected ContentService contentService;
   
   private static Log logger = LogFactory.getLog(BaseContentWizard.class);
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.fileName = null;
      this.author = null;
      this.title = null;
      this.description = null;
      this.mimeType = null;
      this.inlineEdit = false;
      this.objectType = ContentModel.TYPE_CONTENT.toString();
      
      initOtherProperties();
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      if (this.fileName == null || 
          this.fileName.length() == 0 ||
          this.mimeType == null)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * @return Returns the name of the file
    */
   public String getFileName()
   {
      return this.fileName;
   }

   /**
    * @param fileName The name of the file
    */
   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }
   
   /**
    * @return Returns the author
    */
   public String getAuthor()
   {
      return this.author;
   }

   /**
    * @param author Sets the author
    */
   public void setAuthor(String author)
   {
      this.author = author;
   }

   /**
    * @return Returns the mime type currenty selected
    */
   public String getMimeType()
   {
      return this.mimeType;
   }

   /**
    * @param mimeType Sets the currently selected mime type
    */
   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }
   
   /**
    * @return Returns the object type currenty selected
    */
   public String getObjectType()
   {
      return this.objectType;
   }

   /**
    * @param objectType Sets the currently selected object type
    */
   public void setObjectType(String objectType)
   {
      this.objectType = objectType;
   }

   /**
    * @return Returns the description
    */
   public String getDescription()
   {
      return this.description;
   }

   /**
    * @param description Sets the description
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return Returns the title
    */
   public String getTitle()
   {
      return this.title;
   }

   /**
    * @param title Sets the title
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return Returns the inline edit flag.
    */
   public boolean isInlineEdit()
   {
      return this.inlineEdit;
   }

   /**
    * @param inlineEdit The inline edit flag to set.
    */
   public void setInlineEdit(boolean inlineEdit)
   {
      this.inlineEdit = inlineEdit;
   }
   
   /**
    * @return Determines whether the choice to modify all properties
    *         is shown
    */
   public boolean getOtherPropertiesChoiceVisible()
   {
      return this.otherPropertiesChoiceVisible;
   }
   
   /**
    * @return Determines whether the edit properties dialog should be
    *         shown when this one ends
    */
   public boolean getShowOtherProperties()
   {
      return this.showOtherProperties;
   }
   
   /**
    * @param showOthers Sets whether the edit properties dialog is shown
    */
   public void setShowOtherProperties(boolean showOthers)
   {
      this.showOtherProperties = showOthers;
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
         Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("content-types");
            if (typesCfg != null)
            {               
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));
                  if (idQName != null)
                  {
                     TypeDefinition typeDef = this.dictionaryService.getType(idQName);
                     
                     if (typeDef != null)
                     {
                        if (this.dictionaryService.isSubClass(typeDef.getName(), ContentModel.TYPE_CONTENT))
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
                        else
                        {
                           logger.warn("Failed to add '" + child.getAttribute("name") + 
                                 "' to the list of content types as the type is not a subtype of cm:content");
                        }
                     }
                     else
                     {
                        logger.warn("Failed to add '" + child.getAttribute("name") + 
                              "' to the list of content types as the type is not recognised");
                     }
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
            logger.warn("Could not find 'Content Wizards' configuration section");
         }
         
      }
      
      return this.objectTypes;
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   
   
   // ------------------------------------------------------------------------------
   // Service Injection

   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Save the specified content using the currently set wizard attributes
    * 
    * @param fileContent      File content to save
    * @param strContent       String content to save
    */
   protected void saveContent(File fileContent, String strContent) throws Exception
   {
      // get the node ref of the node that will contain the content
      NodeRef containerNodeRef;
      String nodeId = this.navigator.getCurrentNodeId();
      if (nodeId == null)
      {
         containerNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
      }
      else
      {
         containerNodeRef = new NodeRef(Repository.getStoreRef(), nodeId);
      }
      
      FileInfo fileInfo = this.fileFolderService.create(
            containerNodeRef,
            this.fileName,
            Repository.resolveToQName(this.objectType));
      NodeRef fileNodeRef = fileInfo.getNodeRef();
      
      // set the author aspect
      Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
      authorProps.put(ContentModel.PROP_AUTHOR, this.author);
      this.nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
      
      if (logger.isDebugEnabled())
         logger.debug("Created file node for file: " + this.fileName);
      
      // apply the titled aspect - title and description
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.title);
      titledProps.put(ContentModel.PROP_DESCRIPTION, this.description);
      this.nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);
      
      if (logger.isDebugEnabled())
         logger.debug("Added titled aspect with properties: " + titledProps);
      
      // apply the inlineeditable aspect
      if (this.inlineEdit == true)
      {
         Map<QName, Serializable> editProps = new HashMap<QName, Serializable>(1, 1.0f);
         editProps.put(ContentModel.PROP_EDITINLINE, this.inlineEdit);
         this.nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_INLINEEDITABLE, editProps);
         
         if (logger.isDebugEnabled())
            logger.debug("Added inlineeditable aspect with properties: " + editProps);
      }
      
      // get a writer for the content and put the file
      ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
      // set the mimetype and encoding
      writer.setMimetype(this.mimeType);
      writer.setEncoding("UTF-8");
      if (fileContent != null)
      {
         writer.putContent(fileContent);
      }
      else if (strContent != null)
      {
         writer.putContent(strContent);
      }
      else
      {
         writer.putContent("");
      }
      
      // remember the created node now
      this.createdNode = fileNodeRef;
   }
   
   /**
    * Returns the display label for the mime type currently chosen
    * 
    * @param mimeType The mime type to get the display label of 
    * @return The human readable version of the content type
    */
   protected String getSummaryMimeType(String mimeType)
   {
      ServiceRegistry registry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      MimetypeService mimetypeService = registry.getMimetypeService();
         
      // get the mime type display name
      Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
      return mimeTypes.get(mimeType);
   }
   
   /**
    * Returns the display label for the currently selected object type
    * 
    * @return The objevt type label
    */
   protected String getSummaryObjectType()
   {
      String objType = null;
      
      for (SelectItem item : this.getObjectTypes())
      {
         if (item.getValue().equals(this.objectType))
         {
            objType = item.getLabel();
            break;
         }
      }
      
      return objType;
   }
   
   /**
    * Initialises the other properties flags from config
    */
   protected void initOtherProperties()
   {
      ConfigService configSvc = Application.getConfigService(FacesContext.getCurrentInstance());
      
      if (configSvc != null)
      {
         Config config = configSvc.getConfig("Content Wizards");
         if (config != null)
         {
            ConfigElement otherPropsCfg = config.getConfigElement("other-properties");
            if (otherPropsCfg != null)
            {
               // get the attributes
               String userChoiceVisible = otherPropsCfg.getAttribute("user-choice-visible");
               String userChoiceDefault = otherPropsCfg.getAttribute("user-choice-default");
               
               // set the defaults
               if (userChoiceVisible != null)
               {
                  this.otherPropertiesChoiceVisible = Boolean.parseBoolean(userChoiceVisible);
               }
               
               if (userChoiceDefault != null)
               {
                  this.showOtherProperties = Boolean.parseBoolean(userChoiceDefault);
               }
            }
         }
      }
   }
}
