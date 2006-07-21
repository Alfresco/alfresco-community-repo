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
 * Arbitrary properties associated with AVMStores.
 * @author britt
 */
public interface AVMStoreProperty
{
    /**
     * Set the AVMStore.
     * @param store The AVMStore to set.
     */
    public void setStore(AVMStore store);
    
    /**
     * Get the AVMStore.
     * @return The AVMStore this property belongs to.
     */
    public AVMStore getStore();
    
    /**
     * Set the name of the property.
     * @param name The QName for the property.
     */
    public void setName(QName name);
    
    /**
     * Get the name of this property.
     * @return The QName of this property.
     */
    public QName getName();
    
    /**
     * Set the actual property value.
     * @param value The PropertyValue to set.
     */
    public void setValue(PropertyValue value);
    
    /**
     * Get the actual property value.
     * @return The actual PropertyValue.
     */
    public PropertyValue getValue();
}
