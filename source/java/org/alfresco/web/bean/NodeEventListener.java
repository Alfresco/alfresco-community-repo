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
package org.alfresco.web.bean;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

/**
 * @author Kevin Roast
 */
public interface NodeEventListener
{
   /**
    * Callback executed when a Node wrapped object is created. This is generally used
    * to add additional property resolvers to the Node for a specific model type.
    * 
    * @param node    The Node wrapper that has been created
    * @param type    Type of the Node that has been created
    */
   public void created(Node node, QName type);
}
