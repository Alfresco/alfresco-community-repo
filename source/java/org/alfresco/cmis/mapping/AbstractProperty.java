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

import org.alfresco.cmis.CMISPropertyAccessor;
import org.alfresco.cmis.CMISPropertyLuceneBuilder;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

/**
 * Base class for all property accessors
 * 
 * @author andyh
 *
 */
public abstract class AbstractProperty implements CMISPropertyAccessor, CMISPropertyLuceneBuilder
{
    private ServiceRegistry serviceRegistry;
    private String propertyName;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param propertyName
     */
    protected AbstractProperty(ServiceRegistry serviceRegistry, String propertyName)
    {
        this.serviceRegistry = serviceRegistry;
        this.propertyName = propertyName;
    }

    /**
     * @return  service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getName()
     */
    public String getName()
    {
        return propertyName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyAccessor#getMappedProperty()
     */
    public QName getMappedProperty()
    {
        return null;
    }

    
    public Query buildLuceneEquality(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneExists(LuceneQueryParser lqp, Boolean not) throws ParseException
    {
        return null;
    }

    public Query buildLuceneGreaterThan(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneGreaterThanOrEquals(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneIn(LuceneQueryParser lqp, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException
    {
        return null;
    }

    public Query buildLuceneInequality(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneLessThan(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneLessThanOrEquals(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneLike(LuceneQueryParser lqp, Serializable value, Boolean not) throws ParseException
    {
        return null;
    }

    public String getLuceneFieldName()
    {
        throw new UnsupportedOperationException();
    }

    public String getLuceneSortField(LuceneQueryParser lqp)
    {
        throw new UnsupportedOperationException();
    }

    public Serializable getValue(NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }
    
    public void setValue(NodeRef nodeRef, Serializable value)
    {
        throw new UnsupportedOperationException();
    }
    
    public Serializable getValue(AssociationRef assocRef)
    {
        throw new UnsupportedOperationException();
    }
}
