package org.alfresco.web.bean.content;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Bean implementation of the "Edit Content Properties" dialog.
 * 
 * @author gavinc
 */
public class EditContentPropertiesDialog extends BaseDialogBean
{
   protected static final String TEMP_PROP_MIMETYPE = "mimetype";
   
   protected Node editableNode;
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init()
   {
      super.init();
      
      // setup the editable node
      this.editableNode = new Node(this.browseBean.getDocument().getNodeRef());
      
      // special case for Mimetype - since this is a sub-property of the ContentData object
      // we must extract it so it can be edited in the client, then we check for it later
      // and create a new ContentData object to wrap it and it's associated URL
      ContentData content = (ContentData)this.editableNode.getProperties().get(ContentModel.PROP_CONTENT);
      if (content != null)
      {
         this.editableNode.getProperties().put(TEMP_PROP_MIMETYPE, content.getMimetype());
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
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
      if (author != null && author.length() != 0)
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
      
      // deal with adding the "titled" aspect if required
      String title = (String)props.get(ContentModel.PROP_TITLE);
      String description = (String)props.get(ContentModel.PROP_DESCRIPTION);
      if (title != null || description != null)
      {
         // add the aspect to be sure it's present
         nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
         // props will get added later in setProperties()
      }
      
      // add the remaining properties
      Iterator<String> iterProps = props.keySet().iterator();
      while (iterProps.hasNext())
      {
         String propName = iterProps.next();
         QName qname = QName.createQName(propName);
         
         // make sure the property is represented correctly
         Serializable propValue = (Serializable)props.get(propName);
         
         // check for empty strings when using number types, set to null in this case
         if ((propValue != null) && (propValue instanceof String) && 
             (propValue.toString().length() == 0))
         {
            PropertyDefinition propDef = this.dictionaryService.getProperty(qname);
            if (propDef != null)
            {
               if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                   propDef.getDataType().getName().equals(DataTypeDefinition.INT) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
               {
                  propValue = null;
               }
            }
         }
         
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
         
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // reset the document held by the browse bean as it's just been updated
      this.browseBean.getDocument().reset();
         
      return outcome;
   }
   
   /**
    * Formats the error message to display if an error occurs during finish processing
    * 
    * @param The exception
    * @return The formatted message
    */
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS), 
               ((FileExistsException)exception).getExisting().getName());
      }
      else if (exception instanceof InvalidNodeRefException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), 
               new Object[] {this.browseBean.getDocument().getId()});
      }
      else
      {
         return super.formatErrorMessage(exception);
      }
   }
   
   @Override
   protected String getErrorOutcome(Throwable exception)
   {
      if (exception instanceof InvalidNodeRefException)
      {
         // this failure means the node no longer exists - we cannot show 
         // the content properties screen again so go back to the main page
         return "browse";
      }
      else
      {
         return super.getErrorOutcome(exception);
      }
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters

   /**
    * Returns the node being edited
    * 
    * @return The node being edited
    */
   public Node getEditableNode()
   {
      return this.editableNode;
   }
}
