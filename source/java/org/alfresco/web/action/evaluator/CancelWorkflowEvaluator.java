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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.util.ISO9075;
import org.alfresco.web.action.ActionEvaluator;
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
public class CancelWorkflowEvaluator implements ActionEvaluator
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
