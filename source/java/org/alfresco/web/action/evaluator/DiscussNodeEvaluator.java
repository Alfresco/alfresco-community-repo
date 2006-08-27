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
