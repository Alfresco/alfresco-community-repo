/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.workflow.WorkflowUtil;

/**
 * Approve Workflow command implementation
 * 
 * @author Kevin Roast
 */
public final class ApproveWorkflowCommand implements Command
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
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      // get the target Node for the command
      NodeRef nodeRef = (NodeRef)properties.get(PROP_TARGET);
      if (nodeRef == null)
      {
         throw new IllegalArgumentException(
               "Unable to execute ApproveCommand - mandatory parameter not supplied: " + PROP_TARGET);
      }
      
      WorkflowUtil.approve(nodeRef, serviceRegistry.getNodeService(), serviceRegistry.getCopyService());
      
      return true;
   }
}
