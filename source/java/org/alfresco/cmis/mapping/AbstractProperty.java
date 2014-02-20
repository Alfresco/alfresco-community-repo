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
import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserExpressionAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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

    public <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws E
    {
        return null;
    }

    public <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E
    {
        return null;
    }

    public <Q, S, E extends Throwable> Q buildLuceneGreaterThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws E
    {
        return null;
    }

    public <Q, S, E extends Throwable> Q buildLuceneGreaterThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws E
    {
        return null;
    }

    public <Q, S, E extends Throwable> Q buildLuceneIn(LuceneQueryParserAdaptor<Q, S, E> lqpa, Collection<Serializable> values, Boolean not,
            PredicateMode mode) throws E
    {
        LuceneQueryParserExpressionAdaptor<Q, E> expressionAdaptor = lqpa.getExpressionAdaptor();
        for(Serializable value : values)
        {
            expressionAdaptor.addOptional(buildLuceneEquality(lqpa, value, mode, LuceneFunction.FIELD));
        }
        if(not)
        {
            return expressionAdaptor.getNegatedQuery();
        }
        else
        {
            return expressionAdaptor.getQuery();
        }
    }

    public  <Q, S, E extends Throwable> Q buildLuceneInequality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws E
    {
        return lqpa.getNegatedQuery(buildLuceneEquality(lqpa, value, mode, luceneFunction));
    }

    public <Q, S, E extends Throwable> Q buildLuceneLessThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws E
    {
        return null;
    }

    public <Q, S, E extends Throwable> Q buildLuceneLessThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode,
            LuceneFunction luceneFunction) throws E
    {
        return null;
    }

    public <Q, S, E extends Throwable> Q buildLuceneLike(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, Boolean not) throws E
    {
        return null;
    }

    public String getLuceneFieldName()
    {
        throw new UnsupportedOperationException();
    }

    public <Q, S, E extends Throwable> String getLuceneSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa) throws E
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
