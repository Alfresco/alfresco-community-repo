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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.cmis.CMISQueryException;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.search.impl.lucene.AnalysisMode;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

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
        super(serviceRegistry, PropertyIds.BASE_TYPE_ID);
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
    public Query buildLuceneEquality(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return lqp.getFieldQuery("TYPE", getBaseType(getValueAsString(value)), AnalysisMode.IDENTIFIER, luceneFunction);     
    }

    @Override
    public Query buildLuceneInequality(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return lqp.getDoesNotMatchFieldQuery("TYPE", getBaseType(getValueAsString(value)), AnalysisMode.IDENTIFIER, luceneFunction);
    }
    
    @Override
    public Query buildLuceneIn(LuceneQueryParser lqp, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException
    {
        String field = "TYPE";
        
        // Check type conversion

       
        Collection<String> asStrings = DefaultTypeConverter.INSTANCE.convert(String.class, values);

        if (asStrings.size() == 0)
        {
            if (not)
            {
                return new MatchAllDocsQuery();
            }
            else
            {
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        }
        else if (asStrings.size() == 1)
        {
            String value = asStrings.iterator().next();
            if (not)
            {
                return lqp.getDoesNotMatchFieldQuery(field, getBaseType(value), AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);
            }
            else
            {
                return lqp.getFieldQuery(field, getBaseType(value), AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);
            }
        }
        else
        {
            BooleanQuery booleanQuery = new BooleanQuery();
            if (not)
            {
                booleanQuery.add(new MatchAllDocsQuery(), Occur.MUST);
            }
            for (String value : asStrings)
            {
                Query any = lqp.getFieldQuery(field, getBaseType(value), AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);
                if (not)
                {
                    booleanQuery.add(any, Occur.MUST_NOT);
                }
                else
                {
                    booleanQuery.add(any, Occur.SHOULD);
                }
            }
            return booleanQuery;
        }
    }

    @Override
    public Query buildLuceneExists(LuceneQueryParser lqp, Boolean not) throws ParseException
    {
        if (not)
        {
            return new TermQuery(new Term("NO_TOKENS", "__"));
        }
        else
        { 
            return new MatchAllDocsQuery();
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
