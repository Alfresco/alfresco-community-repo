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
import java.util.ArrayList;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISQueryException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.adaptor.lucene.AnalysisMode;
import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Get the CMIS object id property.
 * 
 * @author andyh
 * @author dward
 */
public class ObjectIdProperty extends AbstractVersioningProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public ObjectIdProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_OBJECT_ID);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        NodeRef versionSeries;
        if (isWorkingCopy(nodeRef) || (versionSeries = getVersionSeries(nodeRef)).equals(nodeRef))
        {
            return nodeRef.toString();
        }
        else
        {
            return new StringBuilder(1024).append(versionSeries.toString()).append(';').append(
                    getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL))
                    .toString();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public Serializable getValue(AssociationRef assocRef)
    {
        return CMISServices.ASSOC_ID_PREFIX + assocRef.getId();
    }

    public String getLuceneFieldName()
    {
        return "ID";
    }

    private <Q, S, E extends Throwable> StoreRef getStore(LuceneQueryParserAdaptor<Q, S, E> lqpa)
    {
        ArrayList<StoreRef> stores = lqpa.getSearchParameters().getStores();
        if(stores.size() < 1)
        {
            // default
            return StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        }
        return stores.get(0);
    }
    
    private <Q, S, E extends Throwable> String getValueAsString(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value)
    {
        String nodeRefStr = null;
        if(!NodeRef.isNodeRef((String)value))
        {
            // assume the object id is the node guid
            StoreRef storeRef = getStore(lqpa);
            nodeRefStr = storeRef.toString() + "/" + (String)value;
        }
        else
        {
            nodeRefStr = (String)value;
        }

        Object converted = DefaultTypeConverter.INSTANCE.convert(getServiceRegistry().getDictionaryService().getDataType(DataTypeDefinition.NODE_REF), nodeRefStr);
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        return asString;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(lqpa, value);
        return lqpa.getIdentifierQuery(field, stringValue, AnalysisMode.IDENTIFIER, luceneFunction);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneExists(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.Boolean)
     */
    public <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E
    {
        if (not)
        {
            return lqpa.getMatchNoneQuery();
        } else
        {
            return lqpa.getMatchAllQuery();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneGreaterThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
       throw new CMISQueryException("Property " + getName() +" can not be used in a 'greater than' comparison");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneGreaterThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new CMISQueryException("Property " + getName() + " can not be used in a 'greater than or equals' comparison");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLessThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public <Q, S, E extends Throwable> Q buildLuceneLessThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new CMISQueryException("Property " + getName() + " can not be used in a 'less than' comparison");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLessThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public <Q, S, E extends Throwable> Q buildLuceneLessThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new CMISQueryException("Property " + getName() + " can not be used in a 'less than or equals' comparison");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneLike(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, java.lang.Boolean)
     */
    public <Q, S, E extends Throwable> Q buildLuceneLike(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, Boolean not) throws E
    {
        throw new CMISQueryException("Property " + getName() + " can not be used in a 'like' comparison");
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#getLuceneSortField(java.lang.String)
     */
    public <Q, S, E extends Throwable> String getLuceneSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa)
    {
        return getLuceneFieldName();
    }
    
}
