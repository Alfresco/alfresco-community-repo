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

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * Alfresco Properties for AVM..
 * @author britt
 */
public interface AVMNodeProperty
{
    /**
     * Set the node that owns this property.
     * @param node The AVMNode.
     */
    public void setNode(AVMNode node);
    
    /**
     * Get the node that owns this property.
     * @return An AVMNode.
     */
    public AVMNode getNode();
    
    /**
     * Get the name for this property.
     * @return A QName.
     */
    public QName getName();
    
    /**
     * Set the name for the property.
     * @param id A QName.
     */
    public void setName(QName id);
    
    /**
     * Get the actual property value.
     * @return A PropertyValue.
     */
    public PropertyValue getValue();
    
    /**
     * Set the value of this property.
     * @param value A PropertyValue.
     */
    public void setValue(PropertyValue value);
}
