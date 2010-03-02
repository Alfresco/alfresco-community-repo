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
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.workflow.WorkflowUtil;

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
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      // get the target Node for the command
      NodeRef nodeRef = (NodeRef)properties.get(PROP_TARGET);
      if (nodeRef == null)
      {
         throw new IllegalArgumentException(
               "Unable to execute RejectCommand - mandatory parameter not supplied: " + PROP_TARGET);
      }
      
      WorkflowUtil.reject(nodeRef, serviceRegistry.getNodeService(), serviceRegistry.getCopyService());
      
      return true;
   }
}
