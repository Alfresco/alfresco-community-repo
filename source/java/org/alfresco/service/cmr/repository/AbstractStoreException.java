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
package org.alfresco.service.cmr.repository;


/**
 * Store-related exception that keeps a handle to the store reference
 * 
 * @author Derek Hulley
 */
public abstract class AbstractStoreException extends RuntimeException
{
    private StoreRef storeRef;
    
    public AbstractStoreException(StoreRef storeRef)
    {
        this(null, storeRef);
    }

    public AbstractStoreException(String msg, StoreRef storeRef)
    {
        super(msg);
        this.storeRef = storeRef;
    }

    /**
     * @return Returns the offending store reference
     */
    public StoreRef getStoreRef()
    {
        return storeRef;
    }
}
