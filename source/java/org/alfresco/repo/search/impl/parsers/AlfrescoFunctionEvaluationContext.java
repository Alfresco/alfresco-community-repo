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
package org.alfresco.repo.search.impl.parsers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

public class AlfrescoFunctionEvaluationContext implements FunctionEvaluationContext
{
    private static HashSet<String> EXPOSED_FIELDS = new HashSet<String>();
    
    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;
    
    private String defaultNamespace;

    static
    {
        EXPOSED_FIELDS.add("PATH");
        EXPOSED_FIELDS.add("TEXT");
        EXPOSED_FIELDS.add("ID");
        EXPOSED_FIELDS.add("ISROOT");
        EXPOSED_FIELDS.add("ISNODE");
        EXPOSED_FIELDS.add("TX");
        EXPOSED_FIELDS.add("PARENT");
        EXPOSED_FIELDS.add("PRIMARYPARENT");
        EXPOSED_FIELDS.add("QNAME");
        EXPOSED_FIELDS.add("CLASS");
        EXPOSED_FIELDS.add("TYPE");
        EXPOSED_FIELDS.add("EXACTTYPE");
        EXPOSED_FIELDS.add("ASPECT");
        EXPOSED_FIELDS.add("EXACTASPECT");
        EXPOSED_FIELDS.add("ALL");
        EXPOSED_FIELDS.add("ISUNSET");
        EXPOSED_FIELDS.add("ISNULL");
        EXPOSED_FIELDS.add("ISNOTNULL");
    }
    
    public AlfrescoFunctionEvaluationContext(NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, String defaultNamespace)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
        this.dictionaryService = dictionaryService;
        this.defaultNamespace = defaultNamespace;
    }

    public Query buildLuceneEquality(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneExists(LuceneQueryParser lqp, String propertyName, Boolean not) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneGreaterThan(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneGreaterThanOrEquals(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneIn(LuceneQueryParser lqp, String propertyName, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneInequality(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneLessThan(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneLessThanOrEquals(LuceneQueryParser lqp, String propertyName, Serializable value, PredicateMode mode) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public Query buildLuceneLike(LuceneQueryParser lqp, String propertyName, Serializable value, Boolean not) throws ParseException
    {
        throw new UnsupportedOperationException();
    }

    public String getLuceneSortField(String propertyName)
    {
        throw new UnsupportedOperationException();
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
        if (propertyName.startsWith("@"))
        {
            // Leave it to the query parser to expand
            return propertyName;
        }

        if(propertyName.startsWith("{"))
        {
            QName qname = QName.createQName(propertyName);
            if (dictionaryService.getProperty(qname) != null)
            {
                return "@" + qname.toString();
            }
            else
            {
                throw new FTSQueryException("Unknown property: "+propertyName);
            }
        }
        
        int index = propertyName.indexOf(':');
        if (index != -1)
        {
            // Try as a property, if invalid pass through
            QName qname = QName.createQName(propertyName, namespacePrefixResolver);
            if (dictionaryService.getProperty(qname) != null)
            {
                return "@" + qname.toString();
            }
            else
            {
                throw new FTSQueryException("Unknown property: "+propertyName);
            }
        }
        
        index = propertyName.indexOf('_');
        if (index != -1)
        {
            // Try as a property, if invalid pass through
            QName qname = QName.createQName(propertyName.substring(0, index), propertyName.substring(index+1), namespacePrefixResolver);
            if (dictionaryService.getProperty(qname) != null)
            {
                return "@" + qname.toString();
            }
            else
            {
                throw new FTSQueryException("Unknown property: "+propertyName);
            }
        }
        
        if(EXPOSED_FIELDS.contains(propertyName))
        {
            return propertyName;
        }
        
        QName qname = QName.createQName(defaultNamespace, propertyName);
        if (dictionaryService.getProperty(qname) != null)
        {
            return "@" + qname.toString();
        }
        else
        {
            throw new FTSQueryException("Unknown property: "+propertyName);
        }
        
    }

}
