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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.AVMUtil;

/**
 * UI Action Evaluator - return true if the node is not part of an in-progress WCM workflow.
 * No check for deleted items is made in this evaluator. @see WCMWorkflowEvaluator
 * 
 * @author Kevin Roast
 */
public class WCMWorkflowDeletedEvaluator extends WCMLockEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      boolean proceed = false;
      if (super.evaluate(node))
      {
         final FacesContext facesContext = FacesContext.getCurrentInstance();
         final AVMService avmService = Repository.getServiceRegistry(facesContext).getAVMService();
         final String path = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef()).getSecond();

         // evaluate to true if we are within a workflow store (i.e. list of resources in the task
         // dialog) or not part of an already in-progress workflow
         proceed = (AVMUtil.isWorkflowStore(AVMUtil.getStoreName(path)) ||
                    !((AVMNode)node).isWorkflowInFlight());
      }
      return proceed;
   }
}