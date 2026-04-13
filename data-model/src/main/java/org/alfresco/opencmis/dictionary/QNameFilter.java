/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.opencmis.dictionary;

import java.util.Collection;

import org.alfresco.service.namespace.QName;

/**
 * Filters QNames and excludes any
 * that are in a predefined list.
 *
 * @author steveglover
 * @author Gethin James
 */
public interface QNameFilter
{
    public static final String WILDCARD = "*";
    
    /**
     * Filters out any QName defined in the "excludedTypes" property
     * 
     * @param typesToFilter - original list
     * @return the filtered list
     */
    Collection<QName> filterQName(Collection<QName> typesToFilter);
    
    /**
     * Indicates that this QName should be excluded.
     * @param typeQName QName
     * @return boolean true if it is excluded
     */
    boolean isExcluded(QName typeQName);
    
    /**
     * Exclude the type
     * 
     * @param typeQName the type to exclude
     * 
     */
    void setExcluded(QName typeQName, boolean excluded);
    
    void initFilter();
}
