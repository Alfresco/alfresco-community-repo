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

import org.alfresco.model.ForumModel;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Discuss a node.
 * 
 * @author Kevin Roast
 */
public final class DiscussNodeEvaluator implements ActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      return (node.hasAspect(ForumModel.ASPECT_DISCUSSABLE) == true);
   }
}
/*
<a:booleanEvaluator value="#{r.beingDiscussed == true}">
*/