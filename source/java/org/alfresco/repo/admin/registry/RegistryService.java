/*
 * Copyright (C) 2007 Alfresco, Inc.
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
package org.alfresco.repo.admin.registry;

import java.io.Serializable;

/**
 * Interface for service providing access to key-value pairs for storage
 * of system-controlled metadata.
 * 
 * @author Derek Hulley
 */
public interface RegistryService
{
    /**
     * Assign a value to the registry key, which must be of the form <b>/a/b/c</b>.
     * 
     * @param key           the registry key path delimited with '/'.
     * @param value         any value that can be stored in the repository.
     */
    void addValue(String key, Serializable value);
    
    /**
     * @param key           the registry key path delimited with '/'.
     * @return              Returns the value stored in the key.
     * 
     * @see #addValue(String, Serializable)
     */
    Serializable getValue(String key);
}
