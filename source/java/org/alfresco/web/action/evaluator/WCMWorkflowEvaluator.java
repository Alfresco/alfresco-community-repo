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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.util.Pair;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.wf.AVMSubmittedAspect;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;

/**
 * UI Action Evaluator - return true if the node is not part of an in-progress WCM workflow.
 * 
 * @author Kevin Roast
 */
public class WCMWorkflowEvaluator implements ActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      final FacesContext facesContext = FacesContext.getCurrentInstance();
      final AVMService avmService = 
         Repository.getServiceRegistry(facesContext).getAVMService();
      final Pair<Integer, String> p = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef());
      final String path = p.getSecond();
      return (!avmService.hasAspect(p.getFirst(), path, AVMSubmittedAspect.ASPECT) ||
              AVMConstants.isWorkflowStore(AVMConstants.getStoreName(path)));
   }
}
