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

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Delete Space" dialog
 * 
 * @author gavinc
 */
public class DeleteSpaceAssociationDialog extends DeleteSpaceDialog
{
   private static final Log logger = LogFactory.getLog(DeleteSpaceAssociationDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get the space to delete
      Node node = this.browseBean.getActionSpace();
      if (node != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Trying to delete space association: " + node.getId());
         
         NodeRef parentRef = this.navigator.getCurrentNode().getNodeRef();
         QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                  QName.createValidLocalName(node.getName()));
         ChildAssociationRef childAssocRef = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, 
                  parentRef, qname, node.getNodeRef());
 
         // remove the child association
         this.getNodeService().removeChildAssociation(childAssocRef);
      }
      else
      {
         logger.warn("WARNING: delete called without a current Space!");
      }
      
      return outcome;
   }
  
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns the confirmation to display to the user before deleting the content.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      Node node = this.browseBean.getActionSpace();
      if (node != null)
      {
         String spaceConfirmMsg = Application.getMessage(FacesContext.getCurrentInstance(),
            "delete_space_assoc_confirm");
         return MessageFormat.format(spaceConfirmMsg, new Object[] {node.getName()});
      }
      else
      {
         return Application.getMessage(FacesContext.getCurrentInstance(), 
                  "delete_node_not_found");
      }
   }
}
