/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.cmis.mapping;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.cmis.CMISPropertyAccessor;
import org.alfresco.opencmis.dictionary.CMISPropertyLuceneBuilder;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
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
     * @return service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.PropertyAccessor#getName()
     */
    public String getName()
    {
        return propertyName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.PropertyAccessor#getMappedProperty()
     */
    public QName getMappedProperty()
    {
        return null;
    }

    public Query buildLuceneEquality(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneExists(AbstractLuceneQueryParser lqp, Boolean not) throws ParseException
    {
        return null;
    }

    public Query buildLuceneGreaterThan(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneGreaterThanOrEquals(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneIn(AbstractLuceneQueryParser lqp, Collection<Serializable> values, Boolean not,
            PredicateMode mode) throws ParseException
    {
        return null;
    }

    public Query buildLuceneInequality(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneLessThan(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneLessThanOrEquals(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws ParseException
    {
        return null;
    }

    public Query buildLuceneLike(AbstractLuceneQueryParser lqp, Serializable value, Boolean not) throws ParseException
    {
        return null;
    }

    public String getLuceneFieldName()
    {
        throw new UnsupportedOperationException();
    }

    public String getLuceneSortField(AbstractLuceneQueryParser lqp)
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
