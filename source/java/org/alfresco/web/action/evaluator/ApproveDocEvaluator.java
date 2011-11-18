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

import java.util.Map;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - 'Approve' workflow step for document or space.
 * 
 * @author Kevin Roast
 */
public class ApproveDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 2958297435415449179L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      Map<String,Object> properties = node.getProperties();
      
      Boolean approveMove = (Boolean) properties.get("app:approveMove");
      boolean isMove = approveMove == null ? false : approveMove; 
      
      boolean canProceed = (properties.get("app:approveStep") != null) && !node.isLocked();
      //If this approval is going to result in a move of the node then we check whether the user
      //has permission. The delete permission is required in order to move a node (odd, perhaps, but true).
      canProceed &= (!isMove || node.hasPermission(PermissionService.DELETE));
      
      return canProceed;
   }
}
