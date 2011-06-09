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

import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.AnalysisMode;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * Common support for lucene query building.
 * 
 * @author andyh
 *
 */
public abstract class AbstractSimpleProperty extends AbstractProperty
{

    protected AbstractSimpleProperty(ServiceRegistry serviceRegistry, String propertyName)
    {
        super(serviceRegistry, propertyName);
    }

    protected abstract String getValueAsString(Serializable value);
    
    protected String getRangeMax()
    {
        return "\uFFFF";
    }
    
    protected String getRangeMin()
    {
        return "\u0000";
    }
    
    protected abstract DataTypeDefinition getInDataType();
    
    protected abstract QName getQNameForExists();
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneEquality(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        return lqp.getFieldQuery(getLuceneFieldName(), getValueAsString(value), AnalysisMode.IDENTIFIER, luceneFunction);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneExists(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.Boolean)
     */
    public Query buildLuceneExists(AbstractLuceneQueryParser lqp, Boolean not) throws ParseException
    {
        if (not)
        {
            return lqp.getFieldQuery("ISNULL", getQNameForExists().toString(), AnalysisMode.DEFAULT, LuceneFunction.FIELD);
        }
        else
        {
            return lqp.getFieldQuery("ISNOTNULL", getQNameForExists().toString(), AnalysisMode.DEFAULT, LuceneFunction.FIELD);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneGreaterThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneGreaterThan(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(value);
        return lqp.getRangeQuery(field, stringValue, getRangeMax(), false, true, AnalysisMode.IDENTIFIER, luceneFunction);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneGreaterThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneGreaterThanOrEquals(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(value);
        return lqp.getRangeQuery(field, stringValue, getRangeMax(), true, true, AnalysisMode.IDENTIFIER, luceneFunction);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneIn(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.util.Collection, java.lang.Boolean, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneIn(AbstractLuceneQueryParser lqp, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException
    {
        String field = getLuceneFieldName();
        
        // Check type conversion

        @SuppressWarnings("unused")
        Object converted = DefaultTypeConverter.INSTANCE.convert(getInDataType(), values);
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
                return lqp.getDoesNotMatchFieldQuery(field, value, AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);
            }
            else
            {
                return lqp.getFieldQuery(field, value, AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);
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
                Query any = lqp.getFieldQuery(field, value, AnalysisMode.IDENTIFIER, LuceneFunction.FIELD);
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneInequality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneInequality(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(value);
        return lqp.getDoesNotMatchFieldQuery(field, stringValue, AnalysisMode.IDENTIFIER, luceneFunction);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLessThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneLessThan(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(value);
        return lqp.getRangeQuery(field, getRangeMin(), stringValue, true, false, AnalysisMode.IDENTIFIER, luceneFunction);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLessThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneLessThanOrEquals(AbstractLuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(value);
        return lqp.getRangeQuery(field, getRangeMin(), stringValue, true, true, AnalysisMode.IDENTIFIER, luceneFunction);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#buildLuceneLike(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.io.Serializable, java.lang.Boolean)
     */
    public Query buildLuceneLike(AbstractLuceneQueryParser lqp, Serializable value, Boolean not) throws ParseException
    {
        String field = getLuceneFieldName();
        String stringValue = getValueAsString(value);

        if (not)
        {
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new MatchAllDocsQuery(), Occur.MUST);
            booleanQuery.add(lqp.getLikeQuery(field, stringValue, AnalysisMode.IDENTIFIER), Occur.MUST_NOT);
            return booleanQuery;
        }
        else
        {
            return lqp.getLikeQuery(field, stringValue, AnalysisMode.IDENTIFIER);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.property.PropertyLuceneBuilder#getLuceneSortField()
     */
    public String getLuceneSortField(AbstractLuceneQueryParser lqp)
    {
        return getLuceneFieldName();
    }
}
