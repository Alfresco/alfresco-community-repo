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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
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
   private static final long serialVersionUID = 7663087149225546333L;

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
            String currentUserName = user.getUserName();
   
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
