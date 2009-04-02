/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.mapping;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.lucene.search.Query;

/**
 * Get the CMIS version series checked out property
 * 
 * @author andyh
 */
public class IsVersionSeriesCheckedOutPropertyAccessor extends AbstractPropertyAccessor
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public IsVersionSeriesCheckedOutPropertyAccessor(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        if (getServiceRegistry().getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            return true;
        }
        else
        {
            LockType type = getServiceRegistry().getLockService().getLockType(nodeRef);
            if (type == LockType.READ_ONLY_LOCK)
            {
                NodeRef wc = getServiceRegistry().getCheckOutCheckInService().getWorkingCopy(nodeRef);
                return (wc != null);
            }
            else
            {
                return false;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#setValue(org.alfresco.service.cmr.repository.NodeRef, java.io.Serializable)
     */
    public void setValue(NodeRef nodeRef, Serializable value)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, Serializable value, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneExists(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.Boolean)
     */
    public Query buildLuceneExists(LuceneQueryParser lqp, Boolean not)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneGreaterThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneGreaterThan(LuceneQueryParser lqp, Serializable value, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneGreaterThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneGreaterThanOrEquals(LuceneQueryParser lqp, Serializable value, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneIn(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.util.Collection, java.lang.Boolean, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneIn(LuceneQueryParser lqp, Collection<Serializable> values, Boolean not, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneInequality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneInequality(LuceneQueryParser lqp, Serializable value, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLessThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneLessThan(LuceneQueryParser lqp, Serializable value, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLessThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneLessThanOrEquals(LuceneQueryParser lqp, Serializable value, PredicateMode mode)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLike(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, java.lang.Boolean)
     */
    public Query buildLuceneLike(LuceneQueryParser lqp, Serializable value, Boolean not)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#getLuceneSortField()
     */
    public String getLuceneSortField()
    {
        throw new UnsupportedOperationException();
    }
}
