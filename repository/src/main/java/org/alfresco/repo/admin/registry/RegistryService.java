/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.admin.registry;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface for service providing access to key-value pairs for storage of system-controlled metadata.
 * 
 * @author Derek Hulley
 */
public interface RegistryService
{
    /**
     * Assign a value to the registry key, which must be of the form <b>/a/b/c</b>.
     * 
     * @param key
     *            the registry key.
     * @param value
     *            any value that can be stored in the repository.
     */
    void addProperty(RegistryKey key, Serializable value);

    /**
     * @param key
     *            the registry key.
     * @return Returns the value stored in the key or <tt>null</tt> if no value exists at the path and name provided
     * 
     * @see #addProperty(RegistryKey, Serializable)
     */
    Serializable getProperty(RegistryKey key);

    /**
     * Fetches all child elements for the given path. The key's property should be <tt>null</tt> as it is completely ignored. <code><pre>
     *    ...
     *    registryService.addValue(KEY_A_B_C_1, VALUE_ONE);
     *    registryService.addValue(KEY_A_B_C_2, VALUE_TWO);
     *    ...
     *    assertTrue(registryService.getChildElements(KEY_A_B_null).contains("C"));
     *    ...
     * </pre></code>
     * 
     * @param key
     *            the registry key with the path. The last element in the path will be ignored, and can be any acceptable value localname or <tt>null</tt>.
     * @return Returns all child elements (not values) for the given key, ignoring the last element in the key.
     * 
     * @see RegistryKey#getPath()
     */
    Collection<String> getChildElements(RegistryKey key);

    /**
     * Copies the path or value from the source to the target location. The source and target keys <b>must</b> be both either path-specific or property-specific. If the source doesn't exist, then nothing will be done; there is no guarantee that the target will exist after the call.
     * <p>
     * This is essentially a merge operation. Use {@link #delete(RegistryKey) delete} first if the target must be cleaned.
     * 
     * @param sourceKey
     *            the source registry key to take values from
     * @param targetKey
     *            the target registyr key to move the path or value to
     */
    void copy(RegistryKey sourceKey, RegistryKey targetKey);

    /**
     * Delete the path element or value described by the key. If the key points to nothing, then nothing is done. <code>delete(/a/b/c)</code> will remove value <b>c</b> from path <b>/a/b</b>.<br/>
     * <code>delete(/a/b/null)</code> will remove node <b>/a/b</b> along with all values and child elements.
     * 
     * @param key
     *            the path or value to delete
     */
    void delete(RegistryKey key);
}
