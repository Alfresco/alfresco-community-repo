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

import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Utility class to check if a node type is instance of another type
 *
 * @author Claudia Agache
 * @since 3.2
 */
public class NodeTypeUtility
{
    /** Static cache for results of types that are instances of other Alfresco types. */
    private static ConcurrentHashMap<String, Boolean> instanceOfCache = new ConcurrentHashMap<>();

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Utility method to quickly determine whether one class is equal to or sub of another.
     *
     * @param className     class name
     * @param ofClassName   class name to check against
     * @return boolean      true if equal to or sub, false otherwise
     */
    public boolean instanceOf(QName className, QName ofClassName)
    {
        ParameterCheck.mandatory("className", className);
        ParameterCheck.mandatory("ofClassName", ofClassName);

        String key = className.toString() + "|" + ofClassName.toString();
        return instanceOfCache.computeIfAbsent(key, k ->
                (ofClassName.equals(className) || dictionaryService.isSubClass(className, ofClassName)));
    }
}
