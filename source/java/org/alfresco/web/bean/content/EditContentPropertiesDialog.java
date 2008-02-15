/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.content;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.i18n.I18NUtil;
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
   private static final long serialVersionUID = -5681296528149487178L;
   
   protected static final String TEMP_PROP_MIMETYPE = "mimetype";
   protected static final String TEMP_PROP_ENCODING = "encoding";
   
   protected Node editableNode;
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup the editable node
      this.editableNode = initEditableNode();
      
      // special case for Mimetype - since this is a sub-property of the ContentData object
      // we must extract it so it can be edited in the client, then we check for it later
      // and create a new ContentData object to wrap it and it's associated URL
      ContentData content = (ContentData)this.editableNode.getProperties().get(ContentModel.PROP_CONTENT);
      if (content != null)
      {
         this.editableNode.getProperties().put(TEMP_PROP_MIMETYPE, content.getMimetype());
         this.editableNode.getProperties().put(TEMP_PROP_ENCODING, content.getEncoding());
      }
   }
   
   /**
    * Init the editable Node
    */
   protected Node initEditableNode()
   {
      return new Node(this.browseBean.getDocument().getNodeRef());
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // get the name and move the node as necessary
      String name = (String) editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         getFileFolderService().rename(nodeRef, name);
      }
      
      // we need to put all the properties from the editable bag back into 
      // the format expected by the repository
      Map<QName, Serializable> repoProps = this.getNodeService().getProperties(nodeRef);
      
      // Extract and deal with the special mimetype property for ContentData
      String mimetype = (String) editedProps.get(TEMP_PROP_MIMETYPE);
      if (mimetype != null)
      {
         // remove temporary prop from list so it isn't saved with the others
         editedProps.remove(TEMP_PROP_MIMETYPE);
         ContentData contentData = (ContentData)editedProps.get(ContentModel.PROP_CONTENT);
         if (contentData != null)
         {
            contentData = ContentData.setMimetype(contentData, mimetype);
            editedProps.put(ContentModel.PROP_CONTENT.toString(), contentData);
         }
      }
      // Extract and deal with the special encoding property for ContentData
      String encoding = (String) editedProps.get(TEMP_PROP_ENCODING);
      if (encoding != null)
      {
         // remove temporary prop from list so it isn't saved with the others
         editedProps.remove(TEMP_PROP_ENCODING);
         ContentData contentData = (ContentData) editedProps.get(ContentModel.PROP_CONTENT);
         if (contentData != null)
         {
            contentData = ContentData.setEncoding(contentData, encoding);
            editedProps.put(ContentModel.PROP_CONTENT.toString(), contentData);
         }
      }
      
      // add the "author" aspect if required, properties will get set below
      if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == false)
      {
         this.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, null);
      }
      
      // add the "titled" aspect if required, properties will get set below
      if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
      {
         getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
      }
      
      // add the remaining properties
      Iterator<String> iterProps = editedProps.keySet().iterator();
      while (iterProps.hasNext())
      {
         String propName = iterProps.next();
         QName qname = QName.createQName(propName);
         
         // make sure the property is represented correctly
         Serializable propValue = (Serializable)editedProps.get(propName);
         
         // check for empty strings when using number types, set to null in this case
         if (propValue instanceof String)
         {
            PropertyDefinition propDef = this.getDictionaryService().getProperty(qname);
            if (((String)propValue).length() == 0)
            {
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
            // handle locale strings to Locale objects
            else if (propDef != null && propDef.getDataType().getName().equals(DataTypeDefinition.LOCALE))
            {
               propValue = I18NUtil.parseLocale((String)propValue);
            }
         }
         
         repoProps.put(qname, propValue);
      }
      
      // send the properties back to the repository
      this.getNodeService().setProperties(nodeRef, repoProps);
      
      // we also need to persist any association changes that may have been made
      
      // add any associations added in the UI
      Map<String, Map<String, AssociationRef>> addedAssocs = this.editableNode.getAddedAssociations();
      for (Map<String, AssociationRef> typedAssoc : addedAssocs.values())
      {
         for (AssociationRef assoc : typedAssoc.values())
         {
            this.getNodeService().createAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
         }
      }
      
      // remove any association removed in the UI
      Map<String, Map<String, AssociationRef>> removedAssocs = this.editableNode.getRemovedAssociations();
      for (Map<String, AssociationRef> typedAssoc : removedAssocs.values())
      {
         for (AssociationRef assoc : typedAssoc.values())
         {
            this.getNodeService().removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
         }
      }
      
      // add any child associations added in the UI
      Map<String, Map<String, ChildAssociationRef>> addedChildAssocs = this.editableNode.getAddedChildAssociations();
      for (Map<String, ChildAssociationRef> typedAssoc : addedChildAssocs.values())
      {
         for (ChildAssociationRef assoc : typedAssoc.values())
         {
            this.getNodeService().addChild(assoc.getParentRef(), assoc.getChildRef(), assoc.getTypeQName(), assoc.getTypeQName());
         }
      }
      
      // remove any child association removed in the UI
      Map<String, Map<String, ChildAssociationRef>> removedChildAssocs = this.editableNode.getRemovedChildAssociations();
      for (Map<String, ChildAssociationRef> typedAssoc : removedChildAssocs.values())
      {
         for (ChildAssociationRef assoc : typedAssoc.values())
         {
            this.getNodeService().removeChild(assoc.getParentRef(), assoc.getChildRef());
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
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
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
