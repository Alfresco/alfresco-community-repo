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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Initial implementation of a Command Processor that is always passed enough URL elements
 * to construct a single NodeRef argument. The NodeRef is checked against READ permissions
 * for the current user during the validateArguments() call.
 * <p>
 * This class should be enough to form the base of Command Processor objects that only require
 * a single NodeRef passed on the URL. 
 * 
 * @author Kevin Roast
 */
public abstract class BaseNodeCommandProcessor implements CommandProcessor
{
   protected NodeRef targetRef;
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(org.alfresco.service.ServiceRegistry, java.lang.String, java.lang.String[])
    */
   public boolean validateArguments(ServiceRegistry serviceRegistry, String command, String[] args)
   {
      if (args.length < 3)
      {
         throw new IllegalArgumentException("Not enough URL arguments passed to command servlet.");
      }
      
      // get NodeRef to the node with the workflow attached to it
      StoreRef storeRef = new StoreRef(args[0], args[1]);
      this.targetRef = new NodeRef(storeRef, args[2]);
      
      // get the services we need to execute the workflow command
      PermissionService permissionService = serviceRegistry.getPermissionService();
      
      // check that the user has at least READ access on the node - else redirect to the login page
      return (permissionService.hasPermission(this.targetRef, PermissionService.READ) == AccessStatus.ALLOWED);
   }
}
