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
package org.alfresco.service.cmr.view;

import org.alfresco.service.namespace.QName;


/**
 * Encapsulation of Import binding parameters
 * 
 * @author David Caruana
 */
public interface ImporterBinding
{

    /**
     * UUID Binding 
     */
    public enum UUID_BINDING
    {
        CREATE_NEW, CREATE_NEW_WITH_UUID, REMOVE_EXISTING, REPLACE_EXISTING, UPDATE_EXISTING, THROW_ON_COLLISION
    }

    /**
     * Gets the Node UUID Binding
     * 
     * @return  UUID_BINDING
     */
    public UUID_BINDING getUUIDBinding();

    /**
     * Gets whether the search for imported node references should search within the import
     * transaction or not.
     * 
     * @return true => search within import transaction;  false => only search existing committed items 
     */
    public boolean allowReferenceWithinTransaction();
    
    /**
     * Gets a value for the specified name - to support simple name / value substitution
     * 
     * @param key   the value name
     * @return  the value
     */
    public String getValue(String key);

    /**
     * Gets the list of content model classes to exclude from import
     * 
     * @return  list of model class qnames to exclude (return null to indicate use of default list)
     */
    public QName[] getExcludedClasses();
    
}
