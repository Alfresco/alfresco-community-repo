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
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.WorkflowUtil;

/**
 * Reject Workflow command implementation
 * 
 * @author Kevin Roast
 */
public final class RejectWorkflowCommand implements Command
{
   public static final String PROP_TARGET = "target";
   
   private static final String[] PROPERTIES = new String[] {PROP_TARGET};
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#getPropertyNames()
    */
   public String[] getPropertyNames()
   {
      return PROPERTIES;
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public void execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      // get the target Node for the command
      NodeRef nodeRef = (NodeRef)properties.get(PROP_TARGET);
      if (nodeRef == null)
      {
         throw new IllegalArgumentException(
               "Unable to execute RejectCommand - mandatory parameter not supplied: " + PROP_TARGET);
      }
      
      WorkflowUtil.reject(nodeRef, serviceRegistry.getNodeService(), serviceRegistry.getCopyService());
   }
}
