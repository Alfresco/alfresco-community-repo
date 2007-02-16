/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.EditContentPropertiesDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Backing bean for the Edit File Properties dialog.
 * 
 * @author Kevin Roast
 */
public class EditFilePropertiesDialog extends EditContentPropertiesDialog
{
   protected AVMBrowseBean avmBrowseBean;
   protected AVMService avmService;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation

   /**
    * @see org.alfresco.web.bean.content.EditContentPropertiesDialog#initEditableNode()
    */
   protected Node initEditableNode()
   {
      return new Node(this.avmBrowseBean.getAvmActionNode().getNodeRef());
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // handle the name property separately, it is a special case for AVM nodes
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         editedProps.remove(ContentModel.PROP_NAME);
      }
      
      // we need to put all the properties from the editable bag back into 
      // the format expected by the repository
      Map<QName, Serializable> repoProps = this.nodeService.getProperties(nodeRef);
      
      // but first extract and deal with the special mimetype property for ContentData
      String mimetype = (String)editedProps.get(TEMP_PROP_MIMETYPE);
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
      
      // add the "titled" aspect if required, properties will get set below
      if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
      {
         nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
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
         
         repoProps.put(qname, propValue);
      }
      
      // send the properties back to the repository
      this.nodeService.setProperties(nodeRef, repoProps);
      
      // perform the rename last as for an AVM it changes the NodeRef
      if (name != null)
      {
         this.fileFolderService.rename(nodeRef, name);
         editedProps.put(ContentModel.PROP_NAME.toString(), name);
      }
      
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // a rename may have occured - we need to reset the NodeRef of the modified AVM Node
      // as an AVM NodeRef contains the name as part of ref - which can therefore change! 
      String name = this.editableNode.getName();
      String oldPath = AVMNodeConverter.ToAVMVersionPath(this.editableNode.getNodeRef()).getSecond();
      String newPath = oldPath.substring(0, oldPath.lastIndexOf('/') + 1) + name;
      this.avmBrowseBean.setAvmActionNode(new AVMNode(this.avmService.lookup(-1, newPath)));
      
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
               ((FileExistsException)exception).getName());
      }
      else if (exception instanceof InvalidNodeRefException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), 
               new Object[] {this.avmBrowseBean.getAvmActionNode().getPath()});
      }
      else
      {
         return super.formatErrorMessage(exception);
      }
   }
}
