/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.search.impl.AbstractCategoryServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * @author Andy
 *
 */
public class SolrCategoryServiceImpl extends AbstractCategoryServiceImpl
{

    @Override
    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        final SearchParameters searchParameters = createSearchTopCategoriesParameters(storeRef, aspectName, count);
        searchParameters.setLanguage(SearchService.LANGUAGE_INDEX_FTS_ALFRESCO);
        final String field = searchParameters.getFieldFacets().stream()
                .map(FieldFacet::getField)
                .findFirst()
                .orElse("");
        
        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(searchParameters);
            final List<Pair<String, Integer>> facetCounts = resultSet.getFieldFacet(field);
            final List<Pair<NodeRef, Integer>> answer = new LinkedList<>();
            for (Pair<String, Integer> term : facetCounts)
            {
                Pair<NodeRef, Integer> toAdd;
                final NodeRef nodeRef = new NodeRef(term.getFirst());
                if (nodeService.exists(nodeRef))
                {
                    toAdd = new Pair<>(nodeRef, term.getSecond());
                }
                else
                {
                    toAdd = new Pair<>(null, term.getSecond());
                }
                answer.add(toAdd);
            }
            return answer;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        
    }

}
