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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Evaluates whether the cut action should be visible. 
 * 
 * If the node is a discussion don't allow the action.
 * 
 * @author gavinc
 */
public class DiscussionCutEvaluator extends CutNodeEvaluator
{
   private static final long serialVersionUID = 7260556874788184200L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      boolean result = super.evaluate(node);
      
      // if the node in question is a forum...
      if (result && node.getType().equals(ForumModel.TYPE_FORUM))
      {
         // get the association type
         FacesContext context = FacesContext.getCurrentInstance();
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
         ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(node.getNodeRef());
         QName assocType = parentAssoc.getTypeQName();
         
         // only allow the action if the association type is not the discussion assoc
         result = (assocType.equals(ForumModel.ASSOC_DISCUSSION) == false);
      }
      
      return result;
   }
}
