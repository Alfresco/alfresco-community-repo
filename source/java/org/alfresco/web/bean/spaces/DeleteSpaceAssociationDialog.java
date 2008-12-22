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
