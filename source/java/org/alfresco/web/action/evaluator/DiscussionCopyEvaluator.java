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
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Evaluates whether the copy action should be visible. 
 * 
 * If the node is a discussion don't allow the action.
 * 
 * @author gavinc
 */
public class DiscussionCopyEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -4080878553011296677L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      boolean result = true;
      
      // if the node in question is a forum...
      if (node.getType().equals(ForumModel.TYPE_FORUM))
      {
         // get the association type
         FacesContext context = FacesContext.getCurrentInstance();
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
         ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(node.getNodeRef());
         QName assocType = parentAssoc.getTypeQName();
         
         // only allow the action if the association type is not the discussion assoc
         result = (assocType.equals(ForumModel.ASSOC_DISCUSSION) == false);
      }
      
      // impossible to copy a translation without content.
      if (result && node.getAspects().contains(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
      {
         result = false;
      }
      
      if (!node.hasPermission(PermissionService.READ)) 
      { 
          result = false; 
      } 
      
      return result;
   }
}
