/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
     * Setter for list of model URI's
     * @param editableURIs List<String>
     */
    public void setEditableURIs(List<String> editableURIs)
    {
        this.editableURIs = editableURIs;
    }

    /**
     * Getter for list of model URI's
     * @return return the list of model URI's
     */
    public List<String> getEditableURIs()
    {
        return editableURIs;
    }

    /**
     * Setter for list of qnames
     * @param whiteList List<QName>
     */
    public void setWhiteList(List<QName> whiteList)
    {
        this.whiteList = whiteList;
    }

    /**
     * Compares the node properties with the requested update to make sure all potential updates are permitted
     * @param before current node properties
     * @param after updated properties for the node
     * @return true -  if all modified property keys are in the whitelist
     */
    public boolean check(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        boolean proceed = true;
        HashSet<QName> unionKeys = new HashSet<>(before.keySet());
        unionKeys.addAll(after.keySet());
        for (QName key : unionKeys)
        {
            //Check if property has been added or removed
            if (!before.containsKey(key)  || !after.containsKey(key))
            {
                //Property modified check to see if allowed
                proceed = whiteList.contains(key);
                if (!proceed)
                {
                    break;
                }
            }
            //Check if property emptied or empty property filled
            if  ((before.get(key) == null && after.get(key) != null) ||
                    (after.get(key) == null && before.get(key) != null))
            {
                //Property modified check to see if allowed
                proceed = whiteList.contains(key);
                if (!proceed)
                {
                    break;
                }
            }
            //If properties aren't missing or empty check equality
            if (before.get(key) != null && after.get(key) != null && !(after.get(key).equals(before.get(key))))
            {
                //Property modified check to see if allowed
                proceed = whiteList.contains(key) || getEditableURIs().contains(key.getNamespaceURI());
                if (!proceed)
                {
                    break;
                }
            }
        }
        return proceed;
    }


}
