/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.impl;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.model.AbstractClass;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class AbstractClassImpl<T extends AbstractClass> {
    static String PARAM_MODEL_IDS = "modelIds";
    static String PARAM_PARENT_IDS = "parentIds";
    static String PARAM_URI_PREFIX = "uriPrefix";

    public CollectionWithPagingInfo<T> createPagedResult(List<T> list, Paging paging)
    {
        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int totalItems = list.size();

        Collections.sort(list);

        if (skipCount >= totalItems)
        {
            List<T> empty = Collections.emptyList();
            return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
        }
        else
        {
            int end = Math.min(skipCount + maxItems, totalItems);
            boolean hasMoreItems = totalItems > end;
            list = list.subList(skipCount, end);
            return CollectionWithPagingInfo.asPaged(paging, list, hasMoreItems, totalItems);
        }
    }

    public boolean filterByNamespace(ModelApiFilter query, QName type)
    {
        //System aspect/type is not allowed
        if (type.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI))
        {
            return false;
        }
        if (query != null && query.getMatchedPrefix() != null)
        {
            return Pattern.matches(query.getMatchedPrefix(), type.getNamespaceURI());
        }
        if (query != null && query.getNotMatchedPrefix() != null)
        {
            return  !Pattern.matches(query.getNotMatchedPrefix(), type.getNamespaceURI());
        }
        return  true;
    }

    public ModelApiFilter getQuery(Query queryParameters)
    {
        if (queryParameters != null)
        {
            ClassQueryWalker propertyWalker = new ClassQueryWalker();
            QueryHelper.walk(queryParameters, propertyWalker);
            return new ModelApiFilter(propertyWalker.getModelIds(), propertyWalker.getParentIds(), propertyWalker.getMatchedPrefix(), propertyWalker.getNotMatchedPrefix());
        }
        return null;
    }

    public static class ClassQueryWalker extends MapBasedQueryWalker
    {
        private String notMatchedPrefix = null;
        private String matchedPrefix = null;

        public ClassQueryWalker()
        {
            super(new HashSet<>(Arrays.asList(PARAM_MODEL_IDS, PARAM_PARENT_IDS)), new HashSet<>(Collections.singleton(PARAM_URI_PREFIX)));
        }


        @Override
        public void matches(String property, String value, boolean negated)
        {
            if (negated && property.equals(PARAM_URI_PREFIX))
            {
                notMatchedPrefix = value;
            }
            else if (property.equals(PARAM_URI_PREFIX))
            {
                matchedPrefix = value;
            }
        }

        private Set<String> parseProperty(String property)
        {
            String propertyParam = getProperty(property, WhereClauseParser.EQUALS, String.class);
            Set<String> ids = null;

            if (propertyParam != null)
            {
                ids = new HashSet<>(Arrays.asList(propertyParam.trim().split(",")));
            }
            return ids;
        }

        public Set<String> getModelIds()
        {
            return parseProperty(PARAM_MODEL_IDS);
        }

        public Set<String> getParentIds()
        {
            return parseProperty(PARAM_PARENT_IDS);
        }

        public String getNotMatchedPrefix()
        {
            return this.notMatchedPrefix;
        }

        public String getMatchedPrefix()
        {
            return this.matchedPrefix;
        }
    }

    public static class ModelApiFilter
    {
        private Set<String> modelIds;
        private Set<String> parentIds;
        private String matchedPrefix;
        private String notMatchedPrefix;

        public ModelApiFilter(Set<String> modelIds, Set<String> parentIds, String matchedPrefix, String notMatchedPrefix)
        {
            this.modelIds = modelIds;
            this.parentIds = parentIds;
            this.matchedPrefix = matchedPrefix;
            this.notMatchedPrefix = notMatchedPrefix;
        }

        public Set<String> getModelIds()
        {
            return modelIds;
        }

        public String getMatchedPrefix()
        {
            return matchedPrefix;
        }

        public String getNotMatchedPrefix()
        {
            return notMatchedPrefix;
        }

        public Set<String> getParentIds()
        {
            return parentIds;
        }
    }
}
