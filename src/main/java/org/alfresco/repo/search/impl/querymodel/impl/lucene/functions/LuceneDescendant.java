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
package org.alfresco.repo.search.impl.querymodel.impl.lucene.functions;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.adaptor.lucene.QueryConstants;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Descendant;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @author andyh
 *
 */
public class LuceneDescendant<Q, S, E extends Throwable> extends Descendant implements LuceneQueryBuilderComponent<Q, S, E>
{

    /**
     * 
     */
    public LuceneDescendant()
    {
        super();
    }

    private StoreRef getStore(LuceneQueryBuilderContext<Q, S, E> luceneContext)
    {
    	ArrayList<StoreRef> stores = luceneContext.getLuceneQueryParserAdaptor().getSearchParameters().getStores();
    	if(stores.size() < 1)
    	{
    		// default
    		return StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
    	}
    	return stores.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent#addComponent(org.apache.lucene.search.BooleanQuery,
     *      org.apache.lucene.search.BooleanQuery, org.alfresco.service.cmr.dictionary.DictionaryService,
     *      java.lang.String)
     */
    public Q addComponent(Set<String> selectors, Map<String, Argument> functionArgs, LuceneQueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext)
            throws E
    {
        LuceneQueryParserAdaptor<Q, S, E> lqpa = luceneContext.getLuceneQueryParserAdaptor();
        Argument argument = functionArgs.get(ARG_ANCESTOR);
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
                throw new QueryModelException("Selector must be specified for child constraint (IN_TREE) and join"); 
            }
        }

        NodeRef nodeRef;
        if(NodeRef.isNodeRef(id))
        {
            nodeRef = new NodeRef(id);
        }
        else
        {
        	// assume id is the node uuid e.g. for OpenCMIS
        	StoreRef storeRef = getStore(luceneContext);
        	nodeRef = new NodeRef(storeRef, id);
        }

        // Lucene world 
        if(functionContext.getNodeService() != null)
        {
            if(!functionContext.getNodeService().exists(nodeRef))
            {
                throw new QueryModelException("Object does not exist: "+id); 
            }
            Path path = functionContext.getNodeService().getPath(nodeRef);
            StringBuilder builder = new StringBuilder(path.toPrefixString(luceneContext.getNamespacePrefixResolver()));
            builder.append("//*");
            Q query = lqpa.getFieldQuery(QueryConstants.FIELD_PATH, builder.toString());
            return query;
        }
        // SOLR
        else
        {
            Q query = lqpa.getFieldQuery(QueryConstants.FIELD_ANCESTOR, nodeRef.toString());
            return query;
        }
        
    }
    
}
