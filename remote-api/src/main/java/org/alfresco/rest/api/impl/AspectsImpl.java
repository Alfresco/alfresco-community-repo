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
import org.alfresco.rest.api.Aspects;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.model.Aspect;
import org.alfresco.rest.api.model.PropertyDefinition;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.util.PropertyCheck;

import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AspectsImpl implements Aspects
{
    static String PARAM_MODEL_IDS = "modelIds";
    static String PARAM_PARENT_IDS = "parentIds";
    static String PARAM_URI_PREFIX = "uriPrefix";

    protected DictionaryService dictionaryService;
    protected NamespacePrefixResolver namespaceService;
    protected ClassDefinitionMapper classDefinitionMapper;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespacePrefixResolver namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setClassDefinitionMapper(ClassDefinitionMapper classDefinitionMapper)
    {
        this.classDefinitionMapper = classDefinitionMapper;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "classDefinitionMapper", classDefinitionMapper);
    }


    @Override
    public CollectionWithPagingInfo<Aspect> listAspects(Parameters params)
    {
        Paging paging = params.getPaging();
        AspectsFilter query = getQuery(params.getQuery());
        Stream<QName> aspectList = null;

        if (query != null && query.getModelIds() != null)
        {
            aspectList = query.getModelIds().parallelStream().map(this::getModelAspects).flatMap(Collection::parallelStream);
        }
        else if (query != null && query.getParentIds() != null)
        {
            aspectList = query.getParentIds().parallelStream().map(this::getChildAspects).flatMap(Collection::parallelStream);
        }
        else
        {
            aspectList = this.dictionaryService.getAllAspects().parallelStream();
        }

        List<Aspect> allAspects = aspectList.filter((qName) -> filterAspect(query, qName))
                .map((qName) -> this.convertToAspect(dictionaryService.getAspect(qName)))
                .collect(Collectors.toList());
        return createPagedResult(allAspects, paging);
    }

    @Override
    public Aspect getAspectById(String aspectId)
    {
        if (aspectId == null)
            throw new InvalidArgumentException("Invalid parameter: unknown scheme specified");

        AspectDefinition aspectDefinition = null;

        try
        {
           aspectDefinition = dictionaryService.getAspect(QName.createQName(aspectId, this.namespaceService));
        }
        catch (NamespaceException exception)
        {
            throw new EntityNotFoundException(aspectId);
        }

        if (aspectDefinition == null)
            throw new EntityNotFoundException(aspectId);

        return this.convertToAspect(aspectDefinition);
    }

    public Aspect convertToAspect(AspectDefinition aspectDefinition)
    {
        List<PropertyDefinition> properties = this.classDefinitionMapper.fromDictionaryClassDefinition(aspectDefinition, dictionaryService).getProperties();
        return new Aspect(aspectDefinition, dictionaryService, properties);
    }

    public AspectsFilter getQuery(Query queryParameters)
    {
        if (queryParameters != null)
        {
            AspectQueryWalker propertyWalker = new AspectQueryWalker();
            QueryHelper.walk(queryParameters, propertyWalker);
            return new AspectsFilter(propertyWalker.getModelIds(), propertyWalker.getParentIds(), propertyWalker.getMatchedPrefix(), propertyWalker.getNotMatchedPrefix());
        }
        return null;
    }

    private Collection<QName> getModelAspects(String modelId)
    {
        ModelDefinition modelDefinition =  null;

        if (modelId == null)
            throw new InvalidArgumentException("modelId is null");

        try
        {
            modelDefinition =  this.dictionaryService.getModel(QName.createQName(modelId, this.namespaceService));
        }
        catch (NamespaceException exception)
        {
            throw new InvalidArgumentException(exception.getMessage());
        }

        return this.dictionaryService.getAspects(modelDefinition.getName());
    }

    private Collection<QName> getChildAspects(String aspectId)
    {
        Collection<QName> subAspects = null;
        try
        {
            QName parentAspect = QName.createQName(aspectId, this.namespaceService);
            subAspects = this.dictionaryService.getSubAspects(parentAspect, true);
        }
        catch (NamespaceException exception)
        {
            throw new InvalidArgumentException(exception.getMessage());
        }

        return subAspects;
    }

    private boolean filterAspect(AspectsFilter query, QName aspect)
    {
        // should not allow the system aspect
        if (aspect.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI))
        {
            return false;
        }
        if (query != null && query.getMatchedPrefix() != null)
        {
            return Pattern.matches(query.getMatchedPrefix(), aspect.getNamespaceURI());
        }
        if (query != null && query.getNotMatchedPrefix() != null)
        {
            return  !Pattern.matches(query.getNotMatchedPrefix(), aspect.getNamespaceURI());
        }
        return  true;
    }

    private CollectionWithPagingInfo<Aspect> createPagedResult(List<Aspect> list, Paging paging)
    {
        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int totalItems = list.size();

        Collections.sort(list);

        if (skipCount >= totalItems)
        {
            List<Aspect> empty = Collections.emptyList();
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

    public static class AspectQueryWalker extends MapBasedQueryWalker
    {
        private String notMatchedPrefix = null;
        private String matchedPrefix = null;

        public AspectQueryWalker()
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

    public static class AspectsFilter
    {
        private Set<String> modelIds;
        private Set<String> parentIds;
        private String matchedPrefix;
        private String notMatchedPrefix;

        public AspectsFilter(Set<String> modelIds, Set<String> parentIds, String matchedPrefix, String notMatchedPrefix)
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