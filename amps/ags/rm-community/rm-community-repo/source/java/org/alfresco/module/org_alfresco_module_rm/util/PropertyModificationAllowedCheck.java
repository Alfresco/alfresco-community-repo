/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.util;

import static java.util.Collections.unmodifiableList;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Utility check for modification of a properties based off presence in a whitelist.
 *
 * @author Ross Gale
 * @since 3.2
 */
public class PropertyModificationAllowedCheck
{
    /**
     * List of qnames that can be modified
     */
    private List<QName> whiteList;

    /**
     * List of model URI's for which the properties can be updated
     */
    private List<String> editableURIs;

    /**
     * Getter for list of model URI's
     *
     * @return return the list of model URI's
     */
    private List<String> getEditableURIs()
    {
        return unmodifiableList(editableURIs);
    }

    /**
     * Setter for list of model URI's
     *
     * @param editableURIs List<String>
     */
    public void setEditableURIs(List<String> editableURIs)
    {
        this.editableURIs = unmodifiableList(editableURIs);
    }

    /**
     * Setter for list of qnames
     *
     * @param whiteList List<QName>
     */
    public void setWhiteList(List<QName> whiteList)
    {
        this.whiteList = unmodifiableList(whiteList);

    }

    /**
     * Compares the node properties with the requested update to make sure all potential updates are permitted
     *
     * @param before current node properties
     * @param after  updated properties for the node
     * @return true -  if all modified property keys are in the whitelist or
     *                    in the list of model URI's for which the properties can be modified
     */
    public boolean check(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        boolean proceed = true;
        // Initially check for changes to existing keys and values.
        for (final Map.Entry<QName, Serializable> entry : before.entrySet())
        {
            final QName key = entry.getKey();
            final Serializable beforeValue = entry.getValue();
            //check if property has been updated
            final boolean modified = after.containsKey(key) && after.get(key) != null
                    && !after.get(key).equals(beforeValue);

            //check if the property has been emptied or removed
            final boolean propertyRemovedEmptied = (after.get(key) == null && beforeValue != null)
                                            || !after.containsKey(key);
            if (modified || propertyRemovedEmptied)
            {
                proceed = allowPropertyUpdate(key);
            }
            if (!proceed)
            {
                return proceed;
            }
        }

        // Check for new values. Record individual values and group as a single map.
        final Set<QName> newKeys = new HashSet<>(after.keySet());
        newKeys.removeAll(before.keySet());
        for (final QName key : newKeys)
        {
            proceed = allowPropertyUpdate(key);
            if (!proceed)
            {
                break;
            }
        }
        return proceed;
    }

    /**
     * Determines whether the property should be allowed to be updated or not.
     *
     * @param key property
     * @return true if property update is allowed
     */
    private boolean allowPropertyUpdate(QName key)
    {
        return whiteList.contains(key) || getEditableURIs().contains(key.getNamespaceURI());
    }

}
