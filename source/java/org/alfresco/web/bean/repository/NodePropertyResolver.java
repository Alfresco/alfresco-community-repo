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
package org.alfresco.web.bean.repository;

/**
 * Simple interface used to implement small classes capable of calculating dynamic property values
 * for Nodes at runtime. This allows bean responsible for building large lists of Nodes to
 * encapsulate the code needed to retrieve non-standard Node properties. The values are then
 * calculated on demand by the property resolver.
 * 
 * When a node is reset() the standard and other props are cleared. If property resolvers are used
 * then the non-standard props will be restored automatically as well. 
 * 
 * @author Kevin Roast
 */
public interface NodePropertyResolver
{
   /**
    * Get the property value for this resolver
    * 
    * @param node       Node this property is for
    * 
    * @return property value
    */
   public Object get(Node node);
}
