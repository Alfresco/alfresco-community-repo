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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
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
      
      if (node.hasAspect(ContentModel.ASPECT_WORKING_COPY) &&
          node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE) == null)
      {
         if (node.hasAspect(ForumModel.ASPECT_DISCUSSABLE))
         {
            CheckOutCheckInService checkOutCheckInService =
                Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCheckOutCheckInService();
            // get the original locked node (via the copiedfrom aspect)
            NodeRef lockedNodeRef = checkOutCheckInService.getCheckedOut(node.getNodeRef());
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
