/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
