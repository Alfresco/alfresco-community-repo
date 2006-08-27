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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Checkin a document with potentially a Forum attached.
 * 
 * @author Kevin Roast
 */
public class ForumsCheckinDocEvaluator implements ActionEvaluator
{
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
