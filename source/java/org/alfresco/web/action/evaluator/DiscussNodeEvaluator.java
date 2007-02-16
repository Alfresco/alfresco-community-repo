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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.action.evaluator;

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Discuss a node.
 * 
 * @author Kevin Roast
 */
public class DiscussNodeEvaluator implements ActionEvaluator
{
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
