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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;

/**
 * UI Action Evaluator for cancel workflow action. The action
 * is only allowed if the workflow the task belongs to was 
 * started by the current user.
 * 
 * @author gavinc
 */
public class CancelWorkflowEvaluator extends BaseActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      boolean result = false;
      FacesContext context = FacesContext.getCurrentInstance();
   
      // get the task from the node
      WorkflowTask task = (WorkflowTask)node.getProperties().get("workflowTask");
      if (task != null)
      {
         NodeRef initiator = task.path.instance.initiator;
         if (initiator != null)
         {
            // find the current username
            User user = Application.getCurrentUser(context);
            String currentUserName = ISO9075.encode(user.getUserName());
   
            // get the username of the initiator
            NodeService nodeSvc = Repository.getServiceRegistry(
                  context).getNodeService();
            String userName = (String)nodeSvc.getProperty(initiator, ContentModel.PROP_USERNAME);
            
            // if the current user started the workflow allow the cancel action
            if (currentUserName.equals(userName))
            {
               result = true;
            }
         }
      }
      
      return result;
   }
}
