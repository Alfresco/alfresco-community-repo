/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search.impl.parsers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.adaptor.lucene.QueryConstants;
import org.alfresco.repo.search.impl.querymodel.FunctionArgument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Alfrecso function evaluation context for evaluating FTS expressions against lucene.
 * 
 * @author andyh
 */
@SuppressWarnings("deprecation")
public class AlfrescoFunctionEvaluationContext implements FunctionEvaluationContext
{
    private static HashSet<String> EXPOSED_FIELDS = new HashSet<String>();

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

    private String defaultNamespace;

    static
    {
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PATH);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TEXT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_SOLR4_ID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ISROOT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ISNODE);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TX);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PARENT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PRIMARYPARENT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_QNAME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_CLASS);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TYPE);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_EXACTTYPE);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ASPECT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_EXACTASPECT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ALL);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_EXISTS);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ISUNSET);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ISNULL);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ISNOTNULL);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_FTSSTATUS);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_FTSREF);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ASSOCTYPEQNAME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PRIMARYASSOCTYPEQNAME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PRIMARYASSOCQNAME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_DBID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TAG);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ACLID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_OWNER);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_READER);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_AUTHORITY);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_OWNERSET);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_READERSET);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_AUTHORITYSET);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_DENIED);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_DENYSET);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TXID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ACLTXID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TXCOMMITTIME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ACLTXCOMMITTIME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_INACLTXID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_INTXID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TENANT);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ANCESTOR);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_EXCEPTION_MESSAGE);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_EXCEPTION_STACK);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_LID);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PARENT_ASSOC_CRC);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_SITE);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_TAG);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PNAME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_NPATH);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_ANAME);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_APATH);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_DOC_TYPE);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_PROPERTIES);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_NULLPROPERTIES);
        EXPOSED_FIELDS.add(QueryConstants.FIELD_FINGERPRINT);
    }

    /**
     * @param namespacePrefixResolver NamespacePrefixResolver
     * @param dictionaryService DictionaryService
     * @param defaultNamespace String
     */
    public AlfrescoFunctionEvaluationContext(NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String defaultNamespace)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
        this.dictionaryService = dictionaryService;
        this.defaultNamespace = defaultNamespace;
    }

    public  <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Boolean not) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneGreaterThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneGreaterThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, PredicateMode mode, LuceneFunction luceneFunction)
    throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneIn(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Collection<Serializable> values, Boolean not, PredicateMode mode) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneInequality(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneLessThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneLessThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, PredicateMode mode, LuceneFunction luceneFunction)
    throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> Q buildLuceneLike(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName, Serializable value, Boolean not) throws E
    {
        throw new UnsupportedOperationException();
    }

    public  <Q, S, E extends Throwable> String getLuceneSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa, String propertyName) throws E
    {
        // Score is special
        if (propertyName.equalsIgnoreCase("Score"))
        {
            return "Score";
        }
        String field = getLuceneFieldName(propertyName);
        // need to find the real field to use
        if (field.startsWith(QueryConstants.PROPERTY_FIELD_PREFIX))
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(field.substring(1)));

            // Handle .size and .mimetype
            if(propertyDef == null)
            {   
                if(field.endsWith(QueryConstants.FIELD_SIZE_SUFFIX))
                {
                    propertyDef = dictionaryService.getProperty(QName.createQName(field.substring(1, field.length()-QueryConstants.FIELD_SIZE_SUFFIX.length())));
                    if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                    {
                        throw new FTSQueryException("Order for "+QueryConstants.FIELD_SIZE_SUFFIX+" only supported on content properties");
                    }
                    else
                    {
                        return field;
                    }
                }
                else if (field.endsWith(QueryConstants.FIELD_MIMETYPE_SUFFIX))
                {
                    propertyDef = dictionaryService.getProperty(QName.createQName(field.substring(1, field.length()-QueryConstants.FIELD_MIMETYPE_SUFFIX.length())));
                    if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                    {
                        throw new FTSQueryException("Order for .mimetype only supported on content properties");
                    }
                    else
                    {
                        return field;
                    }
                }
                else
                {
                    return field;
                }
            }
            else
            {
                if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                {
                    throw new FTSQueryException("Order on content properties is not curently supported");
                }
                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT))
                {
                    if(propertyDef.getIndexTokenisationMode() == IndexTokenisationMode.FALSE)
                    {
                        return field;
                    }

                    String noLocalField = field+".no_locale";
                    if(lqpa.sortFieldExists(noLocalField))
                    {
                        return noLocalField;
                    }
                    field = findSortField(lqpa, field);
                }
                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
                {
                    field = findSortField(lqpa, field);

                }
                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
                {
                    field = lqpa.getDatetimeSortField(field, propertyDef);
                }
            }
        }
        return field;
    }

    /**
     * @param lqpa LuceneQueryParserAdaptor<Q, S, E>
     * @param field String
     * @return ... extends Throwable
     * @throws E 
     */
    private  <Q, S, E extends Throwable> String findSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa, String field) throws E
    {
        return lqpa.getSortField(field);
    }

    public Map<String, NodeRef> getNodeRefs()
    {
        throw new UnsupportedOperationException();
    }

    public NodeService getNodeService()
    {
        throw new UnsupportedOperationException();
    }

    public Serializable getProperty(NodeRef nodeRef, String propertyName)
    {
        throw new UnsupportedOperationException();
    }

    public Float getScore()
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Float> getScores()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isObjectId(String propertyName)
    {
        return false;
    }

    public boolean isOrderable(String fieldName)
    {
        return true;
    }

    public boolean isQueryable(String fieldName)
    {
        return true;
    }

    public String getLuceneFieldName(String propertyName)
    {
        if (propertyName.startsWith(QueryConstants.PROPERTY_FIELD_PREFIX))
        {
            // Leave it to the query parser to expand
            return propertyName;
        }

        if (propertyName.startsWith("{"))
        {
            QName fullQName = QName.createQName(propertyName);
            QName propertyQName = stripSuffixes(fullQName);
            if (dictionaryService.getProperty(propertyQName) != null)
            {
                return QueryConstants.PROPERTY_FIELD_PREFIX + fullQName.toString();
            }
            else if(dictionaryService.getDataType(fullQName) != null)
            {
                return fullQName.toString();
            }
            else
            {
                throw new FTSQueryException("Unknown property: " + fullQName.toString());
            }
        }

        int index = propertyName.indexOf(':');
        if (index != -1)
        {
            // Try as a property, if invalid pass through
            QName fullQName = QName.createQName(propertyName, namespacePrefixResolver);
            QName propertyQName = stripSuffixes(fullQName);
            if (dictionaryService.getProperty(propertyQName) != null)
            {
                return QueryConstants.PROPERTY_FIELD_PREFIX + fullQName.toString();
            }
            else if(dictionaryService.getDataType(fullQName) != null)
            {
                return fullQName.toString();
            }
            else
            {
                throw new FTSQueryException("Unknown property: " + fullQName.toString());
            }
        }

        if (!EXPOSED_FIELDS.contains(propertyName))
        {
            index = propertyName.indexOf('_');
            if (index != -1)
            {
                // Try as a property, if invalid pass through
                QName fullQName = QName.createQName(propertyName.substring(0, index), propertyName.substring(index + 1), namespacePrefixResolver);
                QName propertyQName = stripSuffixes(fullQName);
                if (dictionaryService.getProperty(propertyQName) != null)
                {
                    return QueryConstants.PROPERTY_FIELD_PREFIX + fullQName.toString();
                }
                else
                {
                    throw new FTSQueryException("Unknown property: " + fullQName.toString());
                }
            }
        }

        if (EXPOSED_FIELDS.contains(propertyName))
        {
            return propertyName;
        }

        QName fullQName = QName.createQName(defaultNamespace, propertyName);
        QName propertyQName = stripSuffixes(fullQName);
        if (dictionaryService.getProperty(propertyQName) != null)
        {
            return QueryConstants.PROPERTY_FIELD_PREFIX + fullQName.toString();
        }
        else if(dictionaryService.getDataType(fullQName) != null)
        {
            return fullQName.toString();
        }
        else
        {
            if(propertyName.equalsIgnoreCase("Score"))
            {
                return propertyName.toLowerCase();
            }
                    
            throw new FTSQueryException("Unknown property: " + fullQName.toString());
        }

    }

    public QName stripSuffixes(QName qname)
    {
        String field = qname.toString();
        if(field.endsWith(QueryConstants.FIELD_SIZE_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_SIZE_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_SIZE_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else if(field.endsWith(QueryConstants.FIELD_LOCALE_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_LOCALE_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_LOCALE_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else if(field.endsWith(QueryConstants.FIELD_MIMETYPE_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_MIMETYPE_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_MIMETYPE_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else if(field.endsWith(QueryConstants.FIELD_ENCODING_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_ENCODING_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_ENCODING_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else if(field.endsWith(QueryConstants.FIELD_TRANSFORMATION_EXCEPTION_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_TRANSFORMATION_EXCEPTION_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_TRANSFORMATION_EXCEPTION_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else if(field.endsWith(QueryConstants.FIELD_TRANSFORMATION_STATUS_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_TRANSFORMATION_STATUS_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_TRANSFORMATION_STATUS_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else if(field.endsWith(QueryConstants.FIELD_TRANSFORMATION_TIME_SUFFIX))
        {
            QName propertyField = QName.createQName(field.substring(0, field.length()-QueryConstants.FIELD_TRANSFORMATION_TIME_SUFFIX.length()));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyField);
            if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
            {
                throw new FTSQueryException(QueryConstants.FIELD_TRANSFORMATION_TIME_SUFFIX+" only supported on content properties");
            }
            else
            {
                return propertyField;
            }
        }
        else
        {
            return qname;
        }
    }

    public LuceneFunction getLuceneFunction(FunctionArgument functionArgument)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext#checkFieldApplies(org.alfresco.service.namespace
     * .QName, java.lang.String)
     */
    public void checkFieldApplies(Selector selector, String propertyName)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext#isMultiValued(java.lang.String)
     */
    public boolean isMultiValued(String propertyName)
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext#getAlfrescoQName(org.alfresco.service.namespace.QName)
     */
    @Override
    public String getAlfrescoPropertyName(String propertyName)
    {
        return propertyName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext#getAlfrescoTypeName(java.lang.String)
     */
    @Override
    public String getAlfrescoTypeName(String typeName)
    {
        return typeName;
    }

}
