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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.service.cmr.blog.BlogService.BlogPostInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * 
 * @author Neil Mc Erlean, janv
 * @since 4.0
 */
public abstract class AbstractBlogPostsCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<BlogPostInfo>
{
    protected CannedQuerySortDetails createCQSortDetails(QName sortProp, SortOrder sortOrder)
    {
        List<Pair<? extends Object, SortOrder>> singlePair = new ArrayList<Pair<? extends Object, SortOrder>>(1);
        singlePair.add(new Pair<QName, SortOrder>(sortProp, sortOrder));
        
        return new CannedQuerySortDetails(singlePair);
    }
    
    /**
     * Utility class to sort {@link BlogPostInfo}s on the basis of a Comparable property.
     * Comparisons of two null properties are considered 'equal' by this comparator.
     * Comparisons involving one null and one non-null property will return the null property as
     * being 'before' the non-null property.
     * 
     * Note that it is the responsibility of the calling code to ensure that the specified
     * property values actually implement Comparable themselves.
     */
    protected static class BlogEntityComparator extends PropertyBasedComparator<BlogEntity>
    {
        public BlogEntityComparator(QName comparableProperty)
        {
            super(comparableProperty);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Comparable getProperty(BlogEntity entity) {
           if (comparableProperty.equals(ContentModel.PROP_PUBLISHED))
           {
               return entity.getPublishedDate();
           }
           else if (comparableProperty.equals(ContentModel.PROP_CREATED))
           {
               return entity.getCreatedDate();
           }
           else if (comparableProperty.equals(BlogIntegrationModel.PROP_POSTED))
           {
               return entity.getPostedDate();
           }
           else
           {
               throw new IllegalArgumentException("Unsupported blog sort property: "+comparableProperty);
           }
        }
    }
}
