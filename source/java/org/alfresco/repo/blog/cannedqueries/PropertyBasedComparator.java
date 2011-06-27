/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.blog.cannedqueries;

import java.util.Comparator;

import org.alfresco.repo.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Utility class to sort {@link BlogPostInfo}s on the basis of a Comparable property.
 * Comparisons of two null properties are considered 'equal' by this comparator.
 * Comparisons involving one null and one non-null property will return the null property as
 * being 'before' the non-null property.
 * 
 * Note that it is the responsibility of the calling code to ensure that the specified
 * property values actually implement Comparable themselves.
 */
class PropertyBasedComparator implements Comparator<BlogPostInfo>
{
    private QName comparableProperty;
    private NodeService nodeService;
    
    public PropertyBasedComparator(QName comparableProperty, NodeService nodeService)
    {
        this.comparableProperty = comparableProperty;
        this.nodeService = nodeService;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public int compare(BlogPostInfo nr1, BlogPostInfo nr2)
    {
        Comparable prop1 = (Comparable) nodeService.getProperty(nr1.getNodeRef(), comparableProperty);
        Comparable prop2 = (Comparable) nodeService.getProperty(nr2.getNodeRef(), comparableProperty);
        
        if (prop1 == null && prop2 == null)
        {
            return 0;
        }
        else if (prop1 == null && prop2 != null)
        {
            return -1;
        }
        else if (prop1 != null && prop2 == null)
        {
            return 1;
        }
        else
        {
            return prop1.compareTo(prop2);
        }
    }
}