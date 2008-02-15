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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Checkin a document with potentially a Forum attached.
 * 
 * @author Kevin Roast
 */
public class ForumsCheckinDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -924897450989526336L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      boolean allow = false;
      
      if (node.hasAspect(ContentModel.ASPECT_WORKING_COPY))
      {
         if (node.hasAspect(ForumModel.ASPECT_DISCUSSABLE))
         {
            NodeService nodeService =
               Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
            
            // get the original locked node (via the copiedfrom aspect)
            NodeRef lockedNodeRef = (NodeRef)nodeService.getProperty(node.getNodeRef(), ContentModel.PROP_COPY_REFERENCE);
            if (lockedNodeRef != null)
            {
               Node lockedNode = new Node(lockedNodeRef);
               allow = (node.hasPermission(PermissionService.CHECK_IN) && 
                        lockedNode.hasPermission(PermissionService.CONTRIBUTOR));
            }
         }
         else
         {
            // there is no discussion so just check they have checkin permission for the node
            allow = node.hasPermission(PermissionService.CHECK_IN);
         }
      }
      
      return allow;
   }
}
