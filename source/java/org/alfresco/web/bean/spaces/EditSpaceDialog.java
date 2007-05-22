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
package org.alfresco.web.bean.spaces;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Dialog bean to edit an existing space.
 *
 * @author gavinc
 */
public class EditSpaceDialog extends CreateSpaceDialog
{
   protected Node editableNode;

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      // setup the space being edited
      this.editableNode = initEditableNode();
      this.spaceType = this.editableNode.getType().toString();
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   /**
    * Init the editable Node
    */
   protected Node initEditableNode()
   {
      return new Node(this.browseBean.getActionSpace().getNodeRef());
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // update the existing node in the repository
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();

      // handle the name property separately, perform a rename in case it changed
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         this.fileFolderService.rename(nodeRef, name);
      }

      // get the current set of properties from the repository
      Map<QName, Serializable> repoProps = this.nodeService.getProperties(nodeRef);

      // add the "uifacets" aspect if required, properties will get set below
      if (this.nodeService.hasAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS) == false)
      {
         this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, null);
      }

      // overwrite the current properties with the edited ones
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


     // do nothing by default, subclasses can override if necessary
      if(isEdit())
      {
       this.browseBean.setDocument(this.getEditableNode()); // (this.editableNode.getNodeRef());

       return "dialog:createMultilingualProperties";
      }
      else
      {
       return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
      }
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      this.browseBean.getActionSpace().reset();

      return outcome;
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

   public boolean isEdit()
   {
    return this.edit;
   }

   public void setEdit(boolean x)
   {
    this.edit=x;
   }


   private boolean edit;

}
