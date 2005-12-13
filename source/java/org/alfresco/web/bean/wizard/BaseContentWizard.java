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

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Base Handler class used by the Content Wizards 
 * 
 * @author gavinc kevinr
 */
public abstract class BaseContentWizard extends AbstractWizardBean
{
   private static Log logger = LogFactory.getLog(BaseContentWizard.class);

   protected static final String FINISH_INSTRUCTION_ID = "content_finish_instruction";
   
   // content wizard specific attributes
   protected String fileName;
   protected String author;
   protected String title;
   protected String description;
   protected String contentType;
   protected String objectType;
   protected boolean inlineEdit;
   protected List<SelectItem> contentTypes;
   protected List<SelectItem> objectTypes;
   protected ContentService contentService;
   protected DictionaryService dictionaryService;
   
   // the NodeRef of the node created during finish
   protected NodeRef createdNode;
   
   /**
    * Save the specified content using the currently set wizard attributes
    * 
    * @param fileContent      File content to save
    * @param strContent       String content to save
    */
   protected String saveContent(File fileContent, String strContent)
   {
      String outcome = FINISH_OUTCOME;
      
      UserTransaction tx = null;
      
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         if (this.editMode)
         {
            // update the existing node in the repository
            Node currentDocument = this.browseBean.getDocument();
            NodeRef nodeRef = currentDocument.getNodeRef();
            
            // move the file - location and name checks will be performed
            this.fileFolderService.move(nodeRef, null, this.fileName); 
            // set up the content data
            // update the modified timestamp and other content props
            Map<QName, Serializable> contentProps = this.nodeService.getProperties(nodeRef);
            contentProps.put(ContentModel.PROP_TITLE, this.title);
            contentProps.put(ContentModel.PROP_DESCRIPTION, this.description);
            
            // add author property
            if (this.author != null && this.author.length() != 0)
            {
               if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == false)
               {
                  Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
                  authorProps.put(ContentModel.PROP_AUTHOR, this.author);
                  this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
               }
               else
               {
                  contentProps.put(ContentModel.PROP_AUTHOR, this.author);
               }
            }
            
            // set up content properties - copy or create the compound property
            ContentData contentData = (ContentData)contentProps.get(ContentModel.PROP_CONTENT);
            if (contentData == null)
            {
               contentData = new ContentData(null, this.contentType, 0L, "UTF-8");
            }
            else
            {
               contentData = new ContentData(
                     contentData.getContentUrl(),
                     this.contentType,
                     contentData.getSize(),
                     contentData.getEncoding());
            }
            contentProps.put(ContentModel.PROP_CONTENT, contentData);
            
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INLINEEDITABLE) == false)
            {
               Map<QName, Serializable> editProps = new HashMap<QName, Serializable>(1, 1.0f);
               editProps.put(ContentModel.PROP_EDITINLINE, this.inlineEdit);
               this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_INLINEEDITABLE, editProps);
            }
            else
            {
               contentProps.put(ContentModel.PROP_EDITINLINE, this.inlineEdit);
            }
            this.nodeService.setProperties(nodeRef, contentProps);
         }
         else
         {
            // get the node ref of the node that will contain the content
            NodeRef containerNodeRef;
            String nodeId = getNavigator().getCurrentNodeId();
            if (nodeId == null)
            {
               containerNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
            }
            else
            {
               containerNodeRef = new NodeRef(Repository.getStoreRef(), nodeId);
            }
            
            FileInfo fileInfo = fileFolderService.create(
                  containerNodeRef,
                  this.fileName,
                  Repository.resolveToQName(this.objectType));
            NodeRef fileNodeRef = fileInfo.getNodeRef();
            
            // set the author aspect (if we have one)
            if (this.author != null && this.author.length() > 0)
            {
               Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
               authorProps.put(ContentModel.PROP_AUTHOR, this.author);
               this.nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
            }
            
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
            writer.setMimetype(this.contentType);
            writer.setEncoding("UTF-8");
            if (fileContent != null)
            {
               writer.putContent(fileContent);
            }
            else if (strContent != null)
            {
               writer.putContent(strContent);
            }
            
            // remember the created node now
            this.createdNode = fileNodeRef;
         }
         
         // give subclasses a chance to perform custom processing before committing
         performCustomProcessing();
         
         // commit the transaction
         tx.commit();
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
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepInstructions()
    */
   public String getStepInstructions()
   {
      String stepInstruction = null;
      
      switch (this.currentStep)
      {
         case 3:
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
      
      this.fileName = null;
      this.author = null;
      this.title = null;
      this.description = null;
      this.contentType = null;
      this.inlineEdit = false;
      this.contentTypes = null;
      this.objectTypes = null;
      
      this.objectType = ContentModel.TYPE_CONTENT.toString();
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#populate()
    */
   public void populate()
   {
      // get hold of the current document and populate the appropriate values
      Node currentDocument = this.browseBean.getDocument();
      Map<String, Object> props = currentDocument.getProperties();
      
      Boolean inline = (Boolean)props.get("editInline");
      this.inlineEdit = inline != null ? inline.booleanValue() : false;
      this.author = (String)props.get("creator");
      this.contentType = null;
      ContentData contentData = (ContentData)props.get(ContentModel.PROP_CONTENT);
      if (contentData != null)
      {
         this.contentType = contentData.getMimetype();
      }
      this.description = (String)props.get("description");
      this.fileName = currentDocument.getName();
      this.title = (String)props.get("title");
   }
   
   /**
    * @return Returns the contentService.
    */
   public ContentService getContentService()
   {
      return contentService;
   }

   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
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
    * @return Returns the content type currenty selected
    */
   public String getContentType()
   {
      return this.contentType;
   }

   /**
    * @param contentType Sets the currently selected content type
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
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
    * @return Returns a list of content types to allow the user to select from
    */
   public List<SelectItem> getContentTypes()
   {
      if (this.contentTypes == null)
      {
         this.contentTypes = new ArrayList<SelectItem>(80);
         ServiceRegistry registry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
         MimetypeService mimetypeService = registry.getMimetypeService();
         
         // get the mime type display names
         Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
         for (String mimeType : mimeTypes.keySet())
         {
            this.contentTypes.add(new SelectItem(mimeType, mimeTypes.get(mimeType)));
         }
         
         // make sure the list is sorted by the values
         QuickSort sorter = new QuickSort(this.contentTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      return this.contentTypes;
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
         ConfigService svc = (ConfigService)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(Application.BEAN_CONFIG_SERVICE);
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
    * @return Determines whether the next and finish button should be enabled 
    */
   public boolean getNextFinishDisabled()
   {
      boolean disabled = false;
      
      if (this.fileName == null || this.fileName.length() == 0 ||
          this.title == null || this.title.length() == 0 ||
          this.contentType == null)
      {
         disabled = true;
      }
      
      return disabled;
   }
   
   /**
    * Returns the display label for the content type currently chosen
    * 
    * @return The human readable version of the content type
    */
   protected String getSummaryContentType()
   {
      ServiceRegistry registry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      MimetypeService mimetypeService = registry.getMimetypeService();
         
      // get the mime type display name
      Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
      return mimeTypes.get(this.contentType);
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
    * Performs any processing sub classes may wish to do before commit is called
    */
   protected void performCustomProcessing()
   {
      // used by subclasses if necessary
   }
}
