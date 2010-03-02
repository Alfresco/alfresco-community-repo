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

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Discuss a node.
 * 
 * @author Kevin Roast
 */
public class DiscussNodeEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 8754174908349998903L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      boolean result = false;
      
      if (node.hasAspect(ForumModel.ASPECT_DISCUSSABLE))
      {
         NodeService nodeService = Repository.getServiceRegistry(
               FacesContext.getCurrentInstance()).getNodeService();
         List<ChildAssociationRef> children = nodeService.getChildAssocs(
               node.getNodeRef(), ForumModel.ASSOC_DISCUSSION, 
               RegexQNamePattern.MATCH_ALL);
         
         // make sure there is one visible child association for the node
         if (children.size() == 1)
         {
            result = true;
         }
      }
      
      return result;
   }
}
