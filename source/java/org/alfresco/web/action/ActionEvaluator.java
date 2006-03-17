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
package org.alfresco.web.action;

import org.alfresco.web.bean.repository.Node;

/**
 * Contract supported by all classes that provide dynamic evaluation for a UI action.
 * <p>
 * Evaluators are supplied with a Node instance context object.
 * <p>
 * The evaluator should decide if the action precondition is valid based on the appropriate
 * logic and the properties etc. of the Node context and return the result.
 * 
 * @author Kevin Roast
 */
public interface ActionEvaluator
{
   /**
    * The evaluator should decide if the action precondition is valid based on the appropriate
    * logic and the properties etc. of the Node context and return the result.
    * 
    * @param node    Node context for the action
    * 
    * @return result of whether the action can proceed.
    */
   public boolean evaluate(Node node);
}
