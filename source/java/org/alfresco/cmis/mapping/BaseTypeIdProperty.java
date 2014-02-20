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

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISQueryException;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.search.adaptor.lucene.AnalysisMode;
import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Get the CMIS object type id property
 * 
 * @author andyh
 */
public class BaseTypeIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public BaseTypeIdProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_BASE_TYPE_ID);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        QName type = getServiceRegistry().getNodeService().getType(nodeRef);
        return getServiceRegistry().getCMISDictionaryService().findTypeForClass(type).getBaseType().getTypeId().getId();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service.cmr.repository.AssociationRef)
     */
    public Serializable getValue(AssociationRef assocRef)
    {
        QName type = assocRef.getTypeQName();
        return getServiceRegistry().getCMISDictionaryService().findTypeForClass(type, CMISScope.RELATIONSHIP).getBaseType().getTypeId().getId();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        return lqpa.getFieldQuery("TYPE", getBaseType(getValueAsString(value)), AnalysisMode.IDENTIFIER, luceneFunction);     
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E
    {
        if (not)
        {
            return lqpa.getMatchNoneQuery();
        }
        else
        { 
            return lqpa.getMatchAllQuery();
        }
    }

    
    private String getBaseType(String tableName)
    {
        CMISTypeDefinition typeDef = getServiceRegistry().getCMISDictionaryService().findTypeByQueryName(tableName);
        if (typeDef == null)
        {
            throw new CMISQueryException("Unknwon base type: " + tableName);
        }
        if(!typeDef.getBaseType().equals(typeDef))
        {
            throw new CMISQueryException("Not a base type: " + tableName);
        }
        if(!typeDef.isQueryable())
        {
            throw new CMISQueryException("Base type is not queryable: " + tableName);
        }
        return typeDef.getTypeId().getQName().toString();
    }
    
    private String getValueAsString(Serializable value)
    {
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        return asString;
    }
}
