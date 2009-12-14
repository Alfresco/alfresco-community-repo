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

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.springframework.extensions.surf.util.Pair;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - return true if the node is not a layered folder or if
 * the layered folder is not a primary indirection
 * 
 * @author Gavin Cornwell
 */
public class WCMWorkflowLayeredFolderEvaluator extends WCMWorkflowEvaluator
{
   private static final long serialVersionUID = 8507016785287243649L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      boolean proceed = super.evaluate(node);
      
      if (proceed)
      {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         AVMService avmService = Repository.getServiceRegistry(facesContext).getAVMService();
             
         Pair<Integer, String> p = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef());
         AVMNodeDescriptor nodeDesc = avmService.lookup(-1, p.getSecond());
         int type = nodeDesc.getType();
      
         // if the node is a 'layeredfolder' and a primary indirection don't allow
         if (type == AVMNodeType.LAYERED_DIRECTORY && nodeDesc.isPrimary())
         {
            proceed = false;
         }
      }
      
      return proceed;
   }
}
