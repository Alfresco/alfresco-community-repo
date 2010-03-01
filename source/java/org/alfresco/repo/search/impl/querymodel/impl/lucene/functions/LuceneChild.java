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
package org.alfresco.repo.search.impl.querymodel.impl.lucene.functions;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Child;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

/**
 * @author andyh
 *
 */
public class LuceneChild extends Child implements LuceneQueryBuilderComponent
{

    /**
     * 
     */
    public LuceneChild()
    {
        super();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent#addComponent(org.apache.lucene.search.BooleanQuery,
     *      org.apache.lucene.search.BooleanQuery, org.alfresco.service.cmr.dictionary.DictionaryService,
     *      java.lang.String)
     */
    public Query addComponent(Set<String> selectors, Map<String, Argument> functionArgs, LuceneQueryBuilderContext luceneContext, FunctionEvaluationContext functionContext)
            throws ParseException
    {
        LuceneQueryParser lqp = luceneContext.getLuceneQueryParser();
        Argument argument = functionArgs.get(ARG_PARENT);
        String id = (String) argument.getValue(functionContext);
        argument = functionArgs.get(ARG_SELECTOR);
        if(argument != null)
        {
            String selector = (String) argument.getValue(functionContext);
            if(!selectors.contains(selector))
            {
                throw new QueryModelException("Unkown selector "+selector); 
            }
        }
        else
        {
            if(selectors.size() > 1)
            {
                throw new QueryModelException("Selector must be specified for child constraint (IN_FOLDER) and join"); 
            }
        }
        NodeRef nodeRef;
        if(NodeRef.isNodeRef(id))
        {
            nodeRef = new NodeRef(id);
        }
        else
        {
            int lastIndex = id.lastIndexOf('/');
            String versionLabel = id.substring(lastIndex+1);
            String actualId = id.substring(0, lastIndex);
            if(NodeRef.isNodeRef(actualId))
            {
                nodeRef = new NodeRef(actualId);
                Serializable value = functionContext.getNodeService().getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
                if (value != null)
                {
                    String actualVersionLabel = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                    if(!actualVersionLabel.equals(versionLabel))
                    {
                        throw new QueryModelException("Object id does not refer to the current version"+id);
                    }
                }
            }
            else
            {
                throw new QueryModelException("Invalid Object Id "+id);
            }
        }
        if(!functionContext.getNodeService().exists(nodeRef))
        {
            throw new QueryModelException("Object does not exist: "+id); 
        }
        Query query = lqp.getFieldQuery("PARENT", nodeRef.toString());
        return query;

    }

}
