/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.impl;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Aspects;
import org.alfresco.rest.api.NodeDefinitionMapper;
import org.alfresco.rest.api.model.*;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

import java.util.*;
import java.util.stream.Collectors;

public class AspectsImpl implements Aspects
{

    static String PARAM_MODEL_IDS = "modelIds";
    static String PARAM_CUSTOM_MODEL_ONLY = "customModelOnly";

    protected DictionaryService dictionaryService;
    protected CustomModelsImpl customModels;
    protected NamespacePrefixResolver namespaceService;
    protected NodeDefinitionMapper nodeDefinitionMapper;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setCustomModels(CustomModelsImpl customModels)
    {
        this.customModels = customModels;
    }

    public void setNamespaceService(NamespacePrefixResolver namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeDefinitionMapper(NodeDefinitionMapper nodeDefinitionMapper)
    {
        this.nodeDefinitionMapper = nodeDefinitionMapper;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "customModels", customModels);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "nodeDefinitionMapper", nodeDefinitionMapper);
    }


    @Override
    public CollectionWithPagingInfo<Aspect> listAspects(Parameters params)
    {
        Paging paging = params.getPaging();

        MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(new HashSet<>(Arrays.asList(PARAM_CUSTOM_MODEL_ONLY, PARAM_MODEL_IDS)), null);;
        QueryHelper.walk(params.getQuery(), propertyWalker);

        String queryIds = propertyWalker.getProperty(PARAM_MODEL_IDS, WhereClauseParser.EQUALS, String.class);
        String[] ids = queryIds != null ? queryIds.split(",") : null;

        Boolean customModelOnly = propertyWalker.getProperty(PARAM_CUSTOM_MODEL_ONLY, WhereClauseParser.EQUALS, Boolean.class);

        if(customModelOnly != null && customModelOnly)
        {
            List<Aspect> allAspects = this.customModels.getCustomModels(params).getCollection().stream().map(CustomModel::getAspects)
                    .flatMap(Collection::stream)
                    .map(customAspect -> listAspectById(customAspect.getPrefixedName()))
                    .collect(Collectors.toList());
            return createPagedResult(allAspects, paging);
        }

        Collection<QName> aspectList = null;
        if(ids != null)
        {
            aspectList = Arrays.stream(ids)
                    .map((prefixedName) -> {
                        ModelDefinition modelDefinition = this.dictionaryService.getModel(QName.createQName(prefixedName));
                        return this.dictionaryService.getAspects(modelDefinition.getName());
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        else
        {
            aspectList = this.dictionaryService.getAllAspects();
        }

        List<Aspect> allAspects = aspectList.parallelStream()
                .filter((qName) -> !qName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI))
                .map((qName) -> this.convertToAspect(dictionaryService.getAspect(qName)))
                .collect(Collectors.toList());

        return createPagedResult(allAspects, paging);
    }

    @Override
    public Aspect listAspectById(String prefixedName)
    {
        AspectDefinition aspectDefinition = dictionaryService.getAspect(QName.createQName(prefixedName, this.namespaceService));
        return this.convertToAspect(aspectDefinition);
    }

    public Aspect convertToAspect(AspectDefinition aspectDefinition)
    {
        List<NodeDefinitionProperty> properties = this.nodeDefinitionMapper.fromAspectDefinition(aspectDefinition, dictionaryService).getProperties();
        return new Aspect(aspectDefinition, dictionaryService, properties);
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
}
