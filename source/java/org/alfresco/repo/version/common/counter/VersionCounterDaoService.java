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
package org.alfresco.repo.version.common.counter;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Version counter DAO service interface.
 * 
 * @author Roy Wetherall
 */
public interface VersionCounterDaoService
{
    /**
     * Helper method for simple patching
     * 
     * @param nodeTypeQName not used
     * @param storeRef the store to create a counter for
     */
    public void beforeCreateStore(QName nodeTypeQName, StoreRef storeRef);
    
    /**
     * Get the next available version number for the specified store.
     * 
     * @param storeRef  the store reference
     * @return          the next version number
     */
    public int nextVersionNumber(StoreRef storeRef);   
    
    /**
     * Gets the current version number for the specified store.
     * 
     * @param storeRef  the store reference
     * @return          the current versio number
     */
    public int currentVersionNumber(StoreRef storeRef);
    
    /**
     * Resets the version number for a the specified store.
     * 
     * WARNING: calling this method will completely reset the current 
     * version count for the specified store and cannot be undone.  
     *
     * @param storeRef  the store reference
     */
    public void resetVersionNumber(StoreRef storeRef);
}
