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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.rest.api.Aspects;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.model.Aspect;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AspectsImpl extends AbstractClassImpl<Aspect> implements Aspects
{
    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespaceService;
    private ClassDefinitionMapper classDefinitionMapper;

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

    AspectsImpl(DictionaryService dictionaryService, NamespacePrefixResolver namespaceService, ClassDefinitionMapper classDefinitionMapper)
    {
        super(dictionaryService, namespaceService, classDefinitionMapper);
    }

    @Override
    public CollectionWithPagingInfo<Aspect> listAspects(Parameters params)
    {
        Paging paging = params.getPaging();
        ModelApiFilter query = getQuery(params.getQuery());
        Stream<QName> aspectStream = null;

        if (query != null && query.getModelIds() != null)
        {
            validateListParam(query.getModelIds(), PARAM_MODEL_IDS);
            Set<Pair<QName, Boolean>> modelsFilter = parseModelIds(query.getModelIds(), PARAM_INCLUDE_SUBASPECTS);
            aspectStream = modelsFilter.stream().map(this::getModelAspects).flatMap(Collection::stream);
        }
        else if (query != null && query.getParentIds() != null)
        {
            validateListParam(query.getParentIds(), PARAM_PARENT_IDS);
            aspectStream = query.getParentIds().stream().map(this::getChildAspects).flatMap(Collection::stream);
        }
        else
        {
            aspectStream = this.dictionaryService.getAllAspects().stream();
        }

        List<Aspect> allAspects = aspectStream.filter((qName) -> filterByNamespace(query, qName))
                .filter(distinctByKey(QName::getPrefixString))
                .map((qName) -> this.convertToAspect(dictionaryService.getAspect(qName), params.getInclude()))
                .collect(Collectors.toList());

        return createPagedResult(allAspects, paging);
    }

    @Override
    public Aspect getAspect(String aspectId)
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

        return this.convertToAspect(aspectDefinition, ALL_PROPERTIES);
    }

    public Aspect convertToAspect(AspectDefinition aspectDefinition, List<String> includes)
    {
        try
        {
            Aspect aspect = new Aspect(aspectDefinition, dictionaryService);
            constructFromFilters(aspect, aspectDefinition, includes);
            return aspect;
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("Failed to parse Aspect: " + aspectDefinition.getName() + " . " + ex.getMessage());
        }
    }

    private Collection<QName> getModelAspects(Pair<QName,Boolean> model)
    {
        ModelDefinition modelDefinition =  null;

        try
        {
            modelDefinition =  this.dictionaryService.getModel(model.getFirst());
        }
        catch (Exception exception)
        {
            throw new InvalidArgumentException(exception.getMessage());
        }

        if (modelDefinition == null)
            throw new EntityNotFoundException("model");

        Collection<QName> aspects = this.dictionaryService.getAspects(modelDefinition.getName());

        if (!model.getSecond()) // look for model aspects alone
            return aspects;

        Stream<QName> aspectStream = aspects.stream();
        Stream<QName> childrenStream = aspects.stream()
                .map(aspect -> this.dictionaryService.getSubAspects(aspect, false))
                .flatMap(Collection::stream);

        return Stream.concat(aspectStream, childrenStream).collect(Collectors.toList());
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
}