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
package org.alfresco.cmis.property;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.ParseException;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * Generic mapping of CMIS style property names to Alfresco properties (for non CMIS properties)
 * @author andyh
 *
 */
public class MappingPropertyAccessor extends AbstractGenericPropertyAccessor
{
    public Serializable getProperty(NodeRef nodeRef, String propertyName)
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        return getServiceRegistry().getNodeService().getProperty(nodeRef, propertyQname);
    }

    
    
    
    private String getLuceneFieldName(QName propertyQname)
    {
        StringBuilder field = new StringBuilder(64);
        field.append("@");
        field.append(propertyQname);
        return field.toString();
    }

    private String getValueAsString(QName propertyQname, Serializable value)
    {
        PropertyDefinition pd = getServiceRegistry().getDictionaryService().getProperty(propertyQname);
        Object converted = DefaultTypeConverter.INSTANCE.convert(pd.getDataType(), value);
        String asString =  DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        return asString;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        return lqp.getFieldQuery(field, stringValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneExists(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.lang.Boolean)
     */
    public Query buildLuceneExists(LuceneQueryParser lqp, String propertyName, Boolean not) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        if(not)
        {
            return lqp.getFieldQuery("ISNULL", propertyQname.toString());
        }
        else
        {
            return lqp.getFieldQuery("ISNOTNULL", propertyQname.toString());
        }   
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneGreaterThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneGreaterThan(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        return lqp.getRangeQuery(field, stringValue, "\uFFFF", false, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneGreaterThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneGreaterThanOrEquals(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        return lqp.getRangeQuery(field, stringValue, "\uFFFF", true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneIn(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.util.Collection, java.lang.Boolean,
     *      org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneIn(LuceneQueryParser lqp, String propertyName, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        PropertyDefinition pd = getServiceRegistry().getDictionaryService().getProperty(propertyQname);

        // Check type conversion

        @SuppressWarnings("unused")
        Object converted = DefaultTypeConverter.INSTANCE.convert(pd.getDataType(), values);
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
                return lqp.getDoesNotMatchFieldQuery(field, value);
            }
            else
            {
                return lqp.getFieldQuery(field, value);
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
                Query any = lqp.getFieldQuery(field, value);
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
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneInequality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneInequality(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        return lqp.getDoesNotMatchFieldQuery(field, stringValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneLessThan(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneLessThan(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        return lqp.getRangeQuery(field, "\u0000", stringValue, true, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneLessThanOrEquals(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, org.alfresco.repo.search.impl.querymodel.PredicateMode)
     */
    public Query buildLuceneLessThanOrEquals(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        return lqp.getRangeQuery(field, "\u0000", stringValue, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneLike(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.io.Serializable, java.lang.Boolean)
     */
    public Query buildLuceneLike(LuceneQueryParser lqp, String propertyName, Serializable value, Boolean not) throws ParseException
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        String field = getLuceneFieldName(propertyQname);
        String stringValue = getValueAsString(propertyQname, value);
        
        if (not)
        {
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new MatchAllDocsQuery(), Occur.MUST);
            booleanQuery.add(lqp.getLikeQuery(field, stringValue), Occur.MUST_NOT);
            return booleanQuery;
        }
        else
        {
            return lqp.getLikeQuery(field, stringValue);
        }
    }




    /* (non-Javadoc)
     * @see org.alfresco.cmis.property.GenericPropertyAccessor#getLuceneSortField(java.lang.String)
     */
    public String getLuceneSortField(String propertyName)
    {
        QName propertyQname = getCMISMapping().getPropertyQName(propertyName);
        return getLuceneFieldName(propertyQname);
    }
    
    
}
