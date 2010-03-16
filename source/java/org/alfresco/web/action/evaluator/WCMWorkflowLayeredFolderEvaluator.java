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

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.util.Pair;
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
