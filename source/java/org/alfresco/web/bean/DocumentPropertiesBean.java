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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigLookupContext;
import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.PropertySheetConfigElement;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Backing bean for the edit document properties dialog
 * 
 * @author gavinc
 */
public class DocumentPropertiesBean
{
   private static final String TEMP_PROP_MIMETYPE = "mimetype";
   
   private NodeService nodeService;
   private FileFolderService fileFolderService;
   private DictionaryService dictionaryService;
   private BrowseBean browseBean;
   private List<SelectItem> contentTypes;
   private Node editableNode;
   private Boolean hasOtherProperties;
   
   /**
    * Returns the node being edited
    * 
    * @return The node being edited
    */
   public Node getEditableNode()
   {
      return this.editableNode;
   }
   
   /**
    * Event handler called to setup the document for property editing
    * 
    * @param event The event
    */
   public void setupDocumentForAction(ActionEvent event)
   {
      this.editableNode = new Node(this.browseBean.getDocument().getNodeRef());
      
      // special case for Mimetype - since this is a sub-property of the ContentData object
      // we must extract it so it can be edited in the client, then we check for it later
      // and create a new ContentData object to wrap it and it's associated URL
      ContentData content = (ContentData)this.editableNode.getProperties().get(ContentModel.PROP_CONTENT);
      if (content != null)
      {
         this.editableNode.getProperties().put(TEMP_PROP_MIMETYPE, content.getMimetype());
      }
      
      this.hasOtherProperties = null;
   }
   
   /**
    * Event handler used to save the edited properties back to the repository
    * 
    * @return The outcome
    */
   public String save()
   {
      String outcome = "cancel";
      
      UserTransaction tx = null;
      
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         NodeRef nodeRef = this.browseBean.getDocument().getNodeRef();
         Map<String, Object> props = this.editableNode.getProperties();
         
         // get the name and move the node as necessary
         String name = (String) props.get(ContentModel.PROP_NAME);
         if (name != null)
         {
            fileFolderService.rename(nodeRef, name);
         }
         
         Map<QName, Serializable> properties = this.nodeService.getProperties(nodeRef);
         // we need to put all the properties from the editable bag back into 
         // the format expected by the repository
         
         // but first extract and deal with the special mimetype property for ContentData
         String mimetype = (String)props.get(TEMP_PROP_MIMETYPE);
         if (mimetype != null)
         {
            // remove temporary prop from list so it isn't saved with the others
            props.remove(TEMP_PROP_MIMETYPE);
            ContentData contentData = (ContentData)props.get(ContentModel.PROP_CONTENT);
            if (contentData != null)
            {
               contentData = ContentData.setMimetype(contentData, mimetype);
               props.put(ContentModel.PROP_CONTENT.toString(), contentData);
            }
         }
         
         // extra and deal with the Author prop if the aspect has not been applied yet
         String author = (String)props.get(ContentModel.PROP_AUTHOR);
         if (author != null)
         {
            // add aspect if required
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == false)
            {
               Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
               authorProps.put(ContentModel.PROP_AUTHOR, author);
               this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
            }
            // else it will get updated in the later setProperties() call
         }
         
         // add the remaining properties
         Iterator<String> iterProps = props.keySet().iterator();
         while (iterProps.hasNext())
         {
            String propName = iterProps.next();
            QName qname = QName.createQName(propName);
            
            // make sure the property is represented correctly
            Serializable propValue = (Serializable)props.get(propName);
            PropertyDefinition propDef = this.dictionaryService.getProperty(qname);
            
            properties.put(qname, propValue);
         }
         
         // send the properties back to the repository
         this.nodeService.setProperties(this.browseBean.getDocument().getNodeRef(), properties);
         
         // we also need to persist any association changes that may have been made
         
         // add any associations added in the UI
         Map<String, Map<String, AssociationRef>> addedAssocs = this.editableNode.getAddedAssociations();
         for (Map<String, AssociationRef> typedAssoc : addedAssocs.values())
         {
            for (AssociationRef assoc : typedAssoc.values())
            {
               this.nodeService.createAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
            }
         }
         
         // remove any association removed in the UI
         Map<String, Map<String, AssociationRef>> removedAssocs = this.editableNode.getRemovedAssociations();
         for (Map<String, AssociationRef> typedAssoc : removedAssocs.values())
         {
            for (AssociationRef assoc : typedAssoc.values())
            {
               this.nodeService.removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
            }
         }
         
         // add any child associations added in the UI
         Map<String, Map<String, ChildAssociationRef>> addedChildAssocs = this.editableNode.getAddedChildAssociations();
         for (Map<String, ChildAssociationRef> typedAssoc : addedChildAssocs.values())
         {
            for (ChildAssociationRef assoc : typedAssoc.values())
            {
               this.nodeService.addChild(assoc.getParentRef(), assoc.getChildRef(), assoc.getTypeQName(), assoc.getTypeQName());
            }
         }
         
         // remove any child association removed in the UI
         Map<String, Map<String, ChildAssociationRef>> removedChildAssocs = this.editableNode.getRemovedChildAssociations();
         for (Map<String, ChildAssociationRef> typedAssoc : removedChildAssocs.values())
         {
            for (ChildAssociationRef assoc : typedAssoc.values())
            {
               this.nodeService.removeChild(assoc.getParentRef(), assoc.getChildRef());
            }
         }
         
         // commit the transaction
         tx.commit();
         
         // set the outcome to refresh
         outcome = "finish";
         
         // reset the document held by the browse bean as it's just been updated
         this.browseBean.getDocument().reset();
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
      catch (InvalidNodeRefException err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {this.browseBean.getDocument().getId()}) );
         // this failure means the node no longer exists - we cannot show the doc properties screen
         outcome = "browse";
      }
      catch (Exception e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
      
      return outcome;
   }
   
   public Map<String, Object> getProperties()
   {
      return this.editableNode.getProperties();
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
    * Determines whether this document has any other properties other than the 
    * default set to display to the user.
    * 
    * @return true of there are properties to show, false otherwise
    */
   public boolean getOtherPropertiesPresent()
   {
      if (this.hasOtherProperties == null)
      {
         // we need to use the config service to see whether there are any
         // editable properties configured for this document.
         ConfigService configSvc = (ConfigService)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(Application.BEAN_CONFIG_SERVICE);
         Config configProps = configSvc.getConfig(this.editableNode, new ConfigLookupContext("edit-properties"));
         PropertySheetConfigElement propsToDisplay = (PropertySheetConfigElement)configProps.
               getConfigElement("property-sheet");
         this.hasOtherProperties = Boolean.valueOf(propsToDisplay != null);
      }
      
      return this.hasOtherProperties.booleanValue();
   }
   
   /**
    * @return Returns the nodeService.
    */
   public NodeService getNodeService()
   {
      return this.nodeService;
   }

   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @param fileFolderService the file and folder model-specific functions
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }

   /**
    * Sets the DictionaryService to use when persisting metadata
    * 
    * @param dictionaryService The DictionaryService
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }

   /**
    * @return The BrowseBean
    */
   public BrowseBean getBrowseBean()
   {
      return this.browseBean;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
}
