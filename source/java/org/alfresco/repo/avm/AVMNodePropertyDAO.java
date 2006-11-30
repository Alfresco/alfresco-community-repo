/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * DAO for AVMNodeProperty.
 * @author britt
 */
public interface AVMNodePropertyDAO
{
    /**
     * Save the given AVMNodeProperty.
     * @param prop
     */
    public void save(AVMNodeProperty prop);
    
    /**
     * Get an AVMNodeProperty by owner and name.
     * @param owner An AVMNode.
     * @param name The QName.
     * @return The found AVMNodeProperty or null if not found.
     */
    public AVMNodeProperty get(AVMNode owner, QName name);
    
    /**
     * Get a List of all properties for an owning node.
     * @param node The owning node.
     * @return A List of properties belonging to the given node.
     */
    public List<AVMNodeProperty> get(AVMNode node);

    /**
     * Update a property entry.
     * @param prop The property.
     */
    public void update(AVMNodeProperty prop);
    
    /**
     * Delete all properties associated with a node.
     * @param node The AVMNode whose properties should be deleted.
     */
    public void deleteAll(AVMNode node);
    
    /**
     * Delete the given property from the given node.
     * @param node The node to delete the property to delete.
     * @param name The name of the property to delete.
     */
    public void delete(AVMNode node, QName name);
}
